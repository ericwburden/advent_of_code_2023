package dev.ericburden.aoc2023

class Day01(input: List<List<String>>) {

  private val parsed = input.map { it.sumOf(String::toInt) }

  fun solvePart1(): Int = parsed.max()

  fun solvePart2(): Int = parsed.sortedDescending().take(3).sum()
}
