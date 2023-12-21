package dev.ericburden.aoc2023

/**
 * This enum represents one tile in the map of the garden
 */
enum class GardenMapTile {
    OPEN, ROCK, START;

    companion object {
        /**
         * Parse a [GardenMapTile] from a character
         *
         * @param char The character representing a [GardenMapTile].
         * @return A [GardenMapTile] represented by the input character.
         * @throws Exception When the character does not represent a [GardenMapTile].
         */
        fun fromChar(char: Char): GardenMapTile = when (char) {
            '.' -> OPEN
            '#' -> ROCK
            'S' -> START
            else -> throw Exception("Cannot parse $char to a [GardenMapTile]!")
        }
    }
}

/**
 * This class represents the map of the garden the elf wants to walk in
 *
 * @property grid The grid of tiles that comprise this garden.
 * @property start The elf's starting location for their walkabout.
 */
open class GardenMap(
    val grid: List<List<GardenMapTile>>,
    val start: Utils.Index2D
) {
    companion object {
        /**
         * Parse a [GardenMap] from the input
         *
         * @param input The grid of characters from the input file.
         * @return The [GardenMap] represented by the input file.
         * @throws Exception When the input cannot be parsed.
         */
        fun fromInput(input: List<List<Char>>): GardenMap {
            val grid = input.map { row -> row.map(GardenMapTile::fromChar) }

            // Search the grid for the first row/col index containing a
            // START tile.
            val start = grid.withIndex().flatMap { (rowIdx, row) ->
                row.withIndex().map { (colIdx, value) ->
                    Utils.Index2D(rowIdx, colIdx) to value
                }
            }.find { (_, tile) -> tile == GardenMapTile.START }?.first
                ?: throw Exception("Could not find starting tile!")

            return GardenMap(grid, start)
        }
    }

    // For pretty printing!
    override fun toString(): String = grid.joinToString("\n") { row ->
        row.joinToString("") { tile ->
            when (tile) {
                GardenMapTile.ROCK -> "#"
                GardenMapTile.OPEN -> "."
                GardenMapTile.START -> "S"
            }
        }
    }

    val rows: Int get() = grid.size
    val cols: Int get() = grid.first().size
    val offsets = listOf(
        Utils.Offset2D(-1, 0),
        Utils.Offset2D(1, 0),
        Utils.Offset2D(0, -1),
        Utils.Offset2D(0, 1)
    )

    /**
     * Checks an index to ensure it is in the bounds of the map
     *
     * @param idx The index to check.
     * @return A boolean indicating whether the index is in bounds.
     */
    private fun inBounds(idx: Utils.Index2D): Boolean {
        val (row, col) = idx
        return row in 0..<rows && col in 0..<cols
    }

    // Indexing for the GardenMap
    open operator fun get(idx: Utils.Index2D) = grid[idx.row][idx.col]

    /**
     * Return the reachable neighboring indices of the given index
     *
     * @param idx The index to check for neighbors.
     * @return A list of neighboring indices that are reachable from `idx`. A
     * reachable idx is one that is in the bounds of the map and doesn't contain
     * a rock.
     */
    open fun neighborsOf(idx: Utils.Index2D) = offsets.map { idx + it }
        .filter { inBounds(it) && get(it) != GardenMapTile.ROCK }

    /**
     * Return the number of tiles that can be reached in a certain number of steps
     *
     * @param stepsAllowed The number of steps the elf will take.
     * @return The number of tiles that can be reached in exactly `stepsAllowed`
     * steps.
     */
    fun reachableTiles(stepsAllowed: Int): Int {

        // Mostly just a breadth-first search through the garden spaces.
        val queue = ArrayDeque(listOf(0 to start))
        val seen = mutableSetOf<Utils.Index2D>()

        // With the twist that we're keeping track of every tile we reach
        // and the number of steps it took to reach it.
        val visited = mutableListOf<Pair<Int, Utils.Index2D>>()

        while (queue.isNotEmpty()) {
            val (steps, tileIdx) = queue.removeLast()
            if (steps > stepsAllowed || seen.contains(tileIdx)) continue
            visited.add(steps to tileIdx)
            seen.add(tileIdx)

            for (neighborIdx in neighborsOf(tileIdx)) {
                if (seen.contains(neighborIdx)) continue
                queue.addFirst((steps + 1) to neighborIdx)
            }
        }

        // The tiles that are reachable in exactly `stepsAllowed` steps are
        // the tiles that we reach at the same parity as `stepsAllowed`, i.e.,
        // when `stepsAllowed` is even, the tiles reachable in an even number
        // of steps will be included. When `stepsAllowed` is odd, the tiles
        // reachable in an odd number of steps will be included. For our
        // purposes, 0 counts as an even number (so that the start will be
        // included for even numbers of steps).
        return visited.count { (idx, _) -> idx % 2 == stepsAllowed % 2 }
    }
}

/**
 * This class represents the repeating garden map
 *
 * In part two, the map goes infinite! This subclass of [GardenMap] updates
 * some methods so that it can be treated as if it repeats infinitely in all
 * four directions.
 *
 * @property grid The grid of tiles that comprise this garden.
 * @property start The elf's starting location for their walkabout.
 */
class GardenMapInfinite(
    grid: List<List<GardenMapTile>>,
    start: Utils.Index2D
) : GardenMap(grid, start) {
    companion object {
        fun fromGardenMap(gardenMap: GardenMap): GardenMapInfinite {
            return GardenMapInfinite(gardenMap.grid, gardenMap.start)
        }
    }

    /**
     * Access an index in an infinite, repeating map
     *
     * To treat the _actually_ finite grid as an infinite map for indexing,
     * we need to wrap the indexes around the width and height of the grid.
     *
     * @param idx The index to fetch.
     * @return The map tile at that index.
     */
    override operator fun get(idx: Utils.Index2D): GardenMapTile {
        // Some fun with modulo to wrap the indices in both directions
        val rowIdx = (idx.row % rows + rows) % rows
        val colIdx = (idx.col % cols + cols) % cols
        return grid[rowIdx][colIdx]
    }

    /**
     * Get the neighbors of an index in an infinite garden
     *
     * The only change here is to remove the bounds-checking, because
     * an infinite garden is *boundless*!
     *
     * @param idx The index to check for neighbors.
     * @return A list of neighboring indices that are reachable from `idx`. A
     * reachable idx is one that doesn't contain a rock.
     */
    override fun neighborsOf(idx: Utils.Index2D) = offsets.map { idx + it }
        .filter { get(it) != GardenMapTile.ROCK }
}

class Day21(input: List<List<Char>>) {

    // We're old hands at parsing a grid of characters into a map of some start.
    private val parsed = GardenMap.fromInput(input)

    // Walk the elf through the garden his desired number of steps and return
    // a count of all the tiles that can be landed on in exactly `stepsAllowed`
    // number of steps.
    fun solvePart1(stepsAllowed: Int): Int = parsed.reachableTiles(stepsAllowed)

    // Ugh, this is some funky (to me, at least) math.
    fun solvePart2(stepsAllowed: Int): Long {
        // Start by converting the garden map to a theoretically infinite tiling
        // of the original grid map.
        val map = GardenMapInfinite.fromGardenMap(parsed)

        /*
         And this is where it starts to get a bit...wonky. We can observe from
         the input that the number of steps the elf wants to take is a multiple
         of the grid size + (grid size // 2). The grid is 131 tiles square, so
         we can see that 26501365 = n * 131 + 65, with n = 202,300. We also
         note that, in the real input, there is a clear path from the start to
         each edge and each edge is clear all the way around. Additionally, the
         input forms a _diamond_ shape with a clear lane all the way around the
         manhattan distance of (grid size / 2) from the start. So, this means
         our zoomed out grid looks like:

                2....2
                ..11..
                ..11..
                2....2

        where the `1`'s represent the inner section of obstacles and the `2`'s
        represent the outer section of obstacles, which tiles to:

                2....22....22....22....22....2
                ..11....11....11....11....11..
                ..11....11....11....11....11..
                2....22....22....22....22....2
                2....22....22....22....22....2
                ..11....11....11....11....11..
                ..11....11....11....11....11..
                2....22....22....22....22....2
                2....22....22....22....22....2
                ..11....11....11....11....11..
                ..11....11....11....11....11..
                2....22....22....22....22....2
                2....22....22....22....22....2
                ..11....11....11....11....11..
                ..11....11....11....11....11..
                2....22....22....22....22....2
                2....22....22....22....22....2
                ..11....11....11....11....11..
                ..11....11....11....11....11..
                2....22....22....22....22....2

        So, without assuming that the `1` set of rocks and the `2` set of rocks
        are identical, we have alternating blocks of obstacles that form a
        repeating ring-like pattern around the center. We can then proceed on
        the assumption that these rings form the basis of a pattern in our
        output, like:

            - At (grid size / 2) steps, the elf's walking range encompasses the
              `1` set of obstacles in the center.
            - At (grid size / 2) + (grid size) steps, the elf's range now
              encompasses the entire first ring of obstacles, including 5 groups
              of `1`s and 4 groups of `2`s.
            - At (grid size / 2) + (grid size * 2), the elf's range now
              encompasses the first two rings of obstacles, including 13 groups
              of `1`'s and 12 groups of `2`'s.

        Because the number of obstacle groups included is scaling in some
        predictable fashion with an increase in number of steps by (grid size),
        we are able to manually find the first few results for the function
        steps -> reachable tiles and extrapolate the answer from there.
        */

        // Taking the first four results of steps -> reachable tiles for
        // step sizes at (grid size // 2), (grid size // 2) + (grid size),
        // (grid size // 2) + (grid size * 2), (grid size // 2) + (grid size * 3)...
        val stepIncrements = (0..3).map { incr ->
            (map.grid.size / 2) + (map.grid.size * incr)
        }
        val reachableTileCounts = stepIncrements.map { map.reachableTiles(it).toLong() }
            .toMutableList()

        /*
        For my input, I get [3755, 33494, 92811, 181706] for these first four
        counts of reachable tiles. The increase isn't linear, however, if we
        repeatedly take the differences between the values in sequence...

            [3755, 33494, 92811, 181706]
              [29739, 59317, 88895]
                  [29578, 29578]
                        [0]

        It's Day 9! Or, rather, it's a polynomial sequence. There's definitely
        math that can be done here to derive a formula for predicting the next
        values based on this, but for my own sanity, I'm going to use the same
        method from Day 9.
         */

        // Start by generating the lists of differences shown above.
        val diffs = mutableListOf(reachableTileCounts)
        while (diffs.last().last() > 0) {
            val diffDiffs =
                diffs.last().windowed(2).map { (prev, next) -> next - prev }
                    .toMutableList()
            diffs.add(diffDiffs)
        }
        diffs.reverse()

        // Calculate the number of times we need to predict the next value as
        // the number of times we can increase the number of steps by the
        // grid size, up to the desired number of steps, starting at
        // (grid size // 2), aka: 26501365 = n * 131 + 65, with n = 202,300.
        val numStepCycles = (stepsAllowed - (map.grid.size / 2)) / map.grid.size

        // Now, we use the same procedure from Day 9 to predict the next value
        // in our sequence `numStepCycles` times, and return the last predicted
        // value. Does this count as dynamic programming, I wonder?
        while (diffs.last().size <= numStepCycles) {
            for ((prev, next) in diffs.windowed(2)) {
                next.add(prev.last() + next.last())
            }
        }

        return diffs.last().last()
    }
}
