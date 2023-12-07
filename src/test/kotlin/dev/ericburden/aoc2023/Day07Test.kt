package dev.ericburden.aoc2023

import dev.ericburden.aoc2023.Resources.resourceAsLines
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Day 7")
class Day07Test {

    private val example1 = resourceAsLines("day07ex01.txt")
    private val input = resourceAsLines("day07.txt")
    private val part1ExampleResult = 6440
    private val part1Answer = 252052080
    private val part2ExampleResult = 5905
    private val part2Answer = 252898370

    @Nested
    @DisplayName("Part 1")
    inner class Part1 {
        @Test
        fun `Matches example`() {
            val answer = Day07(example1).solvePart1()
            assertThat(answer).isEqualTo(part1ExampleResult)
        }

        
        @Test
        fun `Higher than wrong answer`() {
            val answer = Day07(input).solvePart1()
            assertThat(answer).isGreaterThan(251749028)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day07(input).solvePart1()
            assertThat(answer).isEqualTo(part1Answer)
        }
    }

    @Nested
    @DisplayName("Part 2")
    inner class Part2 {
        @Test
        fun `Matches example`() {
            val answer = Day07(example1).solvePart2()
            assertThat(answer).isEqualTo(part2ExampleResult)
        }

        
        @Test
        fun `Higher than wrong answers`() {
            val answer = Day07(input).solvePart2()
            assertThat(answer).isGreaterThan(252347214)
            assertThat(answer).isGreaterThan(252804525)
        }

        @Test
        fun `Actual answer`() {
            val answer = Day07(input).solvePart2()
            assertThat(answer).isEqualTo(part2Answer)
        }
    }
}
