package dev.ericburden.aoc2023

/**
 * This enum represents each possible shape on our platform
 *
 * @property rep The character representation of the enum variant
 */
enum class MapTile(private val rep: Char) {
    EMPTY('.'), SQUARE('#'), ROUND('O');

    companion object {
        /**
         * Parse a [MapTile] from a character
         *
         * @param char The character to parse.
         * @return The [MapTile] representation of `char`.
         */
        fun fromChar(char: Char): MapTile {
            return when (char) {
                '.' -> EMPTY
                '#' -> SQUARE
                'O' -> ROUND
                else -> throw Exception("$char does not represent a map tile!")
            }
        }
    }

    /**
     * Produce a string representation of this [MapTile]
     *
     * @return A string representing this [MapTile].
     */
    override fun toString(): String {
        return rep.toString()
    }
}

/**
 * Rotate a rectangular grid
 *
 * Transforms a rectangular grid (list of lists) as shown below:
 *
 *   [[1, 2, 3],            [[7, 4, 1],
 *    [4, 5, 6],     ->      [8, 5, 2],
 *    [7, 8, 9]]             [9, 6, 3]]
 *
 * @return A copy of the original grid, rotated one quarter turn clockwise.
 */
fun <T> List<List<T>>.rotateClockwise(): List<List<T>> {
    if (isEmpty() || first().isEmpty()) return this

    val numRows = this.size
    val numCols = first().size

    return List(numCols) { col ->
        List(numRows) { row ->
            this[numRows - 1 - row][col]
        }
    }
}

/**
 * This class represents the state of the large metal platform
 *
 * @property grid The configuration of rocks on the platform.
 */
data class Platform(val grid: List<List<MapTile>>) {
    companion object {
        /**
         * Parse a [Platform] from the input file
         *
         * @param input The character grid from the input file.
         * @return The [Platform] parsed from the input.
         */
        fun fromInput(input: List<List<Char>>): Platform {
            val rotatedInput = input.rotateClockwise()
            val grid = rotatedInput.map { row -> row.map(MapTile::fromChar) }
            return Platform(grid)
        }

        /**
         * Roll the round rocks to the end of the line
         *
         * This function accepts a line representing a row or column from
         * the platform and rolls all the round rocks from the start of the
         * line towards the end, as far as they will go.
         *
         * @param line A list of [MapTile]s representing a row or column on the
         * platform.
         * @return A copy of the input list with the round rocks rolled as far
         * as they will go from beginning to end.
         */
        private fun tiltToEnd(line: List<MapTile>): List<MapTile> {
            val output = line.toMutableList()
            var lastOpenIdx = output.size   // The index a rock can roll to

            // Walking backwards from the end of the line...
            for (currentIdx in output.lastIndex downTo 0) {
                when (output[currentIdx]) {
                    // If we're currently checking an empty tile and we haven't
                    // found an empty tile to roll a rock to yet...
                    MapTile.EMPTY -> {
                        if (lastOpenIdx == output.size) lastOpenIdx = currentIdx
                    }

                    // If we're checking a square rock, then the next possibly
                    // empty tile will be the tile before the square rock.
                    MapTile.SQUARE -> lastOpenIdx = currentIdx - 1

                    // If we're checking a round rock...
                    MapTile.ROUND -> {
                        if (lastOpenIdx == currentIdx) {
                            // ...and there's no open position to move it to,
                            // treat it like a square rock and set the next
                            // possibly open index to the tile before this rock.
                            lastOpenIdx -= 1
                        } else if (lastOpenIdx < output.size) {
                            // ...and there's an open position to move it to,
                            // move the round rock to that open position and
                            // mark the tile before the destination as a
                            // possibly open tile.
                            output[lastOpenIdx] = MapTile.ROUND
                            output[currentIdx] = MapTile.EMPTY
                            lastOpenIdx -= 1
                        }
                    }
                }
            }

            return output
        }

        /**
         * Calculate the load of a line of [MapTile]s
         *
         * The load of a line of [MapTile]s is the sum of the 1-indexed indices
         * occupied by round rocks. For example, a round rock in the fifth
         * position adds 5 to the load.
         *
         * @param line A list of [MapTile]s representing a row or column on the
         * platform.
         * @return The calculated load of the line.
         */
        private fun calculateLoad(line: List<MapTile>): Int =
            line.withIndex().filter { (_, tile) -> tile == MapTile.ROUND }
                .sumOf { (idx, _) -> idx + 1 }
    }

    /**
     * Returns a string representation of this [Platform]
     *
     * @return A string representation of this [Platform].
     */
    override fun toString(): String =
        grid.joinToString("\n") { row -> row.joinToString("") { it.toString() } }

    /**
     * Produce a copy of this [Platform] tilted towards the end
     *
     * The 'end' of a platform is defined by the individual rows in the `grid`.
     * The last index of the grid is the 'end', and rocks will roll that
     * direction. In order to roll the rocks North, the grid should be rotated
     * such that the last index in each row of the grid is the North end.
     *
     * @return A copy of this [Platform] with rocks rolled towards the end.
     */
    fun tiltToEnd(): Platform = grid.map(Platform::tiltToEnd).toPlatform()

    /**
     * Calculate the load of this [Platform]
     *
     * As with the tilt, load is calculated oriented towards the 'end' of
     * the [Platform], which is based on the orientation of the underlying grid.
     * To get the load on the North pillar, the North end of the grid must be
     * oriented towards the end.
     *
     * @return The load on the end of this [Platform].
     */
    fun calculateLoad(): Int = grid.sumOf(Platform::calculateLoad)

    /**
     * Conduct a full cycle of tilting and rotating the [Platform]
     *
     * Tilts the platform, rolling all rocks towards the end, then rotates
     * the platform clockwise one quarter turn. Repeat for all four cardinal
     * directions, until the returned [Platform] is oriented in the same
     * direction as this one was to begin with.
     *
     * @return A copy of this [Platform] tilted and rotated through a full cycle.
     */
    fun tiltFullCycle(): Platform {
        var grid = grid

        repeat(4) {
            grid = grid.map(Platform::tiltToEnd)
            grid = grid.rotateClockwise()
        }

        return Platform(grid)
    }
}

/**
 * Converts a grid of [MapTile]s to a [Platform]
 *
 * This function extends the functionality of a [List<List<MapTile>>], adding
 * a `toPlatform` function that converts the grid of [MapTile]s into a
 * [Platform].
 *
 * @return The converted [Platform].
 */
fun List<List<MapTile>>.toPlatform(): Platform = Platform(this)

class Day14(input: List<List<Char>>) {

    // Today's input is a grid of characters that needs to be treated like a
    // big metal platform with rolling and stationary rocks.
    private val parsed = Platform.fromInput(input)

    // Tilt the platform to the north and calculate the load on the northern
    // pillar.
    fun solvePart1(): Int = parsed.tiltToEnd().calculateLoad()

    // There's no way we're actually supposed to do a billion cycles. There
    // must be a better way!
    fun solvePart2(): Int {
        var cyclesRemaining = 1_000_000_000
        var platform = parsed

        // First, check how many cycles it takes to see a platform
        // configuration appear twice. This indicates a repeating sequence!
        val seenStates = mutableSetOf<Platform>()
        while (!seenStates.contains(platform)) {
            seenStates.add(platform)
            platform = platform.tiltFullCycle()
            cyclesRemaining -= 1
        }

        // Now, we know where the repeating sequence starts, we need to
        // determine how long the sequence is. Reset our seen states and
        // keep cycling the platform until we finish another repeating
        // sequence.
        val repeatingSequenceStart = cyclesRemaining
        seenStates.clear()
        while (!seenStates.contains(platform)) {
            seenStates.add(platform)
            platform = platform.tiltFullCycle()
            cyclesRemaining -= 1
        }

        // Armed with the knowledge of how many states are in the
        // repeating sequence...
        val repeatingSequenceLength = repeatingSequenceStart - cyclesRemaining

        // ... we can make a list of the loads of each of those states. Now
        // we know all the possible load values that can appear from here on
        // out. But which one? Well...
        val repeatingSequenceLoads = mutableListOf<Int>()
        repeat(repeatingSequenceLength) {
            platform = platform.tiltFullCycle()
            repeatingSequenceLoads.add(platform.calculateLoad())
            cyclesRemaining -= 1
        }

        // Assume the sequence repeats over and over. Eventually, we'll run out
        // of cycles on the platform. So, the number of cycles left after the
        // last full repeating sequence tells us which of the load values
        // to use.
        val cyclesRemainingAfterLastSequence =
            cyclesRemaining % repeatingSequenceLength
        return repeatingSequenceLoads[cyclesRemainingAfterLastSequence - 1]
    }
}
