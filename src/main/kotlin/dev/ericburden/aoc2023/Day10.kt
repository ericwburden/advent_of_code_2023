package dev.ericburden.aoc2023

import java.lang.IllegalArgumentException
import java.util.*

/**
 * This class represents a location in a grid of PipeTiles
 *
 * @param row The row of this location in a grid of PipeTiles
 * @param col The column of this location in a grid of PipeTiles
 */
data class Location(val row: Int, val col: Int) {
    fun toTheSouth(): Location = Location(row + 1, col)
    fun toTheNorth(): Location = Location(row - 1, col)
    fun toTheWest(): Location = Location(row, col - 1)
    fun toTheEast(): Location = Location(row, col + 1)
}

/**
 * This class represents a tile in the grid of pipes.
 *
 * Each tile is accessible via two different directions: north, south,
 * east, or west. The exceptions are tiles with no pipe, which aren't
 * accessible via any direction, and the default value for the start,
 * which is accessible in any direction. This will need to be corrected
 * before tracing the loop of pipes.
 *
 * @property north Is this tile accessible via the north?
 * @property south Is this tile accessible via the south?
 * @property east Is this tile accessible via the east?
 * @property west Is this tile accessible via the west?
 */
data class PipeTile(var north: Boolean, var south: Boolean, var east: Boolean, var west: Boolean) {
    companion object {
        fun default(): PipeTile = PipeTile(false, false, false, false)

        fun start(): PipeTile = PipeTile(true, true, true, true)

        /**
         * Produce a [PipeTile] from a character.
         *
         * Maps the characters from the input file to the corresponding [PipeTile].
         *
         * @param char The character to map.
         * @return The corresponding [PipeTile].
         */
        fun fromChar(char: Char): PipeTile {
            return when (char) {
                '|' -> PipeTile(north = true, south = true, east = false, west = false)
                '-' -> PipeTile(north = false, south = false, east = true, west = true)
                'L' -> PipeTile(north = true, south = false, east = true, west = false)
                'J' -> PipeTile(north = true, south = false, east = false, west = true)
                '7' -> PipeTile(north = false, south = true, east = false, west = true)
                'F' -> PipeTile(north = false, south = true, east = true, west = false)
                '.' -> default()
                'S' -> start()
                else -> throw IllegalArgumentException("$char does not represent a valid pipe section!")
            }
        }
    }

    /**
     * Expands a [PipeTile] from a 1x1 to a 3x3 representation.
     *
     * Expands this [PipeTile] so that it can occupy a 3x3 grid area for filling
     * the tiles outside the loop in part two. Each direction that was accessible
     * from the [PipeTile] now contains a corresponding PIPE variant, while the
     * other tiles are PASSABLE. For example, if '#' is PIPE and '-' is PASSABLE,
     * then the following transformations would apply:
     *
     *         ---             -#-             -#-             ---
     *  '7' -> ##-      'J' -> ##-      '|' -> -#-      '-' -> ###
     *         -#-             ---             -#-             ---
     *
     * @return An [ExpandedMapTile]
     */
    fun expand(): List<List<ExpandedMapTile>> {
        val pipes = MutableList(3) { MutableList(3) { ExpandedMapTile.PASSABLE } }
        pipes[1][1] = ExpandedMapTile.PIPE
        if (north) pipes[0][1] = ExpandedMapTile.PIPE
        if (south) pipes[2][1] = ExpandedMapTile.PIPE
        if (east) pipes[1][2] = ExpandedMapTile.PIPE
        if (west) pipes[1][0] = ExpandedMapTile.PIPE
        return pipes
    }
}

/**
 * Represents a two-dimensional grid of [PipeTile]s
 *
 * @property grid A two-dimensional array containing [PipeTile]s.
 */
data class PipeGrid(val grid: List<List<PipeTile>>) {

    val rows: Int get() = grid.size
    val cols: Int get() = grid.first().size

    /**
     * Override indexing operations so that the values in the grid can be
     * accessed directly via [Location]. This operation includes bounds-checking,
     * and will return a representation of an empty tile space for an attempt to
     * access a location outside the grid.
     *
     * @param location The location of the tile to access.
     */
    operator fun get(location: Location): PipeTile {
        val (row, col) = location
        val indexOutOfBounds = (row < 0 || row >= rows || col < 0 || col >= cols)
        return if (indexOutOfBounds) PipeTile.default() else grid[row][col]
    }

    /**
     * Identify and return a list of accessible neighboring pipes.
     *
     * Checks each neighbor in each of the four cardinal directions and returns
     * a list of all neighboring locations that are accessible from the
     * given location.
     *
     * @param location The location to search for neighbors from.
     * @return A list of accessible locations.
     */
    fun getNeighborsOf(location: Location): List<Location> {
        val neighbors = mutableListOf<Location>()
        if (this[location].north && this[location.toTheNorth()].south) neighbors.add(location.toTheNorth())
        if (this[location].south && this[location.toTheSouth()].north) neighbors.add(location.toTheSouth())
        if (this[location].east && this[location.toTheEast()].west) neighbors.add(location.toTheEast())
        if (this[location].west && this[location.toTheWest()].east) neighbors.add(location.toTheWest())
        return neighbors
    }


}

/**
 * Provides a builder interface for producing a [PipeMap].
 *
 * Starting with the input file data, this class provides an API for
 * building up a [PipeMap] one step at a time.
 */
data class PipeMapBuilder(
    var grid: PipeGrid? = null,
    var start: Location? = null,
    var loopTiles: HashMap<Location, Int>? = null
) {
    /**
     * Read in the input file and fill in the [PipeGrid]
     *
     * @param input The list of lines from the input file.
     * @return The partially completed [PipeMapBuilder] with grid added.
     */
    fun readInputToGrid(input: List<String>): PipeMapBuilder {
        grid = PipeGrid(input.map { line ->
            line.map { char -> PipeTile.fromChar(char) }
        })

        return this
    }

    /**
     * Identify the starting location of the [PipeGrid].
     *
     * Iterate through the grid tiles and pick out the starting Location. The
     * starting [PipeTile] will need to be corrected from the default starting
     * configuration based on its surrounding tiles.
     *
     * @return The partially completed [PipeMapBuilder] with starting location added.
     */
    fun identifyStartLocation(): PipeMapBuilder {
        val grid = grid ?: throw Exception("Must establish grid before identify start location.")

        // Find the starting location.
        var startRow = 0
        var startCol = 0
        loop@ for ((rowIdx, row) in grid.grid.withIndex()) {
            for ((colIdx, pipe) in row.withIndex()) {
                if (pipe == PipeTile.start()) {
                    startRow = rowIdx
                    startCol = colIdx
                    break@loop
                }
            }
        }
        val startLocation = Location(startRow, startCol)

        // Correct the start location to identify which directions are actually valid
        val startPipe = PipeTile(true, true, true, true)
        if (startPipe.north && !grid[startLocation.toTheNorth()].south) grid[startLocation].north = false
        if (startPipe.south && !grid[startLocation.toTheSouth()].north) grid[startLocation].south = false
        if (startPipe.east && !grid[startLocation.toTheEast()].west) grid[startLocation].east = false
        if (startPipe.west && !grid[startLocation.toTheWest()].east) grid[startLocation].west = false
        start = startLocation

        return this
    }

    /**
     * Trace the loop of pipes attached to the start location.
     *
     * Perform a breadth-first search through the pipes, producing a mapping
     * of location to the number of steps that location is from the start.
     *
     * @return The partially completed [PipeMapBuilder] with completed `loopTiles`.
     */
    fun tracePipeLoop(): PipeMapBuilder {
        val grid = grid ?: throw Exception("Must establish grid before identify start location.")
        val start = start ?: throw Exception("Must identify starting location before tracing the pipe loop!")

        // We'll walk through using a Breadth-first search, so that we can walk away
        // from the starting location in both directions at the same time.
        val queue = ArrayDeque(listOf(0 to start))
        val seenLocations: HashMap<Location, Int> = HashMap<Location, Int>()

        // So long as there are tiles left to visit...
        while (queue.isNotEmpty()) {
            // Get the last location from the queue and add it to the
            // mapping of seen locations to number of steps taken, unless
            // we've already been there. That's probably not a concern with
            // this puzzle, but it's good practice.
            val (steps, location) = queue.removeLast()
            if (seenLocations.contains(location)) continue
            seenLocations[location] = steps

            // Add all the neighbors that haven't been visited yet to the queue.
            for (neighbor in grid.getNeighborsOf(location)) {
                if (seenLocations.contains(neighbor)) continue
                queue.addFirst(steps + 1 to neighbor)
            }
        }

        loopTiles = seenLocations

        return this
    }

    /**
     * Finalize the [PipeMapBuilder] into a [PipeMap]
     *
     * @return A [PipeMap] with all the non-null properties of the [PipeMapBuilder].
     */
    fun toPipeMap(): PipeMap {
        val grid = grid ?: throw Exception("Must establish grid before finalizing.")
        val start = start ?: throw Exception("Must identify starting location before finalizing.")
        val loopTiles = loopTiles ?: throw Exception("Must identify loop tiles before finalizing.")
        return PipeMap(grid, start, loopTiles)
    }


}

/**
 * Represents a finalized [PipeMap].
 */
data class PipeMap(
    val grid: PipeGrid,
    var start: Location,
    var loopTiles: HashMap<Location, Int>
) {
    /**
     * Return the maximum number of steps to any tile from the starting location.
     *
     * @return The maximum number of steps.
     */
    fun stepsToFurthestPointFromStart(): Int {
        return loopTiles.values.max()
    }
}

/**
 * Represents a tile type in the [ExpandedPipeMap]
 */
enum class ExpandedMapTile {
    EMPTY,      // Nothing there
    PASSABLE,   // A passable tile next to a pipe
    PIPE,       // An impassable pipe tile
    OUTER,      // An empty tile outside the pipe loop
}

/**
 * Represents an expansion of the [PipeMap] for counting inner loop tiles.
 *
 * In Part two, we need to identify tiles _inside_ the loop of the pipes, which
 * means all tiles completely enclosed by the loop. The original grid makes
 * it difficult to determine which tiles are able to be traveled to from outside
 * the loop. By exapanding each original tile from a 1x1 to a 3x3 configuration,
 * we can walk the grid between the pipes.
 *
 * @param grid A two-dimensional grid of [ExpandedMapTile]s.
 */
data class ExpandedPipeMap(var grid: MutableList<MutableList<ExpandedMapTile>>) {
    companion object {
        /**
         * Expands a [PipeMap] into an [ExpandedPipeMap].
         *
         * @param pipeMap The [PipeMap] to expand.
         * @return The expanded pipe map.
         */
        fun fromPipeMap(pipeMap: PipeMap): ExpandedPipeMap {
            // Expand all the original [PipeTile]s.
            val expansions =
                pipeMap.loopTiles.keys.map { location -> location to pipeMap.grid[location].expand() }

            // For each expanded tile, transfer the [ExpandedPipeTile] variants to
            // correct location in a grid three times the size of the original grid.
            val grid = MutableList(pipeMap.grid.rows * 3) { MutableList(pipeMap.grid.cols * 3) { ExpandedMapTile.EMPTY } }

            // For each [ExpandedPipeTile] in each expansion, transfer it to the
            // appropriate location int the expanded grid.
            for ((location, expansion) in expansions) {
                val (originalRow, originalCol) = location
                for ((rowIdx, row) in expansion.withIndex()) {
                    val newRow = (originalRow * 3) + rowIdx
                    for ((colIdx, isPipe) in row.withIndex()) {
                        val newCol = (originalCol * 3) + colIdx
                        grid[newRow][newCol] = isPipe
                    }
                }
            }

            return ExpandedPipeMap(grid)
        }
    }

    val rows: Int get() = grid.size
    val cols: Int get() = grid.first().size

    /**
     * Indicates whether the given location is accessible in the grid.
     *
     * In the expanded grid, a neighbor is accessible if it is in the grid
     * and it _isn't_ a PIPE tile.
     *
     * @param location The [Location] to check.
     * @return Whether the location is accessible
     */
    private fun isAccessible(location: Location): Boolean {
        if (location.row < 0 || location.row >= rows) return false
        if (location.col < 0 || location.col >= cols) return false
        if (grid[location.row][location.col] == ExpandedMapTile.PIPE) return false
        return true
    }

    /**
     * An updated function for finding neighbors in this new expanded grid.
     *
     * @param location The location to get neighbors for.
     * @return A list of accessible neighbor locations.
     */
    private fun getNeighborsOf(location: Location): List<Location> {
        val (row, col) = location
        val neighbors = mutableListOf<Location>()
        if (isAccessible(Location(row - 1, col))) neighbors.add(Location(row - 1, col))
        if (isAccessible(Location(row + 1, col))) neighbors.add(Location(row + 1, col))
        if (isAccessible(Location(row, col - 1))) neighbors.add(Location(row, col - 1))
        if (isAccessible(Location(row, col + 1))) neighbors.add(Location(row, col + 1))
        return neighbors
    }

    /**
     * Count the tiles enclosed by the loop of pipe.
     *
     * In part two, we identify the tiles that _aren't_ enclosed in
     * the loop by "flood-filling" from an identified outside tile. How
     * was this starting tile identified? Visual inspection of the input!
     *
     * @return The number of tiles enclosed.
     */
    fun countInnerTiles(): Int {
        // Flood fill to populate the list of seen locations. Perform a
        // breadth-first search starting from a known outside tile and
        // convert any tile found into an OUTSIDE tile.
        val queue = ArrayDeque(listOf(Location(0, 0)))
        val seenLocations: HashSet<Location> = HashSet<Location>()

        while (queue.isNotEmpty()) {
            val location = queue.removeLast()
            if (seenLocations.contains(location)) continue
            seenLocations.add(location)

            // Mark the found tile as an OUTER tile
            val (row, col) = location
            grid[row][col] = ExpandedMapTile.OUTER

            // Check each neighbor that hasn't been visited yet.
            for (neighbor in this.getNeighborsOf(location)) {
                if (seenLocations.contains(neighbor)) continue
                queue.addFirst(neighbor)
            }
        }

        // Return a count of all the EMPTY tiles. The total count is
        // divided by 9, because expanding a [PipeTile] to an [ExpandedMapTile]
        // increases the number of grid spaces by a factor of 9.
        return (grid.sumOf { row -> row.count { it == ExpandedMapTile.EMPTY } }) / 9
    }
}


class Day10(input: List<String>) {

    // It's my first builder pattern in Kotlin!
    private val parsed = PipeMapBuilder().readInputToGrid(input).identifyStartLocation().tracePipeLoop().toPipeMap()

    // In part one, we chase a furry critter through a loop of pipes.
    fun solvePart1(): Int = parsed.stepsToFurthestPointFromStart()

    // In part two we increase the resolution of the map to make it possible
    // to flag all the OUTER tiles, leaving the EMPTY tiles as the inner tiles.
    fun solvePart2(): Int = ExpandedPipeMap.fromPipeMap(parsed).countInnerTiles()
}
