package dev.ericburden.aoc2023

import dev.ericburden.aoc2023.Resources.resourceAsLines
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Day 20")
class Day20Test {

    private val example1 = resourceAsLines("day20ex01.txt")
    private val example2 = resourceAsLines("day20ex02.txt")
    private val input = resourceAsLines("day20.txt")
    private val part1Example1Result = 32000000
    private val part1Example2Result = 11687500
    private val part1Answer = 836127690
    private val part2Answer = 240914003753369L

    @Nested
    @DisplayName("Part 1")
    inner class Part1 {
        @Test
        fun `Matches example 1`() {
            val answer = Day20(example1).solvePart1()
            assertThat(answer).isEqualTo(part1Example1Result)
        }

        @Test
        fun `Matches example 2`() {
            val answer = Day20(example2).solvePart1()
            assertThat(answer).isEqualTo(part1Example2Result)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day20(input).solvePart1()
            assertThat(answer).isEqualTo(part1Answer)
        }
    }

    @Nested
    @DisplayName("Part 2")
    inner class Part2 {
        @Test
        fun `Actual answer`() {
            val answer = Day20(input).solvePart2()
            assertThat(answer).isEqualTo(part2Answer)
        }
    }
}
