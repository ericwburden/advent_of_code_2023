package dev.ericburden.aoc2023

import dev.ericburden.aoc2023.Resources.resourceAsLines
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Day 4")
class Day04Test {

  private val example1 = resourceAsLines("day04ex01.txt")
  private val input = resourceAsLines("day04.txt")
  private val part1ExampleResult = 13
  private val part1Answer = 20_829
  private val part2ExampleResult = 30
  private val part2Answer = 12_648_035

  @Nested
  @DisplayName("Part 1")
  inner class Part1 {
    @Test
    fun `Matches example`() {
      val answer = Day04(example1).solvePart1()
      assertThat(answer).isEqualTo(part1ExampleResult)
    }

    @Test
    fun `Actual answer`() {
      val answer = Day04(input).solvePart1()
      assertThat(answer).isEqualTo(part1Answer)
    }
  }

  @Nested
  @DisplayName("Part 2")
  inner class Part2 {
    @Test
    fun `Matches example`() {
      val answer = Day04(example1).solvePart2()
      assertThat(answer).isEqualTo(part2ExampleResult)
    }

    @Test
    fun `Actual answer`() {
      val answer = Day04(input).solvePart2()
      assertThat(answer).isEqualTo(part2Answer)
    }
  }
}
