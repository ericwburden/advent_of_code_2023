package dev.ericburden.aoc2023

import dev.ericburden.aoc2023.Resources.resourceAsLines
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Day 10")
class Day10Test {

    private val example1 = resourceAsLines("day10ex01.txt")
    private val example4 = resourceAsLines("day10ex04.txt")
    private val input = resourceAsLines("day10.txt")
    private val part1ExampleResult = 8
    private val part1Answer = 6968
    private val part2ExampleResult = 10
    private val part2Answer = 413

    @Nested
    @DisplayName("Part 1")
    inner class Part1 {
        @Test
        fun `Matches example`() {
            val answer = Day10(example1).solvePart1()
            assertThat(answer).isEqualTo(part1ExampleResult)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day10(input).solvePart1()
            assertThat(answer).isEqualTo(part1Answer)
        }
    }

    @Nested
    @DisplayName("Part 2")
    inner class Part2 {
        @Test
        fun `Matches example`() {
            val answer = Day10(example4).solvePart2()
            assertThat(answer).isEqualTo(part2ExampleResult)
        }

        @Test
        fun `Is less than known wrong answer`() {
            val answer = Day10(input).solvePart2()
            assertThat(answer).isLessThan(15_172)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day10(input).solvePart2()
            assertThat(answer).isEqualTo(part2Answer)
        }
    }
}
