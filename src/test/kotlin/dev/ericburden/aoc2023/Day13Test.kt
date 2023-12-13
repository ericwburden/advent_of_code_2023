package dev.ericburden.aoc2023

import dev.ericburden.aoc2023.Resources.resourceAsLineChunks
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Day 13")
class Day13Test {

    private val example1 = resourceAsLineChunks("day13ex01.txt")
    private val input = resourceAsLineChunks("day13.txt")
    private val part1ExampleResult = 405
    private val part1Answer = 27505
    private val part2ExampleResult = 400
    private val part2Answer = 22906

    @Nested
    @DisplayName("Part 1")
    inner class Part1 {
        @Test
        fun `Matches example`() {
            val answer = Day13(example1).solvePart1()
            assertThat(answer).isEqualTo(part1ExampleResult)
        }

        @Test
        fun `Actual answer must be greater than answer too low`() {
            val answer = Day13(input).solvePart1()
            assertThat(answer).isGreaterThan(17302)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day13(input).solvePart1()
            assertThat(answer).isEqualTo(part1Answer)
        }
    }

    @Nested
    @DisplayName("Part 2")
    inner class Part2 {
        @Test
        fun `Matches example`() {
            val answer = Day13(example1).solvePart2()
            assertThat(answer).isEqualTo(part2ExampleResult)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day13(input).solvePart2()
            assertThat(answer).isEqualTo(part2Answer)
        }
    }
}
