package dev.ericburden.aoc2023

import dev.ericburden.aoc2023.Resources.resourceAsText
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Day 3")
class Day03Test {

  private val example1 = resourceAsText("day03ex01.txt")
  private val example2 = resourceAsText("day03ex02.txt")
  private val input = resourceAsText("day03.txt")
  private val part1Example1Result = 4361
  private val part1Example2Result = 925
  private val part1Answer = 514_969
  private val part2Example1Result = 467_835
  private val part2Example2Result = 6756
  private val part2Answer = 78_915_902

  @Nested
  @DisplayName("Part 1")
  inner class Part1 {
    @Test
    fun `Matches example`() {
      val answer = Day03(example1).solvePart1()
      assertThat(answer).isEqualTo(part1Example1Result)
    }

    @Test
    fun `Matches another example`() {
      val answer = Day03(example2).solvePart1()
      assertThat(answer).isEqualTo(part1Example2Result)
    }

    @Test
    fun `Actual answer`() {
      val answer = Day03(input).solvePart1()
      assertThat(answer).isEqualTo(part1Answer)
    }
  }

  @Nested
  @DisplayName("Part 2")
  inner class Part2 {
    @Test
    fun `Matches example`() {
      val answer = Day03(example1).solvePart2()
      assertThat(answer).isEqualTo(part2Example1Result)
    }

    @Test
    fun `Matches another example`() {
      val answer = Day03(example2).solvePart2()
      assertThat(answer).isEqualTo(part2Example2Result)
    }

    @Test
    fun `Actual answer`() {
      val answer = Day03(input).solvePart2()
      assertThat(answer).isEqualTo(part2Answer)
    }
  }
}
