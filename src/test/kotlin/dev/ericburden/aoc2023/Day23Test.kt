package dev.ericburden.aoc2023

import dev.ericburden.aoc2023.Resources.resourceAsGridOfChar
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Day 23")
class Day23Test {

    private val example1 = resourceAsGridOfChar("day23ex01.txt")
    private val input = resourceAsGridOfChar("day23.txt")
    private val part1ExampleResult = 94
    private val part1Answer = 2202
    private val part2ExampleResult = 154
    private val part2Answer = 6226

    @Nested
    @DisplayName("Part 1")
    inner class Part1 {
        @Test
        fun `Matches example`() {
            val answer = Day23(example1).solvePart1()
            assertThat(answer).isEqualTo(part1ExampleResult)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day23(input).solvePart1()
            assertThat(answer).isEqualTo(part1Answer)
        }
    }

    @Nested
    @DisplayName("Part 2")
    inner class Part2 {
        @Test
        fun `Matches example`() {
            val answer = Day23(example1).solvePart2()
            assertThat(answer).isEqualTo(part2ExampleResult)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day23(input).solvePart2()
            assertThat(answer).isEqualTo(part2Answer)
        }
    }
}
