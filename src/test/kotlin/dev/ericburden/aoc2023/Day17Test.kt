package dev.ericburden.aoc2023

import dev.ericburden.aoc2023.Resources.resourceAsGridOfDigits
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Day 17")
class Day17Test {

    private val example1 = resourceAsGridOfDigits("day17ex01.txt")
    private val example2 = resourceAsGridOfDigits("day17ex02.txt")
    private val input = resourceAsGridOfDigits("day17.txt")
    private val part1ExampleResult = 102
    private val part1Answer = 1110
    private val part2Example1Result = 94
    private val part2Example2Result = 71
    private val part2Answer = 1294

    @Nested
    @DisplayName("Part 1")
    inner class Part1 {
        @Test
        fun `Matches example`() {
            val answer = Day17(example1).solvePart1()
            assertThat(answer).isEqualTo(part1ExampleResult)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day17(input).solvePart1()
            assertThat(answer).isEqualTo(part1Answer)
        }
    }

    @Nested
    @DisplayName("Part 2")
    inner class Part2 {
        @Test
        fun `Matches example`() {
            val answer = Day17(example1).solvePart2()
            assertThat(answer).isEqualTo(part2Example1Result)
        }

        @Test
        fun `Matches other example`() {
            val answer = Day17(example2).solvePart2()
            assertThat(answer).isEqualTo(part2Example2Result)
        }

        @Test
        fun `Lower than wrong answer`() {
            val answer = Day17(input).solvePart2()
            assertThat(answer).isLessThan(1296)
        }

        @Test
        fun `Higher than wrong answer`() {
            val answer = Day17(input).solvePart2()
            assertThat(answer).isGreaterThan(1292)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day17(input).solvePart2()
            assertThat(answer).isEqualTo(part2Answer)
        }
    }
}
