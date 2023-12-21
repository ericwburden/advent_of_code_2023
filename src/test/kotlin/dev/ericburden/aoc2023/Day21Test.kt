package dev.ericburden.aoc2023

import dev.ericburden.aoc2023.Resources.resourceAsGridOfChar
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Day 21")
class Day21Test {

    private val example1 = resourceAsGridOfChar("day21ex01.txt")
    private val input = resourceAsGridOfChar("day21.txt")
    private val part1ExampleResult = 16
    private val part1Answer = 3594
    private val part2Answer = 605247138198755L

    @Nested
    @DisplayName("Part 1")
    inner class Part1 {
        @Test
        fun `Matches example`() {
            val answer = Day21(example1).solvePart1(6)
            assertThat(answer).isEqualTo(part1ExampleResult)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day21(input).solvePart1(64)
            assertThat(answer).isEqualTo(part1Answer)
        }
    }

    @Nested
    @DisplayName("Part 2")
    inner class Part2 {

        // Part two is another day when solving the real input requires
        // a different approach than solving the example input.

        @Test
        fun `Actual answer`() {
            val answer = Day21(input).solvePart2(26501365)
            assertThat(answer).isEqualTo(part2Answer)
        }
    }
}
