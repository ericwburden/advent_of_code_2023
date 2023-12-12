package dev.ericburden.aoc2023

import dev.ericburden.aoc2023.Utils.repeating

/**
 * Dynamic Programming gives me headaches, so I'm going to give a couple of
 * examples of how the algorithm below works for my own benefit.
 *
 * ## Example 1: ???.### 1,1,3
 *
 * In this example, we have three groups of damaged spring sections (of lengths
 * 1, 1, and 3) and 8 total spring sections, of which 2 are operational ('.'),
 * 3 are damaged ('#'), and the remaining 3 are unknown ('?'). The essence of
 * the DP approach here is to repeatedly calculate the number of possible
 * combinations of spring sections that are possible under sets of increasing
 * constraints, which are, in order:
 * - no damaged sections
 * - one damage section of `Damaged Groups`[0] length
 * - the first _and_ second damaged sections
 * - all three damaged sections
 *
 * For this example, I'll walk through them one constraint at a time.
 *
 * ### First Constraint: No Damaged Groups
 *
 * Damaged Groups: []
 * Starting Conditions  :    [., ?, ?, ?, ., #, #, #]
 * Possible Combinations: [1, 1, 1, 1, 1, 1, 0, 0, 0]
 *                        /   |  |  |  |  |   \
 *                       a    b  c  d  e  f    g
 *
 * Note, the list of possible combinations is one longer than the list of spring
 * sections, and is offset from the spring section list by -1. This means that
 * the first value in the list represents the number of possible ways to
 * construct an empty list of spring sections with the given groups (in this
 * case, none). Conveniently, there is one way to construct an empty list, so
 * the value at (a) is 1.
 *
 * The second value, (b) is also 1, because it is possible to construct a spring
 * with one operational section when there are no damaged sections. The values
 * at (c-f) are similarly 1, because there is one way a spring can be
 * constructed of '.???.' sections with no damage, which is '.....'.
 *
 * On the other hand, (g) must be 0, because it is not possible to construct
 * a pipe with one damaged section if there are no damaged sections allowed.
 * This carries forward to the rest of the list, since '.....#' and '.....#?'
 * are equally impossible.
 *
 * ### Second Constraint: One group of one damaged section
 *
 * In this second round, we are required to construct a spring with one length
 * of damaged spring consisting of only a single 'section'. It is important here
 * to remember that each length of damaged spring (one or more sections) must
 * be separated by an operational section.
 *
 * Groups: [1]
 * Starting Conditions  :    [., ?, ?, ?, ., #, #, #]
 * Possible Combinations: [1, 1, 1, 1, 1, 1, 0, 0, 0] with no groups
 * Possible Combinations: [0, 0, 1, 2, 3, 3, 1, 0, 0] add group of 1 section
 *                        /  /   |  |  |  |  |   \
 *                       a  b    c  d  e  f  g    h
 *
 * With a group length of 1, the pattern is readily recognized as
 * `current[i] = current[i - 1] + (previous[i - 2] ?: 0)`. I'm using the
 * `?:` notation here to indicate 'zero if index out of bounds'.
 *
 * We see that (a, b) are zero, which makes sense because you can't construct
 * a spring with a 1-wide defect from '' or '.'. '.?' (c) _could_ be '.#', so
 * there's one possible configuration at this point that meets our criteria.
 * '.??' (d) could be '.#.' or '..#' and '.???' (e) could be '.#..', '..#.', or
 * '...#'. (f) is the same as (e) with an extra operational section at the end.
 *
 * (g) and (h) break the pattern, though because '#' is a damaged section, which
 * constrains our options for assigning the damaged region. At (g), there's only
 * one way to assign a 1-wide region of damage, and at (h) there's more damage
 * than we can accommodate. Notably, the value at (h) would be the same if the
 * spring were '.???.#?' as well. This means we need to keep up with the width
 * of the damaged sections up to our current position and determine when we can't
 * carry over combinations that were available up to the current point.
 * This would be when either the current group size is larger than the run of
 * possibly damaged sections _or_ the section just prior to where the group
 * would be established is also DAMAGED (which would make the actual damaged
 * region too large). The logic goes like this:
 *
 *      damage can fit = group size <= run of DAMAGED and UNKNOWN
 *                   AND section just prior to group insertion point is not DAMAGE
 *      current[i] = if current section is not DAMAGED and damage can fit:
 *          (A) current[i - 1] + (previous[i - 2] ?: 0)
 *      else if current section is DAMAGED and damage can fit:
 *          (B) (previous[i - 2] ?: 0)
 *      else if current section is not DAMAGED:
 *          (C) current[i - 1]
 *      else:
 *          (D) 0
 *
 * That final `else` applies if the current section _is_ DAMAGED and the group
 * size is larger than the current run of DAMAGED and UNKNOWN sections.
 *
 * ### Third Constraint: Two groups of 1 damaged section each
 *
 * Groups: [1, 1]
 * Starting Conditions  :    [., ?, ?, ?, ., #, #, #]
 * Possible Combinations: [1, 1, 1, 1, 1, 1, 0, 0, 0] with no groups
 * Possible Combinations: [0, 0, 1, 2, 3, 3, 1, 0, 0] add group of 1
 * Possible Combinations: [0, 0, 0, 0, 1, 1, 3, 0, 0] add group of 1
 *                              /   |  |  |  |   \
 *                             a    b  c  d  e    f
 *
 * Here, we see that (a) is behaving as expected. At (a), the run of possible
 * damage is 1 and the spring section is not damaged, so branch (A) of our
 * logic applies, yielding 0. The same is true for (b-d). Logically,
 * we can see that there's only one way to distribute two 1-wide damage regions
 * into '.???.', which is '.#.#.'. Recall that the damaged regions must be
 * separated by an operational section.
 *
 * (e) is interesting because the spring section there _is_ damaged, which
 * ties up one of the damage groups and, again logically, we see that this
 * means that the _other_ damage group can be distributed three different ways,
 * as '.#...#', '..#..#', or '...#.#'. Because the section at (e) is damaged and
 * the current group can fit in the run, this uses branch (B) of our logic above.
 *
 * (f) is a spring section that *is* damaged and the section just prior to the
 * insertion area is also DAMAGED (which would make a two-wide damaged region),
 * so branch (D) applies.
 *
 * ### Third Constraint: Two groups of 1 and one group of 3 damaged sections
 *
 * Groups: [1, 1, 3]
 * Starting Conditions  :    [., ?, ?, ?, ., #, #, #]
 * Possible Combinations: [1, 1, 1, 1, 1, 1, 0, 0, 0] with no groups
 * Possible Combinations: [0, 0, 1, 2, 3, 3, 1, 0, 0] add group of 1
 * Possible Combinations: [0, 0, 0, 0, 1, 1, 3, 0, 0] add group of 1
 * Possible Combinations: [0, 0, 0, 0, 0, 0, 0, 0, 1] add group of 3
 *                                    /         |   \
 *                                   a          b    c
 *
 * Our options are much more limited now, needing to add a 3-wide damaged
 * section on top of the two 1-wide sections. This means that when we reach
 * section (a), we'll only have space for either the two 1-wide sections or
 * or the one 3-wide section, but not both. At point (a), considering the
 * three-wide group, the current section is _not_ damaged and we do have
 * enough room to insert the group, so we take branch (B) of our logic.
 *
 * At section (b), the section _is_ damaged and we do _not_ have space to
 * insert our 3-wide damage group, so we take branch (D). Finally, at section
 * (c), the section _is_ damaged and we _do_ have space for our 3-wide
 * group, so we take branch (B). Except, now branch (B) would yield a result
 * of 0, but we can clearly see that there's one way to arrange all three
 * damage groups: '.#.#.###'. If we examine the logic of branch (B), we can see
 * that with the first two groups, we were selecting the number of possible
 * combinations containing the previous groups at the point _before_ we
 * were inserting the current group. Making a slight modification yields the
 * final algorithm:
 *
 *      damage can fit = group size <= run of DAMAGED and UNKNOWN
 *                   AND section just prior to group insertion point is not DAMAGE
 *      current[i] = if current section is not DAMAGED and damage can fit:
 *          (A) current[i - 1] + (previous[i - (group size + 1)] ?: 0)
 *      else if current section is DAMAGED and damage can fit:
 *          (B) (previous[i - (group size + 1)] ?: 0)
 *      else if current section is not DAMAGED:
 *          (C) current[i - 1]
 *      else:
 *          (D) 0
 *
 * The code below will follow this logic, with some minor indexing tweaks to
 * account for the fact that we can't _really_ shift the pipe section
 * representation over by one index as we did when visualizing these examples.
 *
 * I encourage you to apply the logic above to this second example of a spring
 * from today's example input if you're not 100% clear on how it works yet.
 *
 * Example 2: ?###???????? 3,2,1
 *
 * Damaged Groups: [3, 2, 1]
 * Starting Conditions  :    [., ?, #, #, #, ?, ?, ?, ?, ?, ?, ?, ?]
 * Possible Combinations: [1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0] with no groups
 * Possible Combinations: [0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1] add group of 3
 * Possible Combinations: [0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6] add group of 2
 * Possible Combinations: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 3, 6, 10] add group of 1
 */

/**
 * This enum represents the different states for a section of spring
 *
 * @property rep The character that represents this enum variant.
 */
enum class Condition(val rep: Char) {
    DAMAGED('#'), OPERATIONAL('.'), UNKNOWN('?');

    companion object {
        fun fromChar(char: Char): Condition {
            return when (char) {
                '#' -> DAMAGED
                '.' -> OPERATIONAL
                '?' -> UNKNOWN
                else -> throw IllegalArgumentException("Cannot represent '$char' as a [Condition]!")
            }
        }
    }
}

/**
 * This class represents the condition record of a single spring.
 *
 * @property sections A list of the conditions of the sections of the spring.
 * @property damagedGroups A list of known sizes of damaged regions.
 */
data class ConditionRecord(
    val sections: List<Condition>, val damagedGroups: List<Int>
) {
    companion object {
        // Parse the input lines!
        fun fromString(str: String): ConditionRecord {
            val (conditionStr, groupStr) = str.split(" ").map { it.trim() }
            val conditions = conditionStr.map(Condition::fromChar)
            val groups = groupStr.split(",").map { it.toInt() }
            return ConditionRecord(conditions, groups)
        }
    }

    fun countAllPossibleConditions(): Long {
        // The list of conditions should start with _one_ OPERATIONAL
        // section in order to support the algorithm for counting possible
        // combinations of conditions. Excess OPERATIONAL sections at the
        // beginning are removed.
        val sections = mutableListOf(Condition.OPERATIONAL)
        sections.addAll(this.sections.dropWhile { it == Condition.OPERATIONAL })

        // Note, the first value in this list does not correspond to the first
        // section, but to a hypothetical 'empty' section preceding the list
        // of spring sections. This means there is exactly 1 possible
        // configuration for an empty list of spring sections.
        var possibleCombinations = MutableList(sections.size + 1) { 1L }

        // Starting with the first DAMAGED section all the way to the end,
        // set the number of possible combinations to zero, because there is
        // no way to make a spring with damaged sections if there is no
        // damage (our starting constraint).
        //
        //      Starting Conditions  :    [., ?, ?, ?, ., #, #, #]
        //      Possible Combinations: [1, 1, 1, 1, 1, 1, 0, 0, 0]
        for ((idx, _) in sections.withIndex()
            .dropWhile { (_, c) -> c != Condition.DAMAGED }) {
            possibleCombinations[idx + 1] = 0
        }

        // For each group size of damaged sections, we successively build upon
        // the previous set of possible constraints, determining how many
        // combinations are possible after adding each group as a constraint.
        for (checkedGroupSize in damagedGroups) {
            // Each new 'layer' of possibilities starts out empty.
            val nextPossibleCombinations =
                MutableList(sections.size + 1) { 0L }
            var possiblyDamagedRunSize = 0

            for ((idx, condition) in sections.withIndex().drop(1)) {
                // Reset the 'group' of possibly DAMAGED sections to consider if
                // we encounter an OPERATIONAL section.
                if (condition == Condition.OPERATIONAL) {
                    possiblyDamagedRunSize = 0
                } else {
                    possiblyDamagedRunSize += 1
                }

                // Here is where we implement our logic described exhaustively
                // above:

                // damage can fit = group size <= run of DAMAGED and UNKNOWN
                //              AND section just prior to group insertion point is not DAMAGED
                // current[i] = if current section is not DAMAGED and damage can fit:
                //     (A) current[i - 1] + (previous[i - (group size + 1)] ?: 0)
                // else if current section is DAMAGED and damage can fit:
                //     (B) (previous[i - (group size + 1)] ?: 0)
                // else if current section is not DAMAGED:
                //     (C) current[i - 1]
                // else:
                //     (D) 0
                val groupCanFit = possiblyDamagedRunSize >= checkedGroupSize
                val precedingIdx = (idx - checkedGroupSize).coerceAtLeast(0)
                val notContiguousDamage =
                    sections[precedingIdx] != Condition.DAMAGED
                val isNotDamaged = condition != Condition.DAMAGED
                val damageCanFit = groupCanFit && notContiguousDamage
                nextPossibleCombinations[idx + 1] =
                    if (isNotDamaged && damageCanFit) {
                        nextPossibleCombinations[idx] + possibleCombinations[precedingIdx]
                    } else if (condition == Condition.DAMAGED && damageCanFit) {
                        possibleCombinations[precedingIdx]
                    } else if (isNotDamaged) {
                        nextPossibleCombinations[idx]
                    } else {
                        0L
                    }
            }

            // Update the 'current' layer
            possibleCombinations = nextPossibleCombinations
        }

        // Update the number of possible combinations with the result of
        // determining possible combinations with the current group
        // of DAMAGED sections.
        return possibleCombinations.last()
    }

    fun unfold(): ConditionRecord {
        val conditions =
            listOf(sections + Condition.UNKNOWN).repeating().asSequence()
                .take(5).flatMap { it }.toMutableList().dropLast(1)
        val damagedGroups =
            listOf(damagedGroups).repeating().asSequence().take(5)
                .flatMap { it }.toList()
        return ConditionRecord(conditions, damagedGroups)
    }
}

class Day12(input: List<String>) {

    private val parsed =
        input.filter { it.isNotEmpty() }.map(ConditionRecord::fromString)

    fun solvePart1(): Long = parsed.sumOf { it.countAllPossibleConditions() }

    fun solvePart2(): Long =
        parsed.sumOf { it.unfold().countAllPossibleConditions() }
}
