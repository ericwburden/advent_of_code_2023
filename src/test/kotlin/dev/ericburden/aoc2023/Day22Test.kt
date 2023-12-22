package dev.ericburden.aoc2023

import dev.ericburden.aoc2023.Resources.resourceAsLines
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Day 22")
class Day22Test {

    private val example1 = resourceAsLines("day22ex01.txt")
    private val input = resourceAsLines("day22.txt")
    private val part1ExampleResult = 5
    private val part1Answer = 451
    private val part2ExampleResult = 7
    private val part2Answer = 66530

    @Nested
    @DisplayName("Part 1")
    inner class Part1 {
        @Test
        fun `Matches example`() {
            val answer = Day22(example1).solvePart1()
            assertThat(answer).isEqualTo(part1ExampleResult)
        }

        @Test
        fun `Answer is less than wrong answer`() {
            val answer = Day22(input).solvePart1()
            assertThat(answer).isLessThan(461)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day22(input).solvePart1()
            assertThat(answer).isEqualTo(part1Answer)
        }
    }

    @Nested
    @DisplayName("Part 2")
    inner class Part2 {
        @Test
        fun `Matches example`() {
            val answer = Day22(example1).solvePart2()
            assertThat(answer).isEqualTo(part2ExampleResult)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day22(input).solvePart2()
            assertThat(answer).isEqualTo(part2Answer)
        }
    }
}
