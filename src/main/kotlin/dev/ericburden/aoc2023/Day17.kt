package dev.ericburden.aoc2023

import dev.ericburden.aoc2023.Utils.Index2D
import dev.ericburden.aoc2023.Utils.Offset2D
import dev.ericburden.aoc2023.Utils.plus
import java.util.PriorityQueue

/**
 * This enum represents the four cardinal directions
 *
 * The four cardinal directions are north, south, east, and west. This
 * enum also provides methods for turning from one cardinal direction
 * to another.
 */
enum class CardinalDirection {
    NORTH, SOUTH, EAST, WEST;

    // Turn this direction to the left
    fun turnLeft(): CardinalDirection = when (this) {
        NORTH -> WEST
        EAST -> NORTH
        SOUTH -> EAST
        WEST -> SOUTH
    }

    // Turn this direction to the right
    fun turnRight(): CardinalDirection = when (this) {
        NORTH -> EAST
        EAST -> SOUTH
        SOUTH -> WEST
        WEST -> NORTH
    }
}

// In part two, we have ULTRA crucibles, which behave a little differently
enum class CrucibleKind { REGULAR, ULTRA; }

/**
 * This class represents a big ole crucible, rolling through the streets
 *
 * @property position The location of the crucible on the 2D grid.
 * @property direction The direction the crucible is currently heading.
 * @property run The number of blocks this crucible has moved in a straight line.
 * @property kind The kind of crucible we have (REGULAR or ULTRA).
 */
data class Crucible(
    val position: Index2D,
    val direction: CardinalDirection,
    val run: Int,
    val kind: CrucibleKind = CrucibleKind.REGULAR
) {
    /**
     * Move the crucible forward.
     *
     * @return The crucible that would result from moving this crucible forward
     * one step in the direction it is currently going.
     */
    private fun advance(): Crucible {
        // Update the position with offsets.
        val position = position + when (direction) {
            CardinalDirection.NORTH -> Offset2D(-1, 0)
            CardinalDirection.EAST -> Offset2D(0, 1)
            CardinalDirection.SOUTH -> Offset2D(1, 0)
            CardinalDirection.WEST -> Offset2D(0, -1)
        }

        // Increase the run distance
        val run = run + 1

        // Everything else stays the same
        return Crucible(position, direction, run, kind)
    }

    /**
     * Turn the crucible to the left without moving forward
     *
     * @return The crucible that results from turning this crucible to the left.
     */
    private fun turnLeft(): Crucible {
        // Change heading
        val direction = direction.turnLeft()

        // Reset the run to zero when we turn
        return Crucible(position, direction, 0, kind)
    }

    /**
     * Turn the crucible to the right without moving forward
     *
     * @return The crucible that results from turning this crucible to the right.
     */
    private fun turnRight(): Crucible {
        // Change heading
        val direction = direction.turnRight()

        // Reset the run to zero when we turn
        return Crucible(position, direction, 0, kind)
    }

    /**
     * Identify which states are possible for this crucible
     *
     * @return A list of all the possible next states for this crucible based
     * on its current state.
     */
    fun nextStates() = when (kind) {
        // A regular crucible (part one) can always move left or right, but
        // it can only move forward when the run length is less than three.
        CrucibleKind.REGULAR -> if (run < 3) {
            listOf(
                advance(),
                turnLeft().advance(),
                turnRight().advance()
            )
        } else {
            listOf(
                turnLeft().advance(),
                turnRight().advance()
            )
        }

        // An _ultra_ crucible (part two) can _only_ move forward if its run
        // length is less than four (or it's on the first space). With run
        // lengths of 4-9 (inclusive) it can move forward, left, or right just
        // like a regular crucible. At a run length of 10, it must turn.
        CrucibleKind.ULTRA -> if (run < 4 && (position.row != 0 || position.col != 0)) {
            listOf(advance())
        } else if (run < 10) {
            listOf(
                advance(),
                turnLeft().advance(),
                turnRight().advance()
            )
        } else {
            listOf(
                turnLeft().advance(),
                turnRight().advance()
            )
        }
    }

    /**
     * Indicates whether this crucible can stop
     *
     * A regular crucible can stop any time, but an _ultra_ crucible can only
     * stop after moving at least four blocks in a straight line.
     *
     * @return A flag indicating whether this crucible can stop moving.
     */
    fun canStop() = when (kind) {
        CrucibleKind.REGULAR -> true
        CrucibleKind.ULTRA -> run >= 4
    }
}

/**
 * This class represents the grid of city blocks (and their heat loss amounts)
 *
 * @property grid The grid of heat loss amounts for each city block.
 */
data class CityBlocks(val grid: List<List<Int>>) {
    // Convenience!
    val rows: Int get() = grid.size
    val cols: Int get() = grid.first().size

    // Index a [LaserGrid] with an [Index2D].
    operator fun get(idx: Index2D): Int = grid[idx.row][idx.col]

    /**
     * Check if a crucible is still on the grid
     *
     * @param crucible The crucible to check.
     * @return Is the laser still on the grid?
     */
    private fun isInBounds(crucible: Crucible): Boolean {
        val (row, col) = crucible.position
        return row in 0..<rows && col in 0..<cols
    }

    /**
     * Find the shortest path from a starting [Crucible] state to the end index
     *
     * This is an implementation of Dijkstra's algorithm for finding the
     * shortest path. I _considered_ going full A* here, but this runs in
     * a sufficiently reasonable amount of time, so I'll leave it as-is.
     *
     * @param start The starting state of our crucible.
     * @param end The index we're pathfinding to.
     */
    fun shortestPath(start: Crucible, end: Index2D): Int {
        // Minimum priority queue of <heat loss>, <crucible> pairs, ordered by
        // minimum heat loss.
        val queue =
            PriorityQueue<Pair<Int, Crucible>> { o1, o2 -> o1.first - o2.first }
        queue.add(0 to start)

        // Mapping of minimum heat attained per crucible state. This can't just
        // be position, because the path from a particular location will vary
        // depending on the direction and run length as well.
        val minHeatLosses = HashMap<Crucible, Int>()
        minHeatLosses[start] = 0

        // Find that path!
        while (queue.isNotEmpty()) {
            // Get the state with the minimum heat loss so far from the queue
            val (heatLost, crucible) = queue.remove()

            // If we've reached the end, we win!
            if (crucible.position == end && crucible.canStop()) return heatLost

            // Otherwise, identify all the possible next states for the
            // current crucible.
            val nextStates = crucible.nextStates().filter { isInBounds(it) }

            // For each possible future state...
            for (state in nextStates) {
                // Check to see if the heat loss for reaching this new state
                // can be less than any previous amount of heat lost reaching
                // this state.
                val newHeatLoss = heatLost + this[state.position]
                val previousMinHeatLoss = minHeatLosses[state] ?: Int.MAX_VALUE

                // If so, we've found a new shortest path to this state. Add
                // it to the queue!
                if (newHeatLoss < previousMinHeatLoss) {
                    minHeatLosses[state] = newHeatLoss
                    queue.add(newHeatLoss to state)
                }

            }
        }

        // When disaster strikes! If we search through all possible paths and
        // don't reach the end _ever_ we get this exception.
        throw Exception("Could not find a path to $end from $start!")
    }
}

class Day17(input: List<List<Int>>) {

    // Put the grid in the data class and get ready to rock.
    private val parsed = CityBlocks(input)

    // In part one, we use regular, everyday giant rolling crucibles to move
    // lava around city streets. Totally safe.
    fun solvePart1(): Int {
        val topLeftCrucible = Crucible(Index2D(0, 0), CardinalDirection.EAST, 0)
        val bottomRightIdx = Index2D(parsed.rows - 1, parsed.cols - 1)
        return parsed.shortestPath(topLeftCrucible, bottomRightIdx)
    }

    // In part two, we use _ultra_ crucibles, which are way more ultra than
    // regular crucibles in every conceivable way.
    fun solvePart2(): Int {
        val topLeftCrucible = Crucible(
            Index2D(0, 0), CardinalDirection.EAST, 0, CrucibleKind.ULTRA
        )
        val bottomRightIdx = Index2D(parsed.rows - 1, parsed.cols - 1)
        return parsed.shortestPath(topLeftCrucible, bottomRightIdx)
    }
}
