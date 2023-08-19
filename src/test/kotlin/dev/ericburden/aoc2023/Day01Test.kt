package dev.ericburden.aoc2023

import dev.ericburden.aoc2023.Resources.resourceAsText
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Day 1")
class Day01Test {

  private val input = resourceAsText("day01ex01.txt")

  @Nested
  @DisplayName("Part 1")
  inner class Part1 {
    @Test
    fun `Matches example`() {
      val answer = Day01(input).solvePart1()
      assertThat(answer).isEqualTo(24_000)
    }

    @Test
    fun `Actual answer`() {
      val answer = Day01(resourceAsText("day01.txt")).solvePart1()
      assertThat(answer).isEqualTo(69_795)
    }
  }

  @Nested
  @DisplayName("Part 2")
  inner class Part2 {
    @Test
    fun `Matches example`() {
      val answer = Day01(input).solvePart2()
      assertThat(answer).isEqualTo(45_000)
    }

    @Test
    fun `Actual answer`() {
      val answer = Day01(resourceAsText("day01.txt")).solvePart2()
      assertThat(answer).isEqualTo(208_437)
    }
  }
}
