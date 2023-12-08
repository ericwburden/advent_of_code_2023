package dev.ericburden.aoc2023

import dev.ericburden.aoc2023.Resources.resourceAsLineChunks
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Day 8")
class Day08Test {

    private val example1 = resourceAsLineChunks("day08ex01.txt")
    private val example2 = resourceAsLineChunks("day08ex02.txt")
    private val example3 = resourceAsLineChunks("day08ex03.txt")
    private val input = resourceAsLineChunks("day08.txt")
    private val part1Example1Result = 2
    private val part1Example2Result = 6
    private val part1Answer = 21251
    private val part2Example3Result = 6L
    private val part2Answer = 11678319315857L

    @Nested
    @DisplayName("Part 1")
    inner class Part1 {
        @Test
        fun `Matches example 1`() {
            val answer = Day08(example1).solvePart1()
            assertThat(answer).isEqualTo(part1Example1Result)
        }

        @Test
        fun `Matches example 2`() {
            val answer = Day08(example2).solvePart1()
            assertThat(answer).isEqualTo(part1Example2Result)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day08(input).solvePart1()
            assertThat(answer).isEqualTo(part1Answer)
        }
    }

    @Nested
    @DisplayName("Part 2")
    inner class Part2 {
        @Test
        fun `Matches example`() {
            val answer = Day08(example3).solvePart2(true)
            assertThat(answer).isEqualTo(part2Example3Result)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day08(input).solvePart2()
            assertThat(answer).isEqualTo(part2Answer)
        }
    }
}
