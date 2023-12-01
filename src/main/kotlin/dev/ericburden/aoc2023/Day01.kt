package dev.ericburden.aoc2023

/**
 * Extracts all digit characters from a string as numbers.
 *
 * @return A list of all digits in the input String as Ints.
 */
private fun String.extractDigitsPart1(): List<Int> {
    return this.toList().filter(Char::isDigit).map(Char::digitToInt)
}

/**
 * Extract all digits from a string as numbers
 *
 * This function also extracts digits in "word" form, such as "one" or "two". Note, this function
 * will also extract number-words whose letters overlap, such as "twoone" or "fiveight".
 *
 * @return A list of all digits in the input String as Ints.
 */
private fun String.extractDigitsPart2(): List<Int> {
    val pattern = """(?=(one|two|three|four|five|six|seven|eight|nine|\d))"""

    // Because we have to account for overlaps, I'm using the "non-consuming" regular
    // expression above. This does weird things (IMO) to the match values, causing
    // `findAll()` to return a list of values for each match where the first value
    // is an empty string. Probably just means I need to learn more about regex.
    // Regardless, the result is that I need to flatten the results and remove empty
    // strings before converting the matches to numbers.
    return Regex(pattern)
            .findAll(this)
            .flatMap { it.groupValues }
            .filter(String::isNotBlank)
            .map(String::toNumberUnchecked)
            .toList()
}

/**
 * Convert a String to an Int
 *
 * This function will numeric strings _and_ words that represent digits into integers.
 *
 * @return The Int representation of a String
 */
private fun String.toNumberUnchecked(): Int {
    return when (this) {
        "one" -> 1
        "two" -> 2
        "three" -> 3
        "four" -> 4
        "five" -> 5
        "six" -> 6
        "seven" -> 7
        "eight" -> 8
        "nine" -> 9
        else -> toInt()
    }
}

/**
 * Derive the calibration value from a list of numbers
 *
 * This function "concatenates" the first digit in a list of numbers and the last digit in a list of
 * numbers into a single two-digit number.
 *
 * @return The calibration value extracted from a list of numbers.
 */
private fun List<Int>.toCalibrationValue(): Int {
    return (this.first() * 10) + this.last()
}

class Day01(input: List<String>) {

    private val input = input

    // In part one, the valid numbers in the string are all digits
    fun solvePart1(): Int = input.map(String::extractDigitsPart1).sumOf { it.toCalibrationValue() }

    // In part two, we also have to account for "number-words" like "one" and "twone"
    fun solvePart2(): Int = input.map(String::extractDigitsPart2).sumOf { it.toCalibrationValue() }
}
