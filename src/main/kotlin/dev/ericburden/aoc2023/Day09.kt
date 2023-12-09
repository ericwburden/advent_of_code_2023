package dev.ericburden.aoc2023

<<<<<<< HEAD
/**
 * Return the next number in the sequence.
 *
 * This is likely a very brittle approach (meaning the possibility of
 * recursing too deeply for generic lists of numbers), but for these
 * specially crafted sensor readings, it works just fine. This function
 * recursively generates the list of differences and adds the next
 * forecasted value from that list to the last value of the current list.
 *
 * @return The next forecasted value for this sequence of readings.
 */
fun List<Int>.nextSequenceValue(): Int {
    // Base case. No need to go to all zeros, oncw the sequence contains
    // all values the same, we know what the next value will be.
    if (this.all { it == this[0] }) return this[0]

    // Get the differences between each pair of values in `sequence`.
=======
fun List<Int>.nextSequenceValue(): Int {
    if (this.all { it == this[0] }) return this[0]

>>>>>>> origin/solve-day09
    val derivedSequence = this.windowed(2) { (left, right) -> right - left }
    return this.last() + derivedSequence.nextSequenceValue()
}

class Day09(input: List<String>) {

<<<<<<< HEAD
    // List of space-separated strings to list of lists of numbers,
    // coming up!
    private val parsed = input.map { line -> line.split(" ").map { it.toInt() }}

    // In part one, we sum the next number in sequence for each list.
    fun solvePart1(): Int = parsed.sumOf { it.nextSequenceValue() }

    // In part two, we extrapolate each list _backwards_ by one, then sum
    // those results.
=======
    private val parsed = input.map { line -> line.split(" ").map { it.toInt() }}

    fun solvePart1(): Int = parsed.sumOf { it.nextSequenceValue() }

>>>>>>> origin/solve-day09
    fun solvePart2(): Int = parsed.sumOf { it.reversed().nextSequenceValue() }
}
