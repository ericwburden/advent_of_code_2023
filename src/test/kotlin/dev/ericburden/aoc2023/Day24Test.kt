package dev.ericburden.aoc2023

import dev.ericburden.aoc2023.Resources.resourceAsLines
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Day 24")
class Day24Test {

    private val example1 = resourceAsLines("day24ex01.txt")
    private val input = resourceAsLines("day24.txt")
    private val part1ExampleResult = 2
    private val part1Answer = 16812
    private val part2ExampleResult = 47L
    private val part2Answer = 880547248556435L

    private val testRegion2D = Region2D(7.0, 27.0, 7.0, 27.0)
    private val realRegion2D = Region2D(
        200000000000000.0,
        400000000000000.0,
        200000000000000.0,
        400000000000000.0
    )

    @Nested
    @DisplayName("Part 1")
    inner class Part1 {
        @Test
        fun `Matches example`() {
            val answer = Day24(example1).solvePart1(testRegion2D)
            assertThat(answer).isEqualTo(part1ExampleResult)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day24(input).solvePart1(realRegion2D)
            assertThat(answer).isEqualTo(part1Answer)
        }
    }

    @Nested
    @DisplayName("Part 2")
    inner class Part2 {
        @Test
        fun `Matches example`() {
            val answer = Day24(example1).solvePart2()
            assertThat(answer).isEqualTo(part2ExampleResult)
        }

        // 2147483646 too low
        @Test
        fun `Actual answer`() {
            val answer = Day24(input).solvePart2()
            assertThat(answer).isEqualTo(part2Answer)
        }
    }
}
