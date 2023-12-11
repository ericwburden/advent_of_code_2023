package dev.ericburden.aoc2023

import kotlin.math.abs

/**
 * This class represents a position on our map of galaxies.
 *
 * @property row The row of this position.
 * @property col The col of this position.
 */
data class Position(val row: Int, val col: Int) {
    /**
     * Calculate the Manhattan distance between two Positions
     *
     * @param other The other position to calculate distance to.
     * @return The Manhattan distance to the `other` position.
     */
    fun manhattanDistanceTo(other: Position): Int {
        return abs(row - other.row) + abs(col - other.col)
    }
}

/**
 * This class represents the map of galaxies from the input
 *
 * @property galaxies A list of [Position]s of galaxies on the map.
 * @property expandedRows A list of the row indices in the map with no galaxies.
 * @property expandedCols A list of the column indices in the map with no galaxies.
 */
data class GalaxyMap(
    val galaxies: List<Position>,
    val expandedRows: List<Int>,
    val expandedCols: List<Int>
) {
    companion object {
        /**
         * Parse the input file into a [GalaxyMap]
         *
         * @param input The grid of characters from the input file.
         * @return A [GalaxyMap] parsed from the input.
         */
        fun fromInput(input: List<List<Char>>): GalaxyMap {
            // Every '#' is a galaxy. Everything else is just empty space.
            val galaxies = input.withIndex()
                .flatMap { (rowIdx, row) ->
                    row.withIndex().map { (colIdx, char) ->
                        char to Position(
                            rowIdx,
                            colIdx
                        )
                    }
                }
                .filter { (char, _) -> char == '#' }
                .map { (_, position) -> position }

            // It seemed most straightforward to start with a set of all row
            // and column indices, then remove any row or column with a galaxy
            // on it.
            val expandedRows = input.indices.toMutableSet()
            val expandedCols = input.first().indices.toMutableSet()
            galaxies.forEach { position ->
                expandedRows.remove(position.row)
                expandedCols.remove(position.col)
            }

            // I'm sorting the expanded rows and columns lists here so that I
            // can binary search them later for _max velocity_.
            return GalaxyMap(
                galaxies,
                expandedRows.toList().sorted(),
                expandedCols.toList().sorted()
            )
        }
    }

    /**
     * Count the number of expanded rows between two galaxies
     *
     * Because the expanded rows are sorted, we can binary search to determine
     * how many expanded rows are between two rows that we know aren't in the
     * list (because they have galaxies on them).
     *
     * @param galaxyA The first galaxy in the pair.
     * @param galaxyB the other galaxy in the pair.
     * @return The number of expanded rows between the pair of galaxies.
     */
    private fun expandedRowsBetween(galaxyA: Position, galaxyB: Position): Int =
        abs(
            expandedRows.binarySearch(galaxyA.row) - expandedRows.binarySearch(
                galaxyB.row
            )
        )

    /**
     * Count the number of expanded columns between two galaxies
     *
     * Because the expanded columns are sorted, we can binary search to determine
     * how many expanded columns are between two columns that we know aren't in
     * the list (because they have galaxies on them).
     *
     * @param galaxyA The first galaxy in the pair.
     * @param galaxyB the other galaxy in the pair.
     * @return The number of expanded columns between the pair of galaxies.
     */
    private fun expandedColsBetween(galaxyA: Position, galaxyB: Position): Int =
        abs(
            expandedCols.binarySearch(galaxyA.col) - expandedCols.binarySearch(
                galaxyB.col
            )
        )

    /**
     * Calculate the real distance between two galaxies
     *
     * The distance between two galaxies consists of their Manhattan distance
     * on the map, plus the expansion of the universe that occurred during
     * the time it took the light to travel to our observation point. This
     * is accomplished by doubling the "width" of rows and columns without
     * any galaxies on them.
     *
     * @param galaxyA The first galaxy.
     * @param galaxyB The second galaxy.
     * @return The distance between the two galaxies.
     */
    fun distanceBetween(galaxyA: Position, galaxyB: Position): Int {
        val observedDistance = galaxyA.manhattanDistanceTo(galaxyB)
        val rowExpansions = expandedRowsBetween(galaxyA, galaxyB)
        val colExpansions = expandedColsBetween(galaxyA, galaxyB)

        // Because each expanded row/columns counts as 2, and because we already
        // accounted for each of them once in the `observedDistance`, we just
        // add the "extra width" of 1 for each expanded row/column to our final
        // distance.
        return observedDistance + rowExpansions + colExpansions
    }

    /**
     * Calculate the real distance between two galaxies with variable expansion
     *
     * The distance between two galaxies consists of their Manhattan distance
     * on the map, plus the expansion of the universe that occurred during
     * the time it took the light to travel to our observation point. For part
     * two, the examples are for smaller levels of expansion than the expected
     * answer, so this function accepts the multiple by which the empty rows
     * and columns have expanded as a parameter.
     *
     * @param galaxyA The first galaxy.
     * @param galaxyB The second galaxy.
     * @param expansionFactor The multiple by which the empty rows/columns have
     * expanded.
     * @return The distance between the two galaxies.
     */
    fun enhancedDistanceBetween(
        galaxyA: Position,
        galaxyB: Position,
        expansionFactor: Int
    ): Long {
        val observedDistance = galaxyA.manhattanDistanceTo(galaxyB)

        // We multiply the number of rows/columns by the expansionFactor - 1
        // because, once again, the one-wide row/column is already accounted
        // for in the `observedDistance`.
        val rowExpansions =
            expandedRowsBetween(galaxyA, galaxyB) * (expansionFactor - 1)
        val colExpansions =
            expandedColsBetween(galaxyA, galaxyB) * (expansionFactor - 1)
        return observedDistance.toLong() + rowExpansions.toLong() + colExpansions.toLong()
    }
}

/**
 * Returns a sequence of non-repeating pairs of elements in a list
 *
 * This extension function defines a sequence of pairs of elements from the
 * original list such that all unique pairs are generated. For this purpose,
 * the pairs (A, B) and (B, A) are considered equivalent.
 *
 * @return A sequence of non-repeating pairs of elements in [List<T>].
 */
fun <T> List<T>.pairs(): Sequence<Pair<T, T>> =
    sequence {
        for (i in indices) {
            for (j in i + 1 until size) {
                yield(this@pairs[i] to this@pairs[j])
            }
        }
    }


class Day11(input: List<List<Char>>) {

    // Let's map the galaxy!
    private val parsed = GalaxyMap.fromInput(input)

    // In part one, the galaxy has a static expansion. Since the "shortest path"
    // through empty space is just the distance, calculate the distance between
    // all unique pairs of galaxies and return the sum.
    fun solvePart1(): Int =
        parsed.galaxies.pairs().sumOf { (galaxyA, galaxyB) ->
            parsed.distanceBetween(galaxyA, galaxyB)
        }

    // In part two, we need to accommodate a variable expansion factor if we want
    // to use the same function for testing the example and real inputs (we do
    // want to do that, by the way).
    fun solvePart2(expansionFactor: Int): Long =
        parsed.galaxies.pairs().sumOf { (galaxyA, galaxyB) ->
            parsed.enhancedDistanceBetween(galaxyA, galaxyB, expansionFactor)
        }
}
