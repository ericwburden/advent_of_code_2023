package dev.ericburden.aoc2023

class Day01(input: String) {

  private val parsed = input.trim().split("\n\n").map { it.lines().toList() }
  private val calorieList = parsed.map { it.sumOf(String::toInt) }

  fun solvePart1(): Int = calorieList.max()

  fun solvePart2(): Int = calorieList.sortedDescending().take(3).sum()
}
