package dev.ericburden.aoc2023

import dev.ericburden.aoc2023.Resources.resourceAsGridOfChar
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Day 11")
class Day11Test {

    private val example1 = resourceAsGridOfChar("day11ex01.txt")
    private val input = resourceAsGridOfChar("day11.txt")
    private val part1ExampleResult = 374
    private val part1Answer = 9418609
    private val part2ExampleResult = 8410L
    private val part2Answer = 593821230983L

    @Nested
    @DisplayName("Part 1")
    inner class Part1 {
        @Test
        fun `Matches example`() {
            val answer = Day11(example1).solvePart1()
            assertThat(answer).isEqualTo(part1ExampleResult)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day11(input).solvePart1()
            assertThat(answer).isEqualTo(part1Answer)
        }
    }

    @Nested
    @DisplayName("Part 2")
    inner class Part2 {
        @Test
        fun `Matches example with expansion factor 100`() {
            val answer = Day11(example1).solvePart2(100)
            assertThat(answer).isEqualTo(part2ExampleResult)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day11(input).solvePart2(1_000_000)
            assertThat(answer).isEqualTo(part2Answer)
        }
    }
}
