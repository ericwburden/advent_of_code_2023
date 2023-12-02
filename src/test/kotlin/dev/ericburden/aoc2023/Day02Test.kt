package dev.ericburden.aoc2023

import dev.ericburden.aoc2023.Resources.resourceAsLines
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Day 2")
class Day02Test {

  private val example1 = resourceAsLines("day02ex01.txt")
  private val input = resourceAsLines("day02.txt")
  private val part1ExampleResult = 8
  private val part1Answer = 2439
  private val part2ExampleResult = 2286
  private val part2Answer = 63_711

  @Nested
  @DisplayName("Part 1")
  inner class Part1 {
    @Test
    fun `Matches example`() {
      val answer = Day02(example1).solvePart1()
      assertThat(answer).isEqualTo(part1ExampleResult)
    }

    @Test
    fun `Actual answer`() {
      val answer = Day02(input).solvePart1()
      assertThat(answer).isEqualTo(part1Answer)
    }
  }

  @Nested
  @DisplayName("Part 2")
  inner class Part2 {
    @Test
    fun `Matches example`() {
      val answer = Day02(example1).solvePart2()
      assertThat(answer).isEqualTo(part2ExampleResult)
    }

    @Test
    fun `Actual answer`() {
      val answer = Day02(input).solvePart2()
      assertThat(answer).isEqualTo(part2Answer)
    }
  }
}
