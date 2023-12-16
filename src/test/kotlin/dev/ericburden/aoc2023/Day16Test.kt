package dev.ericburden.aoc2023

import dev.ericburden.aoc2023.Resources.resourceAsGridOfChar
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Day 16")
class Day16Test {

    private val example1 = resourceAsGridOfChar("day16ex01.txt")
    private val input = resourceAsGridOfChar("day16.txt")
    private val part1ExampleResult = 46
    private val part1Answer = 7307
    private val part2ExampleResult = 51
    private val part2Answer = 7635

    @Nested
    @DisplayName("Part 1")
    inner class Part1 {
        @Test
        fun `Matches example`() {
            val answer = Day16(example1).solvePart1()
            assertThat(answer).isEqualTo(part1ExampleResult)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day16(input).solvePart1()
            assertThat(answer).isEqualTo(part1Answer)
        }
    }

    @Nested
    @DisplayName("Part 2")
    inner class Part2 {
        @Test
        fun `Matches example`() {
            val answer = Day16(example1).solvePart2()
            assertThat(answer).isEqualTo(part2ExampleResult)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day16(input).solvePart2()
            assertThat(answer).isEqualTo(part2Answer)
        }
    }
}
