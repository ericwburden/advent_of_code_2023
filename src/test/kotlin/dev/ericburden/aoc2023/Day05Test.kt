package dev.ericburden.aoc2023

import dev.ericburden.aoc2023.Resources.resourceAsLineChunks
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Day 5")
class Day05Test {

  private val example1 = resourceAsLineChunks("day05ex01.txt")
  private val input = resourceAsLineChunks("day05.txt")
  private val part1ExampleResult = 35L
  private val part1Answer = 157211394L
  private val part2ExampleResult = 46L
  private val part2Answer = 50855035L

  @Nested
  @DisplayName("Part 1")
  inner class Part1 {
    @Test
    fun `Matches example`() {
      val answer = Day05(example1).solvePart1()
      assertThat(answer).isEqualTo(part1ExampleResult)
    }

    @Test
    fun `Actual answer`() {
      val answer = Day05(input).solvePart1()
      assertThat(answer).isEqualTo(part1Answer)
    }
  }

  @Nested
  @DisplayName("Part 2")
  inner class Part2 {
    @Test
    fun `Matches example`() {
      val answer = Day05(example1).solvePart2()
      assertThat(answer).isEqualTo(part2ExampleResult)
    }

    @Test
    fun `Actual answer`() {
      val answer = Day05(input).solvePart2()
      assertThat(answer).isEqualTo(part2Answer)
    }
  }
}
