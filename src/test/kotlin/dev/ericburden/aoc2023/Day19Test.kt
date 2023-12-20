package dev.ericburden.aoc2023

import dev.ericburden.aoc2023.Resources.resourceAsLineChunks
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Day 19")
class Day19Test {

    private val example1 = resourceAsLineChunks("day19ex01.txt")
    private val input = resourceAsLineChunks("day19.txt")
    private val part1ExampleResult = 19114
    private val part1Answer = 280909
    private val part2ExampleResult = 167409079868000L
    private val part2Answer = 116138474394508L

    @Nested
    @DisplayName("Part 1")
    inner class Part1 {
        @Test
        fun `Matches example`() {
            val answer = Day19(example1).solvePart1()
            assertThat(answer).isEqualTo(part1ExampleResult)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day19(input).solvePart1()
            assertThat(answer).isEqualTo(part1Answer)
        }
    }

    @Nested
    @DisplayName("Part 2")
    inner class Part2 {
        @Test
        fun `Matches example`() {
            val answer = Day19(example1).solvePart2()
            assertThat(answer).isEqualTo(part2ExampleResult)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day19(input).solvePart2()
            assertThat(answer).isEqualTo(part2Answer)
        }
    }
}
