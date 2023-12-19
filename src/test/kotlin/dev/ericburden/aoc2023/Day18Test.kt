package dev.ericburden.aoc2023

import dev.ericburden.aoc2023.Resources.resourceAsLines
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Day 18")
class Day18Test {

    private val example1 = resourceAsLines("day18ex01.txt")
    private val input = resourceAsLines("day18.txt")
    private val part1ExampleResult = 62L
    private val part1Answer = 48503L
    private val part2ExampleResult = 952408144115L
    private val part2Answer = 148442153147147L

    @Nested
    @DisplayName("Part 1")
    inner class Part1 {
        @Test
        fun `Matches example`() {
            val answer = Day18(example1).solvePart1()
            assertThat(answer).isEqualTo(part1ExampleResult)
        }

        @Test
        fun `Lower than wrong answer`() {
            val answer = Day18(input).solvePart1()
            assertThat(answer).isLessThan(50495)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day18(input).solvePart1()
            assertThat(answer).isEqualTo(part1Answer)
        }
    }

    @Nested
    @DisplayName("Part 2")
    inner class Part2 {
        @Test
        fun `Matches example`() {
            val answer = Day18(example1).solvePart2()
            assertThat(answer).isEqualTo(part2ExampleResult)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day18(input).solvePart2()
            assertThat(answer).isEqualTo(part2Answer)
        }
    }
}
