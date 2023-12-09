package dev.ericburden.aoc2023

fun List<Int>.nextSequenceValue(): Int {
    if (this.all { it == this[0] }) return this[0]

    val derivedSequence = this.windowed(2) { (left, right) -> right - left }
    return this.last() + derivedSequence.nextSequenceValue()
}

class Day09(input: List<String>) {

    private val parsed = input.map { line -> line.split(" ").map { it.toInt() }}

    fun solvePart1(): Int = parsed.sumOf { it.nextSequenceValue() }

    fun solvePart2(): Int = parsed.sumOf { it.reversed().nextSequenceValue() }
}
