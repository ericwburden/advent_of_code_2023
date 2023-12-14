package dev.ericburden.aoc2023

import dev.ericburden.aoc2023.Resources.resourceAsGridOfChar
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Day 14")
class Day14Test {

    private val example1 = resourceAsGridOfChar("day14ex01.txt")
    private val input = resourceAsGridOfChar("day14.txt")
    private val part1ExampleResult = 136
    private val part1Answer = 105208
    private val part2ExampleResult = 64
    private val part2Answer = 102943

    @Nested
    @DisplayName("Part 1")
    inner class Part1 {
        @Test
        fun `Matches example`() {
            val answer = Day14(example1).solvePart1()
            assertThat(answer).isEqualTo(part1ExampleResult)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day14(input).solvePart1()
            assertThat(answer).isEqualTo(part1Answer)
        }
    }

    @Nested
    @DisplayName("Part 2")
    inner class Part2 {
        @Test
        fun `Matches example`() {
            val answer = Day14(example1).solvePart2()
            assertThat(answer).isEqualTo(part2ExampleResult)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day14(input).solvePart2()
            assertThat(answer).isEqualTo(part2Answer)
        }
    }
}
