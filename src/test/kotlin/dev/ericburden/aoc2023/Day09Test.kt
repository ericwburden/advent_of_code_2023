package dev.ericburden.aoc2023

import dev.ericburden.aoc2023.Resources.resourceAsLines
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Day 9")
class Day09Test {

    private val example1 = resourceAsLines("day09ex01.txt")
    private val input = resourceAsLines("day09.txt")
    private val part1ExampleResult = 114
    private val part1Answer = 1987402313
    private val part2ExampleResult = 2
    private val part2Answer = 900

    @Nested
    @DisplayName("Part 1")
    inner class Part1 {
        @Test
        fun `Matches example`() {
            val answer = Day09(example1).solvePart1()
            assertThat(answer).isEqualTo(part1ExampleResult)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day09(input).solvePart1()
            assertThat(answer).isEqualTo(part1Answer)
        }
    }

    @Nested
    @DisplayName("Part 2")
    inner class Part2 {
        @Test
        fun `Matches example`() {
            val answer = Day09(example1).solvePart2()
            assertThat(answer).isEqualTo(part2ExampleResult)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day09(input).solvePart2()
            assertThat(answer).isEqualTo(part2Answer)
        }
    }
}
