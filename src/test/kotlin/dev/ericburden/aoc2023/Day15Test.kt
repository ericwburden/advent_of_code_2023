package dev.ericburden.aoc2023

import dev.ericburden.aoc2023.Resources.resourceAsText
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Day 15")
class Day15Test {

    private val example1 = resourceAsText("day15ex01.txt")
    private val input = resourceAsText("day15.txt")
    private val part1ExampleResult = 1320
    private val part1Answer = 511215
    private val part2ExampleResult = 145
    private val part2Answer = 236057

    @Nested
    @DisplayName("Part 1")
    inner class Part1 {
        @Test
        fun `Matches example`() {
            val answer = Day15(example1).solvePart1()
            assertThat(answer).isEqualTo(part1ExampleResult)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day15(input).solvePart1()
            assertThat(answer).isEqualTo(part1Answer)
        }
    }

    @Nested
    @DisplayName("Part 2")
    inner class Part2 {
        @Test
        fun `Matches example`() {
            val answer = Day15(example1).solvePart2()
            assertThat(answer).isEqualTo(part2ExampleResult)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day15(input).solvePart2()
            assertThat(answer).isEqualTo(part2Answer)
        }
    }
}
