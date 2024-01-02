package dev.ericburden.aoc2023

import dev.ericburden.aoc2023.Resources.resourceAsLines
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Day 25")
class Day25Test {

    private val example1 = resourceAsLines("day25ex01.txt")
    private val input = resourceAsLines("day25.txt")
    private val part1ExampleResult = 54
    private val part1Answer = 525264

    @Nested
    @DisplayName("Part 1")
    inner class Part1 {
        @Test
        fun `Matches example`() {
            val answer = Day25(example1).solvePart1()
            assertThat(answer).isEqualTo(part1ExampleResult)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day25(input).solvePart1()
            assertThat(answer).isEqualTo(part1Answer)
        }
    }
}
