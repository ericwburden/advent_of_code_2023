package dev.ericburden.aoc2023

enum class MirrorDetectionStrategy { ALL_MIRRORED, ONE_SMUDGE }

/**
 * Transposes a rectangular matrix
 *
 * This function extends the functionality of a [List<List<T>>] by adding a
 * `transpose` function which returns a transposed copy of the original
 * list of lists. All the sub-lists must be the same length. For example:
 *
 *  [[1, 2, 3],             [[1, 4, 7],
 *   [4, 5, 6],     ->       [2, 5, 8],
 *   [7, 8, 9]]              [3, 6, 9]]
 *
 *  @return The transposed list of lists.
 */
fun <T> List<List<T>>.transpose(): List<List<T>> {
    if (isEmpty() || this.any { it.size != this[0].size }) {
        throw IllegalArgumentException("Inner lists must have the same size")
    }

    return List(this[0].size) { col ->
        List(size) { row ->
            this[row][col]
        }
    }
}

/**
 * Converts a String to an Integer by interpreting it as a binary value
 *
 * This function extends the functionality of [String] by adding a `toBinary`
 * function that interprets the characters in the string as 1's and 0's then
 * converts it to the [Int] represented by the binary string.
 *
 * @param one The character to interpret as 1.
 * @param zero The character to interpret as 0.
 * @return The String as binary and converted to an Int.
 */
fun String.toBinary(one: Char = '#', zero: Char = '.'): Int =
    toList().joinToString("") { char ->
        when (char) {
            one -> "1"
            zero -> "0"
            else -> {
                throw Exception("$char has not be specified as either '1' or '0'!")
            }
        }
    }.toInt(radix = 2)

infix fun Int.bitdiff(other: Int): Int {
    return Integer.bitCount(this xor other)
}

/**
 * The class represents one of the patterns of the landscape with a mirror
 *
 * @property rows Integer representations of the rows in the pattern.
 * @property cols Integer representations of the columns in the pattern.
 */
data class LandscapePattern(val rows: List<Int>, val cols: List<Int>) {
    companion object {
        /**
         * Parses a [LandscapePattern] from a chunk of the input file
         *
         * @param input A chunk of the input file.
         * @return A [LandscapePattern].
         */
        fun fromInputChunk(input: List<String>): LandscapePattern {
            // Each row and column is represented as an integer where the binary
            // of the integer corresponds to the input string (either row-wise
            // or column-wise), with '#' -> 1 and '.' -> 0. This honestly
            // was as much to make debugging easier as for efficiency.
            val rows = input.map { it.toBinary() }
            val cols = input.map { it.toList() }.transpose()
                .map { it.joinToString("").toBinary() }

            return LandscapePattern(rows, cols)
        }

        /**
         * Checks for a line of symmetry in a list of lines
         *
         * Each `line` is a row or column from one of the inputs. This function
         * checks `checkIdx` to determine whether it represents the left (or
         * top) row (or column) in a line of symmetry, meaning the lines on one
         * side of the line are a mirror image of the other side, all the way
         * to the edge of the input chunk.
         *
         * @param lines A list of rows or columns (as integers).
         * @param checkIdx The index in the list to check for symmetry.
         * @return A flag indicating whether the indicated index sits to
         * the left (or top) of a line of symmetry.
         */
        private fun hasLineOfSymmetry(
            lines: List<Int>,
            checkIdx: Int
        ): Boolean {
            require(0 <= checkIdx && checkIdx < lines.size) {
                throw Exception("There is no row $checkIdx in ${lines}!")
            }

            // Last line can't be a line of symmetry
            if (checkIdx == lines.size - 1) return false

            // If there is a line of symmetry, the given index will be to the
            // left of it in the list of lines.
            var leftIdx = checkIdx
            var rightIdx = checkIdx + 1

            // So long as the two lines being checked are the same, we expand
            // the mirrored region until one side of it reaches past the
            // beginning or end of the list of lines.
            while (lines[leftIdx] == lines[rightIdx]) {
                leftIdx--; rightIdx++ // Step the indices out by one
                if (leftIdx < 0 || rightIdx >= lines.size) return true
            }

            // If we reach a point where the two lines being checked aren't
            // equal, and we're still inside the list of lines, then we know
            // this index isn't where the line of symmetry is.
            return false
        }

        /**
         * Checks for a line of symmetry, accounting for one smudge
         *
         * The difference between this function and `hasLineOfSymmetry` is that
         * this function allows one, and only one, pair of lines to differ by
         * one bit.
         *
         * @param lines A list of rows or columns (as integers).
         * @param checkIdx The index in the list to check for symmetry.
         * @return A flag indicating whether the indicated index sits to
         * the left (or top) of a line of symmetry.
         */
        private fun hasLineOfSymmetryWithSmudge(
            lines: List<Int>,
            checkIdx: Int
        ): Boolean {
            require(0 <= checkIdx && checkIdx < lines.size) {
                throw Exception("There is no row $checkIdx in ${lines}!")
            }
            if (checkIdx == lines.size - 1) return false

            var leftIdx = checkIdx
            var rightIdx = checkIdx + 1

            // Need to keep track of whether we've already cleaned a smudge
            var cleanedSmudge = false

            // The `bitdiff` result is the number of bits different between the
            // two lines being examined. A value of zero would mean the two are
            // the same and a value of one is an acceptable smudge.
            // As long as the two lines being checked have zero or one smudges...
            while (lines[leftIdx] bitdiff lines[rightIdx] <= 1) {
                // This pair contains a smudge
                if (lines[leftIdx] bitdiff lines[rightIdx] == 1) {
                    // If we've already cleaned a smudge, there's no line of
                    // symmetry here.
                    if (cleanedSmudge) break

                    // Note the cleaned smudge. This can't happen again.
                    cleanedSmudge = true
                }
                leftIdx--; rightIdx++  // Step the indices out by one

                // If we reach either end of the lines, return whether or
                // not we cleaned one smudge. If no smudges were cleaned,
                // there is no line of symmetry.
                if (leftIdx < 0 || rightIdx >= lines.size) return cleanedSmudge
            }
            return false
        }
    }

    /**
     * Summarize this [LandscapePattern] for my notes
     *
     * A pattern is summarized by finding the line of symmetry, counting the
     * number of rows to the left (or columns above) and returning that number
     * (* 100 for rows).
     *
     * @param strategy The strategy to use when determining whether or not a
     * particular row/column is adjacent to a mirror.
     * @return The summary value for this [LandscapePattern].
     */
    fun summarize(strategy: MirrorDetectionStrategy): Int {
        // Search for a horizontal line of symmetry, returning the
        // number of lines above it * 100 if found.
        for (idx in rows.indices) {
            val adjacentToMirror = when (strategy) {
                MirrorDetectionStrategy.ALL_MIRRORED -> hasLineOfSymmetry(
                    rows,
                    idx
                )

                MirrorDetectionStrategy.ONE_SMUDGE -> hasLineOfSymmetryWithSmudge(
                    rows,
                    idx
                )
            }
            if (adjacentToMirror) return (idx + 1) * 100
        }

        // Search for a vertical line of symmetry, returning the number of
        // lines to the left of it if found.
        for (idx in cols.indices) {
            val adjacentToMirror = when (strategy) {
                MirrorDetectionStrategy.ALL_MIRRORED -> hasLineOfSymmetry(
                    cols,
                    idx
                )

                MirrorDetectionStrategy.ONE_SMUDGE -> hasLineOfSymmetryWithSmudge(
                    cols,
                    idx
                )
            }
            if (adjacentToMirror) return idx + 1
        }

        // Pitch a fit if no mirror is found.
        throw Exception("Could not find a mirror for $this!")
    }
}

class Day13(input: List<List<String>>) {

    // Parse each grid in the input file into a [LandscapePattern]
    private val parsed =
        input.filter { it.isNotEmpty() }.map(LandscapePattern::fromInputChunk)

    // For each [LandscapePattern] find the mirror, calculate the summary
    // value, and return the sum. The ALL_MIRRORED pattern means that
    // mirrors are detected by having a perfect reflection across a line of
    // symmetry all the way to one edge of the grid.
    fun solvePart1(): Int =
        parsed.sumOf { it.summarize(MirrorDetectionStrategy.ALL_MIRRORED) }

    // For each [LandscapePattern] find the mirror, calculate the summary
    // value, and return the sum  The ONE_SMUDGE pattern means that
    // mirrors are detected by having a reflection _with exactly one incorrect
    // value_ (a smudge) across a line of symmetry all the way to one edge of
    // the grid.
    fun solvePart2(): Int =
        parsed.sumOf { it.summarize(MirrorDetectionStrategy.ONE_SMUDGE) }
}
