package dev.ericburden.aoc2023

// My first imports!
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sqrt

/**
 * Class that represents one of the current distance records
 *
 * Each race can be represented as the time limit and the last record distance.
 *
 * @property time The time available to complete the race.
 * @property distance The last record distance (we need to beat).
 */
data class RaceRecord(val time: Double, val distance: Double) {
    companion object {
        /**
         * Parse the input from a string representing the input file
         *
         * Today's input is only two lines and they both mean different things, so we'll parse the
         * input as a single string, convert each line to a list of numbers, and zip them together
         * to make our [RaceRecord]s
         */
        fun parseInput(input: String): List<RaceRecord> {
            val toDoubleOrThrow: (String) -> Double = { n ->
                n.toDoubleOrNull()
                        ?: throw IllegalArgumentException("$n cannot be parsed to a Double!")
            }

            val (raceTimes, raceDistances) =
                    input.trim().split("\\n".toRegex()).map { line ->
                        line.replaceFirst("\\w+:".toRegex(), "")
                                .trim()
                                .split("\\s+".toRegex())
                                .map(toDoubleOrThrow)
                    }

            return raceTimes.zip(raceDistances) { time, distance -> RaceRecord(time, distance) }
        }
    }

    /**
     * Return the number of different ways we can win the race!
     *
     * @ return An Int indicating just how many different hold times will get us the winning
     * distance.
     */
    fun countWinningStrategies(): Int {
        // For: Hold Time -> h; Race Time -> t; Distance to Beat -> d
        // We can derive a formula for beating the previous record as:
        //   - (t - h) * h > d
        //
        // Which rearranges to: (-1)*h^2 + (t * h) - d > 0
        // Now, that there is a quadratic expression! You know, the old
        // `ax^2 + bx + c = 0`? We can solve it like:
        //     h = (-t +/- sqrt(t^2 - 4 * (-1) * (-d))) / 2 * (-1)
        //
        // That's math! The 'tricky' part is that we don't want to know the _exact_
        // hold time to _equal_ the previous record, we want to know the smallest
        // and largest hold times that will _beat_ the record. For that, we round
        // _away_ from the mean possible hold time and then adjust our value towards
        // the mean by 1. For example, the results of this formula for the first
        // example race (7ms, 9mm) indicate that you could travel the 9mm by holding
        // the button for 1.697ms or 5.303ms. The minimum number of whole milliseconds,
        // then is `floor(1.697) + 1` and the maximum is `ceil(5.303) - 1`. This
        // correctly handles cases where the exact time is an integer as well.
        val sqrtFormulaPart = sqrt((time * time) - (4 * distance))
        val minWinningHold = floor((-time + sqrtFormulaPart) / -2).toInt() + 1
        val maxWinningHold = ceil((-time - sqrtFormulaPart) / -2).toInt() - 1

        // The total number of winning holds is the length of the inclusive range
        // of all possible winning holds.
        return (maxWinningHold - minWinningHold) + 1
    }
}

class Day06(input: String) {

    // One string to one list. Piece of cake.
    private val parsed = RaceRecord.parseInput(input)

    // In part 1, we calculate all our winning hold times and return
    // the product of that count from each race.
    fun solvePart1(): Int = parsed.map { it.countWinningStrategies() }.reduce { acc, n -> acc * n }

    // In part 2, we learn what 'kerning' is and get mad about it. Then
    // we concatenate all the race times/records into one and figure how
    // how many ways we can win that one race.
    fun solvePart2(): Int {
        val uberRaceTime = parsed.joinToString("") { it.time.toInt().toString() }.toDouble()
        val uberDistance = parsed.joinToString("") { it.distance.toInt().toString() }.toDouble()
        return RaceRecord(uberRaceTime, uberDistance).countWinningStrategies()
    }
}
