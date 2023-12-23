package dev.ericburden.aoc2023

/**
 * This class represents one of the tiles in the hiking map
 *
 * I opted to try something a little different with my tiles this time. Instead
 * of having each type be an enum, I'm having each type of tile derive from
 * this [AbstractPathTile] with a set function for identifying indices around
 * it for possible next path steps.
 *
 * @property position The position of this tile in the grid.
 */
abstract class AbstractPathTile(val position: Utils.Index2D) {
    companion object {
        /**
         * Create a concrete instance of an [AbstractPathTile] represented by a character
         *
         * @param char The character that represents the kind of [AbstractPathTile].
         * @param position The position of this tile in the grid.
         * @return The [AbstractPathTile] represented by `char` at `position`.
         * @throws Exception When `char` doesn't represent an [AbstractPathTile].
         */
        fun fromChar(char: Char, position: Utils.Index2D) =
            when (char) {
                '.' -> OpenPath(position)
                '#' -> Impassable(position)
                '>' -> EastSlope(position)
                '<' -> WestSlope(position)
                '^' -> NorthSlope(position)
                'v' -> SouthSlope(position)
                else -> throw Exception("Cannot parse $char to a path tile!")
            }
    }

    // Concrete classes will use these to determine which neighboring 2D
    // indices are possible next destinations for taking a step.
    abstract val offsets: List<Utils.Offset2D>

    // Possible neighboring indices are comprised of the current position
    // plus each of this class's offsets.
    fun possibleNeighbors(): List<Utils.Index2D> = offsets.map { position + it }
}

// This class represents an open tile, from which a hiker can proceed in any
// of the four cardinal directions.
class OpenPath(position: Utils.Index2D) : AbstractPathTile(position) {
    override val offsets = listOf(
        Utils.Offset2D(-1, 0),
        Utils.Offset2D(1, 0),
        Utils.Offset2D(0, -1),
        Utils.Offset2D(0, 1)
    )
}

// This class represents an impassable tile. A hiker cannot proceed from
// such a tile.
class Impassable(position: Utils.Index2D) : AbstractPathTile(position) {
    override val offsets = listOf<Utils.Offset2D>()
}

// This class represents a slope to the east. A hiker can only proceed
// east from this tile.
class EastSlope(position: Utils.Index2D) : AbstractPathTile(position) {
    override val offsets = listOf(Utils.Offset2D(0, 1))
}

// This class represents a slope to the west. A hiker can only proceed
// west from this tile.
class WestSlope(position: Utils.Index2D) : AbstractPathTile(position) {
    override val offsets = listOf(Utils.Offset2D(0, -1))
}

// This class represents a slope to the north. A hiker can only proceed
// north from this tile.
class NorthSlope(position: Utils.Index2D) : AbstractPathTile(position) {
    override val offsets = listOf(Utils.Offset2D(-1, 0))
}

// This class represents a slope to the south. A hiker can only proceed
// south from this tile.
class SouthSlope(position: Utils.Index2D) : AbstractPathTile(position) {
    override val offsets = listOf(Utils.Offset2D(1, 0))
}

/**
 * This class represents the entire map of the hiking trail.
 *
 * @property grid The grid of tiles in this hiking map.
 */
data class HikingMap(val grid: List<List<AbstractPathTile>>) {
    companion object {
        /**
         * Parse the input into a hiking map
         *
         * @param input The grid of characters from the input file.
         * @return The [HikingMap] represented by the input file.
         * @throws Exception If any character in the input file can't be parsed
         * into an [AbstractPathTile].
         */
        fun fromInput(input: List<List<Char>>): HikingMap {
            val grid = input.withIndex().map { (rowIdx, row) ->
                row.withIndex().map { (colIdx, char) ->
                    val idx = Utils.Index2D(rowIdx, colIdx)
                    AbstractPathTile.fromChar(char, idx)
                }
            }
            return HikingMap(grid)
        }
    }

    // Rows and columns in the hiking map
    private val rows: Int get() = grid.size
    private val cols: Int get() = grid.first().size

    // The start and end tiles in this map.
    val start: OpenPath
        get() = (grid.first().find { it is OpenPath } as OpenPath?)!!
    val finish: OpenPath
        get() = (grid.last().find { it is OpenPath } as OpenPath?)!!

    /**
     * Attempt get the tile from the map at `idx`
     *
     * If the index is a valid index in the map *and* the tile is passable,
     * return the tile. Otherwise, return null.
     *
     * @param idx The index to attempt to fetch a tile from.
     * @return An [AbstractPathTile] if the index is valid, else null.
     */
    private fun getOrNull(idx: Utils.Index2D): AbstractPathTile? {
        val inBounds = idx.row in 0..<rows && idx.col in 0..<cols
        if (inBounds && grid[idx.row][idx.col] !is Impassable) {
            return grid[idx.row][idx.col]
        }
        return null
    }

    /**
     * Return the length of the longest path from start to finish.
     *
     * @return The length of the longest path through the map.
     */
    fun longestPathThrough(): Int {
        // This will be an exhaustive depth-first search. Each item in the stack
        // consists of a tile and the path taken to get to that tile. This path
        // is used to ensure that we don't make a loop while walking along the
        // trail.
        val stack =
            mutableListOf(start as AbstractPathTile to listOf(start.position))
        var maxPathLength = 0

        // So long as we have paths left to explore...
        while (stack.isNotEmpty()) {
            // Get the last tile and path. If we're at the finish line,
            // recalculate the maximum path length and move on to another path.
            val (tile, pathToTile) = stack.removeLast()
            if (tile == finish) {
                maxPathLength = maxOf(maxPathLength, pathToTile.size - 1)
                continue
            }

            // Add each reachable, neighboring tile (and the path to it) to
            // the stack.
            val neighbors = tile.possibleNeighbors().mapNotNull { getOrNull(it) }
            for (neighbor in neighbors) {
                if (neighbor.position in pathToTile) continue // No loops!
                stack.add(neighbor to pathToTile + neighbor.position)
            }
        }

        return maxPathLength
    }

    /**
     * Return a copy of this [HikingMap] with all the slopes removed
     *
     * @return A copy of this map with all the slopes removed.
     */
    fun dryUpTheTrails(): HikingMap {
        // Convert each slope to an [OpenPath].
        val grid = grid.map { row ->
            row.map { tile ->
                when (tile) {
                    is NorthSlope -> OpenPath(tile.position)
                    is WestSlope -> OpenPath(tile.position)
                    is EastSlope -> OpenPath(tile.position)
                    is SouthSlope -> OpenPath(tile.position)
                    else -> tile
                }
            }
        }
        return HikingMap(grid)
    }

    /**
     * From a given starting tile, find the next adjacent junctions
     *
     * A junction is any tile where the path branches or the start/finish tiles.
     * We're searching for these to try to cut down on the number of steps
     * needed to algorithmically walk all the paths when they're not restricted
     * by slopes.
     *
     * @param start The [AbstractPathTile] to start from.
     * @return A list of the next reachable junctions and the number of steps
     * to each.
     */
    private fun nextJunctions(start: AbstractPathTile): List<Pair<Int, AbstractPathTile>> {
        // This is another DFS with a twist, searching along pairs of
        // <step count> : <path tile>
        val stack = mutableListOf(0 to start)
        val seen = mutableSetOf<AbstractPathTile>()

        // This is the list of found junctions.
        val junctions = mutableListOf<Pair<Int, AbstractPathTile>>()

        // So long as there are tiles left to search.
        while (stack.isNotEmpty()) {
            // Get the next tile, and if we haven't already searched it...
            val (steps, tile) = stack.removeLast()
            if (tile in seen) continue
            seen.add(tile)

            // Find all the neighbors!
            val neighbors = tile.possibleNeighbors()
                .mapNotNull { getOrNull(it) }
                .filter { it !in seen }

            // If this tile is a junction (multiple neighbors) or is
            // the finish line, add it to the list of junctions and
            // move on.
            if (steps > 0 && (neighbors.size > 1 || tile == finish)) {
                junctions.add(steps to tile)
                continue
            }

            // Otherwise, add each neighboring tile and the number of steps
            // it took to reach it to the stack.
            for (neighbor in neighbors) {
                stack.add(steps + 1 to neighbor)
            }
        }

        // Return the list of junctions
        return junctions
    }

    /**
     * Convert this [HikingMap] into a [JunctionMap]
     *
     * A [JunctionMap] serves as a directed acyclic graph through the
     * [HikingMap]. This cuts down on the amount of work needed to search
     * through paths by simplifying the path from junction to junction.
     *
     * @return The [JunctionMap] derived from this [HikingMap].
     */
    fun toJunctionMap(): JunctionMap {
        // Yet another DFS with a twist! In this case, we're building up a
        // DAG of the junctions.
        val stack = mutableListOf(start as AbstractPathTile)
        val seen = mutableSetOf<AbstractPathTile>()
        val map =
            mutableMapOf<AbstractPathTile, List<Pair<Int, AbstractPathTile>>>()

        // For each junction tile still in the stack...
        while (stack.isNotEmpty()) {
            // If this junction hasn't been explored yet...
            val junction = stack.removeLast()
            if (junction in seen) continue
            seen.add(junction)

            // Get the next junctions! Add the mapping from the current
            // junction to the next junctions to our map.
            val nextJunctions = nextJunctions(junction)
            map[junction] = nextJunctions

            // Explore each of the next junctions.
            for ((_, nextJunction) in nextJunctions) {
                if (nextJunction in seen) continue // No loops!
                stack.add(nextJunction)
            }
        }

        return JunctionMap(map)
    }
}

/**
 * This class represents a map through path junctions.
 *
 * @property A mapping of tile -> [(steps, tile)].
 */
data class JunctionMap(val map: Map<AbstractPathTile, List<Pair<Int, AbstractPathTile>>>) {
    /**
     * Find the longest path through the [JunctionMap]
     *
     * @param start The starting tile.
     * @param finish The ending tile.
     * @return The number of steps in the longest path through the [JunctionMap].
     */
    fun longestPath(start: AbstractPathTile, finish: AbstractPathTile): Int {
        // That's right, DFS *again*!!!!
        val stack = mutableListOf(0 to (start to listOf(start)))
        var maxPathLength = 0

        // You know the drill...
        while (stack.isNotEmpty()) {
            // For each step not yet explored...
            val (steps, path) = stack.removeLast()
            val (tile, pathToTile) = path

            // If we've reached the end with this path, recalculate max path.
            if (tile == finish) {
                maxPathLength = maxOf(maxPathLength, steps)
                continue
            }

            // Explore the next junction along each path.
            for ((nextSteps, nextTile) in map[tile]!!) {
                if (nextTile in pathToTile) continue
                stack.add(steps + nextSteps to (nextTile to pathToTile + nextTile))
            }
        }

        return maxPathLength
    }
}


class Day23(input: List<List<Char>>) {

    // Make a map!
    private val parsed = HikingMap.fromInput(input)

    // Find the longest path through the hiking trail, navigating those
    // slippery slopes!
    fun solvePart1(): Int = parsed.longestPathThrough()

    // Slopes are no match for us! Finding the longest way around while
    // climbing up those slopes!
    fun solvePart2(): Int {
        val driedHikingMap = parsed.dryUpTheTrails()
        val junctionMap = driedHikingMap.toJunctionMap()
        return junctionMap.longestPath(
            driedHikingMap.start,
            driedHikingMap.finish
        )
    }
}
