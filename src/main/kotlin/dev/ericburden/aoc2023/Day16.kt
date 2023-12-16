package dev.ericburden.aoc2023

/**
 * This class serves as the superclass for all grid cells
 *
 * Each cell in the grid is represented by a subclass of this
 * [AbstractLaserGridCell]. All cells start out without energy but can be
 * energized by having the laser pass through the cell.
 *
 * @property energized Indicates whether a laser has passed through this cell.
 */
abstract class AbstractLaserGridCell(var energized: Boolean = false) {
    companion object {
        /**
         * Parse a concrete instance of an [AbstractLaserGridCell] from a character
         *
         * Each character in the input corresponds to a particular subclass of
         * [AbstractLaserGridCell].
         *
         * @param char A character from the input file.
         * @return The corresponding concrete grid cell.
         */
        fun fromChar(char: Char): AbstractLaserGridCell {
            return when (char) {
                '.' -> EmptyGridCell(false)
                '|' -> VerticalSplitterGridCell(false)
                '-' -> HorizontalSplitterGridCell(false)
                '/' -> RightLeaningMirrorGridCell(false)
                '\\' -> LeftLeaningMirrorGridCell(false)
                else -> throw Exception("$char is not a valid kind of grid cell!")
            }
        }
    }

    /**
     * Energize this cell when it's shot with a laser
     */
    fun energize() {
        this.energized = true
    }

    // Deflects a laser to the next set of cells
    abstract fun deflect(laser: Laser): List<Laser>

    // For printing! By which I mean debugging!
    abstract override fun toString(): String

    // We need to be able to copy these so we can avoid issues with sneaky
    // mutations.
    abstract fun copy(): AbstractLaserGridCell
}

// This subclass represents an empty grid cell, '.'
class EmptyGridCell(energized: Boolean) : AbstractLaserGridCell(energized) {
    // Lasers that enter this cell are just forwarded to the next cell.
    override fun deflect(laser: Laser): List<Laser> =
        listOf(laser.advance())

    override fun toString(): String = if (energized) "#" else "."

    override fun copy() = EmptyGridCell(energized)
}

// This subclass represents a vertical splitter, '|'
class VerticalSplitterGridCell(energized: Boolean) :
    AbstractLaserGridCell(energized) {
    // Lasers in this cell moving north or south just keep going. Lasers
    // moving east or west are split.
    override fun deflect(laser: Laser): List<Laser> = when (laser.heading) {
        LaserHeading.NORTH -> listOf(laser.advance())
        LaserHeading.SOUTH -> listOf(laser.advance())
        else -> listOf(laser.turnLeftAndAdvance(), laser.turnRightAndAdvance())
    }

    override fun toString(): String = if (energized) "#" else "|"

    override fun copy() = VerticalSplitterGridCell(energized)
}

// This subclass represents a horizontal splitter, '-'
class HorizontalSplitterGridCell(energized: Boolean) :
    AbstractLaserGridCell(energized) {
    // Lasers in this cell heading east and west just keep going. Lasers
    // moving north or south are split.
    override fun deflect(laser: Laser): List<Laser> = when (laser.heading) {
        LaserHeading.EAST -> listOf(laser.advance())
        LaserHeading.WEST -> listOf(laser.advance())
        else -> listOf(laser.turnLeftAndAdvance(), laser.turnRightAndAdvance())
    }

    override fun toString(): String = if (energized) "#" else "-"

    override fun copy() = HorizontalSplitterGridCell(energized)
}

// This subclass represents a right-leaning mirror, '/'
class RightLeaningMirrorGridCell(energized: Boolean) :
    AbstractLaserGridCell(energized) {
    // Lasers in this cell heading north or south are turned to the right.
    // Lasers going east or west are turned to the left.
    override fun deflect(laser: Laser): List<Laser> = when (laser.heading) {
        LaserHeading.NORTH -> listOf(laser.turnRightAndAdvance())
        LaserHeading.EAST -> listOf(laser.turnLeftAndAdvance())
        LaserHeading.SOUTH -> listOf(laser.turnRightAndAdvance())
        LaserHeading.WEST -> listOf(laser.turnLeftAndAdvance())
    }

    override fun toString(): String = if (energized) "#" else "/"

    override fun copy() = RightLeaningMirrorGridCell(energized)
}

// This subclass represents a left-leaning mirror, '\'
class LeftLeaningMirrorGridCell(energized: Boolean) :
    AbstractLaserGridCell(energized) {
    // Lasers in this cell heading north or south are turned to the left.
    // Lasers going east or west are turned to the right.
    override fun deflect(laser: Laser): List<Laser> = when (laser.heading) {
        LaserHeading.NORTH -> listOf(laser.turnLeftAndAdvance())
        LaserHeading.EAST -> listOf(laser.turnRightAndAdvance())
        LaserHeading.SOUTH -> listOf(laser.turnLeftAndAdvance())
        LaserHeading.WEST -> listOf(laser.turnRightAndAdvance())
    }

    override fun toString(): String = if (energized) "#" else "\\"

    override fun copy() = LeftLeaningMirrorGridCell(energized)
}

// This class represents an index in a 2-dimensional grid
data class Index2D(val row: Int, val col: Int)

// This class represents an offset from a grid position. Used to shift
// a position by adding this offset to a position.
data class Offset2D(val rows: Int, val cols: Int)

// Implement adding [Offset2D] to an [Index2D]
operator fun Index2D.plus(offset: Offset2D): Index2D =
    Index2D(row + offset.rows, col + offset.cols)

// Convenience!
fun MutableList<MutableList<AbstractLaserGridCell>>.wrap(): LaserGrid =
    LaserGrid(this)

/**
 * This enum class represents all four directions a laser could travel in
 *
 * A laser can travel north, south, east, or west.
 */
enum class LaserHeading {
    NORTH, EAST, SOUTH, WEST;

    // Turn this direction to the left
    fun turnLeft(): LaserHeading = when (this) {
        NORTH -> WEST
        EAST -> NORTH
        SOUTH -> EAST
        WEST -> SOUTH
    }

    // Turn this direction to the right
    fun turnRight(): LaserHeading = when (this) {
        NORTH -> EAST
        EAST -> SOUTH
        SOUTH -> WEST
        WEST -> NORTH
    }
}

/**
 * This class represents a _LAZER_
 *
 * I really thought about using another abstract/subclass setup for lasers,
 * and even started down that path, but it actually made my code _more_
 * complicated (to me, at least).
 *
 * @property position The position of the laser in a grid.
 * @property heading The direction this laser is moving.
 */
data class Laser(
    val position: Index2D = Index2D(0, 0),
    val heading: LaserHeading = LaserHeading.EAST
) {
    /**
     * Advance the laser forward by one space
     *
     * @return A Laser in the next position.
     */
    fun advance(): Laser {
        val newPosition = position + when (heading) {
            LaserHeading.NORTH -> Offset2D(-1, 0)
            LaserHeading.EAST -> Offset2D(0, 1)
            LaserHeading.SOUTH -> Offset2D(1, 0)
            LaserHeading.WEST -> Offset2D(0, -1)
        }
        return Laser(newPosition, heading)
    }

    // Turn the laser right and move it forward one space
    fun turnRightAndAdvance() = Laser(position, heading.turnRight()).advance()

    // Turn the laser left and move it forward one space
    fun turnLeftAndAdvance() = Laser(position, heading.turnLeft()).advance()
}

/**
 * This class represents a grid of cells that we can shoot lasers through
 *
 * @property grid The grid of cells composing this [LaserGrid].
 */
data class LaserGrid(val grid: MutableList<MutableList<AbstractLaserGridCell>>) {
    companion object {
        /**
         * Parse a [LaserGrid] from the input file
         *
         * @param input A grid of characters from the input.
         * @return A parsed [LaserGrid].
         */
        fun fromInput(input: List<List<Char>>): LaserGrid = input.map { row ->
            row.map(AbstractLaserGridCell::fromChar).toMutableList()
        }.toMutableList().wrap()
    }

    private val rows: Int get() = grid.size
    private val cols: Int get() = grid.first().size

    // Still debugging!
    override fun toString(): String =
        grid.joinToString("\n") { row -> row.joinToString("") { cell -> cell.toString() } }

    // Index a [LaserGrid] with an [Index2D].
    operator fun get(idx: Index2D): AbstractLaserGridCell =
        grid[idx.row][idx.col]

    // Set values in a [LaserGrid] with an [Index2D].
    operator fun set(position: Index2D, value: AbstractLaserGridCell) {
        this.grid[position.row][position.col] = value
    }

    /**
     * Check if a laser is still on the grid
     *
     * @param laser The laser to check.
     * @return Is the laser still on the grid?
     */
    private fun isInBounds(laser: Laser): Boolean {
        val (row, col) = laser.position
        return row in 0..<rows && col in 0..<cols
    }

    /**
     * Shoot tha lazerz!!!
     *
     * Given a laser at a particular position and heading, allow that laser to
     * move around the grid energizing cells while being split and turned. Once
     * the full path of the laser has been determined, return a copy of this
     * [LaserGrid] with the cells the laser passed through energized.
     *
     * @param laser The starting [Laser]
     * @return A copy of this [LaserGrid], with cells exposed to laser energized.
     */
    fun applyLaser(laser: Laser): LaserGrid {
        // Going to trace the path of the laser with a depth-first search,
        // so we're going to need a stack.
        val stack = mutableListOf(laser)

        // Just in case there are loops in the path of the laser, this will
        // keep us from tracing those loops over and over again.
        val seen = HashSet<Laser>()

        // Work on and return a deep copy of the current grid, to avoid
        // issues with persistent mutations.
        val laserGrid =
            this.grid.map { row -> row.map { it.copy() }.toMutableList() }
                .toMutableList().wrap()

        // Now, so long as there are lasers still moving around, we take the
        // next laser to move off the stack and move it forward, turning and
        // splitting as necessary.
        while (stack.isNotEmpty()) {
            val currentLaser = stack.removeLast()
            seen.add(currentLaser) // Won't do _that_ again!

            // Get and energize the cell currently occupied by the laser
            val currentCell = laserGrid[currentLaser.position]
            currentCell.energize()

            // Get the next laser(s) from moving this one. If we're on a
            // splitter, it might be _two_ new lasers.
            val nextLasers = currentCell.deflect(currentLaser)
                .filter { laserGrid.isInBounds(it) }

            // Add each new laser we haven't seen at this position and
            // direction to the stack to check next.
            for (nextLaser in nextLasers) {
                if (seen.contains(nextLaser)) continue
                stack.add(nextLaser)
            }
        }

        // Remember, this is a copy, not the original.
        return laserGrid
    }

    // Calculate the total number of energized cells in this [LaserGrid].
    fun totalEnergy(): Int = grid.sumOf { row -> row.count { it.energized } }

    /**
     * Determine _all_ the lasers we can pew, pew!
     *
     * Populate and return a list of all lasers that can be generated by walking
     * around the edge of the grid and shooting a laser away from the edge. As
     * described in the puzzle.
     */
    fun allPossibleLasers(): List<Laser> {
        val lasers = mutableListOf<Laser>()

        // All the left and right edges
        for (row in grid.indices) {
            lasers.add(Laser(Index2D(row, 0), LaserHeading.EAST))
            lasers.add(Laser(Index2D(row, cols - 1), LaserHeading.WEST))
        }

        // All the top and bottom edges
        for (col in grid.first().indices) {
            lasers.add(Laser(Index2D(0, col), LaserHeading.SOUTH))
            lasers.add(Laser(Index2D(rows - 1, col), LaserHeading.NORTH))
        }

        return lasers
    }

}


class Day16(input: List<List<Char>>) {

    // Turn those characters into a [LaserGrid]!
    private val parsed = LaserGrid.fromInput(input)

    // In part one, we shoot tha lazer, and see where it goes.
    fun solvePart1() = parsed.applyLaser(Laser()).totalEnergy()

    // In part two, I start to wonder whether sometimes brute force really
    // _is_ the intended answer. We check all possible starting positions
    // and directions for lasers and return the maximum grid energy found.
    fun solvePart2() =
        parsed.allPossibleLasers().maxOf { parsed.applyLaser(it).totalEnergy() }

}
