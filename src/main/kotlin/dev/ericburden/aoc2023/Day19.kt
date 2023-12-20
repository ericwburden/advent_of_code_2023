package dev.ericburden.aoc2023

/**
 * This enum represents the four types of part ratings
 */
enum class RatingCategory {
    X, M, A, S;

    companion object {
        /**
         * Parse a [RatingCategory] from a string
         *
         * @param str The string to parse the rating category from.
         * @return A [RatingCategory] parsed from a string.
         * @throws Exception When the string cannot be parsed.
         */
        fun fromString(str: String): RatingCategory = when (str) {
            "x" -> X
            "m" -> M
            "a" -> A
            "s" -> S
            else -> throw Exception("There is no category for $str!")
        }
    }
}

/**
 * This class represents the different possible results from applying a [Rule]
 *
 * Each [Rule] in a [Workflow] has one of three possible outcomes:
 * - The part is accepted.
 * - The part is rejected.
 * - The part is transferred to another workflow.
 */
sealed class RuleResult {
    // This variant represents a transfer to another workflow and includes
    // the label of the new workflow as a property.
    class Transfer(val label: String) : RuleResult()
    data object Approve : RuleResult()
    data object Reject : RuleResult()

    companion object {
        /**
         * Parse a [RuleResult] from a string
         *
         * @param str The string to parse.
         * @return A [RuleResult] parsed from the String.
         */
        fun fromString(str: String) = when (str) {
            "A" -> Approve
            "R" -> Reject
            else -> Transfer(str)
        }
    }
}

/**
 * This enum represents the kind of comparison used by a [Rule]
 *
 * Each [Rule] compares some [RatingCategory] of a [MachinePart] to a constant
 * value when determining whether the part passes that rule or gets forwarded
 * further along the [Workflow].
 */
enum class ComparisonKind {
    GREATER_THAN, LESS_THAN;

    companion object {
        /**
         * Parses a [ComparisonKind] from a string
         *
         * @param str The string to parse a comparison kind from.
         * @return The [ComparisonKind] represented by the input string.
         * @throws Exception When the input string cannot be parsed.
         */
        fun fromString(str: String) = when (str) {
            ">" -> GREATER_THAN
            "<" -> LESS_THAN
            else -> throw Exception("Cannot convert $str to a [ComparisonType]!")
        }
    }
}

/**
 * This class represents a rule for checking a [MachinePart]
 *
 * Each step (except the last) in a [Workflow] consists of a rule that tests
 * a [MachinePart] to see if it is approved, rejected, passed to another
 * [Workflow], or forwarded further along the current [Workflow].
 *
 * @property category Which rating from the [MachinePart] is being checked?
 * @property result The [RuleResult] of successfully applying this rule.
 * @property comparison The kind of comparison to use when applying this rule.
 * @property compareTo The constant value to compare the part's rating to.
 */
data class Rule(
    val category: RatingCategory,
    val result: RuleResult,
    val comparison: ComparisonKind,
    val compareTo: Int,
) {
    companion object {
        /**
         * Parse a [Rule] from a string
         *
         * @param str The string to parse the rule from.
         * @return The [Rule] represented by the parsed string.
         * @throws Exception when the string cannot be parsed.
         */
        fun fromString(str: String): Rule {
            // Use this regular expression to match against the string
            // and break it up into its constituent parts.
            val regex = Regex("""([xmas])([><])(\d+):(\w+)""")
            val matches = regex.find(str)
            val (catStr, opStr, valStr, resStr) = matches?.destructured
                ?: throw Exception("Could not parse $str into a [Rule]!")

            // Then, take each string and parse it individually.
            val testCategory = RatingCategory.fromString(catStr)
            val result = RuleResult.fromString(resStr)
            val comparison = ComparisonKind.fromString(opStr)
            val compareTo = valStr.toInt()

            return Rule(testCategory, result, comparison, compareTo)
        }
    }

    /**
     * Check a [MachinePart] to see if it satisfies this [Rule]
     *
     * @param part The [MachinePart] to check.
     * @return A boolean indicating whether the part satisfies this rule.
     */
    fun passes(part: MachinePart) = when (comparison) {
        ComparisonKind.GREATER_THAN -> part[category] > compareTo
        ComparisonKind.LESS_THAN -> part[category] < compareTo
    }
}

/**
 * This class represents a workflow
 *
 * Each workflow consists of a list of sequential rules to check a [MachinePart]
 * against and a final `default` action to take if the part doesn't satisfy
 * any of the `rules`.
 *
 * @property rules The ordered list of rules to check a [MachinePart] against.
 * @property default The default [RuleResult] to apply if no rule is satisfied.
 */
data class Workflow(
    val rules: List<Rule>,
    val default: RuleResult
) {
    /**
     * Process a [MachinePart] through this [Workflow]
     *
     * Check the part against each rule, in order. If the part satisfies any
     * rule, return the result. Otherwise, return the default result.
     *
     * @param part The [MachinePart] to check.
     * @return The result of the workflow.
     */
    fun processPart(part: MachinePart): RuleResult {
        for (rule in rules) if (rule.passes(part)) return rule.result
        return default
    }

    /**
     * Process a range of machine parts
     *
     * In part two, we no longer care about specific parts and are concerned
     * instead with determining how many possible part combinations could pass
     * through our full [WorkflowSet]. To process a [MachinePartRange], instead
     * of using a [Rule] to determine if the specific values pass, we use that
     * [Rule] to split the [MachinePartRange] into parts that do and do not
     * satisfy the rule. The range that does not satisfy the rule is processed
     * further in the [Workflow] while the range that _does_ satisfy the
     * rule is added to the output of this function along with the associated
     * [RuleResult] of satisfying that [Rule].
     *
     * @param partRange A range of possible machine part ratings.
     * @return A list of machine part ranges and the associated action for each.
     */
    fun processPartRange(partRange: MachinePartRange): List<Pair<MachinePartRange, RuleResult>> {

        // This function will output a list of machine part ranges with the
        // action to take on that range based on whether that part
        // satisfied one of the rules in this workflow.
        val results = mutableListOf<Pair<MachinePartRange, RuleResult>>()
        var currentRange = partRange

        for (rule in rules) {
            currentRange = when (rule.comparison) {
                // When the comparison is "greater than", the part of the range
                // greater than the comparison constant is said to have
                // satisfied the rule and is added to the output. The part of
                // the range that fails the rule is forwarded to the next
                // rule in the workflow.
                ComparisonKind.GREATER_THAN -> {
                    val (partRange1, partRange2) = currentRange.splitAt(
                        rule.category,
                        rule.compareTo + 1
                    )
                    results.add(partRange2 to rule.result)
                    partRange1
                }

                // The same thing in reverse is true for the "less than"
                // comparison.
                ComparisonKind.LESS_THAN -> {
                    val (partRange1, partRange2) = currentRange.splitAt(
                        rule.category,
                        rule.compareTo
                    )
                    results.add(partRange1 to rule.result)
                    partRange2
                }
            }
        }

        // Any range of parts left over didn't satisfy _any_ rules, and so
        // it gets added to the results with the default [RuleResult]
        results.add(currentRange to default)

        return results
    }
}

/**
 * This class represents the full set of all [Workflow]s
 *
 * @property workflows A mapping of workflow label to workflow
 */
data class WorkflowSet(val workflows: Map<String, Workflow>) {
    companion object {
        /**
         * Parse a [WorkflowSet] from a list of lines from the input
         *
         * @param lines The lines from the input that define workflows.
         * @return The parsed [WorkflowSet].
         * @throws Exception When a line from the input cannot be parsed.
         */
        fun fromInput(lines: List<String>): WorkflowSet {
            val regex = Regex("""(\w+)\{(.*)\}""")
            val workflows = lines.associate { line ->
                val matches = regex.find(line)
                val (label, rulesStr) = matches?.destructured
                    ?: throw Exception("Cannot parse a workflow from $line!")
                val rulesStrList = rulesStr.split(",").toMutableList()
                val default = RuleResult.fromString(rulesStrList.removeLast())
                val rules = rulesStrList.map(Rule::fromString)
                label to Workflow(rules, default)
            }
            return WorkflowSet(workflows)
        }
    }

    /**
     * Process a single part through the various workflows
     *
     * Each part starts at the workflow labelled "in". From there, it is
     * processed by one or more [Workflow]s until it is ultimately accepted
     * or rejected.
     *
     * @param part The [MachinePart] to process.
     * @return The result of processing this part.
     * @throws Exception When attempting to pass a part to a [Workflow] that's
     * not in the [WorkflowSet]. This should not happen with well-formed input.
     */
    fun processPart(part: MachinePart): RuleResult {
        var result: RuleResult = RuleResult.Transfer("in")

        // So long as the result of processing the part through a workflow
        // is to transfer it, we keep passing the part to the next workflow
        // and processing. This loop will end once the part is either
        // accepted or rejected.
        while (result is RuleResult.Transfer) {
            result = workflows[result.label]?.processPart(part)
                ?: throw Exception("There is no workflow labeled ${result.label}")
        }
        return result
    }

    /**
     * Find all part ranges that can be approved
     *
     * In part two, we need to identify all the possible combinations of
     * part ratings that can be approved. To do _that_, we need to identify
     * all the _ranges_ of part ratings that ultimately reach the "approved"
     * result.
     *
     * @return A list of [MachinePartRange]s that are ultimately approved.
     * @throws Exception When attempting to pass a part to a [Workflow] that's
     * not in the [WorkflowSet]. This should not happen with well-formed input.
     */
    fun findAllApprovedPartRanges(): List<MachinePartRange> {
        // These are the ranges we'll return at the end
        val approvedRanges = mutableListOf<MachinePartRange>()

        // We'll be doing a variant of a breadth-first search through the
        // workflows. There's no need to check for whether we're in a
        // loop this time, all the workflows flow from start to finish.
        // We start with the full range of 1..4000 in all four ratings
        // at the "in" workflow.
        val queue = ArrayDeque<Pair<MachinePartRange, RuleResult>>(
            listOf(MachinePartRange() to RuleResult.Transfer("in"))
        )

        // Until we've processed all ranges through all workflows...
        while (queue.isNotEmpty()) {
            val (partRange, ruleResult) = queue.removeLast()

            // If the range of parts is approved, it gets added to the results.
            // If it's rejected, we skip it. If it's transferred, then we process
            // it through the target workflow, yielding a list of <range/result>
            // pairings. Each of those pairs is added to the queue for
            // further processing.
            when (ruleResult) {
                is RuleResult.Approve -> approvedRanges.add(partRange)
                is RuleResult.Reject -> continue
                is RuleResult.Transfer -> {
                    val workflow = workflows[ruleResult.label]
                        ?: throw Exception("There is no workflow labeled ${ruleResult.label}!")
                    for (rangeResultPair in workflow.processPartRange(partRange)) {
                        queue.addFirst(rangeResultPair)
                    }
                }
            }
        }

        return approvedRanges
    }
}

/**
 * This class represents a machine part
 *
 * Yes. The rating abbreviations are X-MAS. I saw that too.
 *
 * @property x The degree of coolness of the part.
 * @property m The musicality of the part.
 * @property a The extent of the part's aerodynamic qualities.
 * @property s SHININESS!!!!!
 */
data class MachinePart(val x: Int, val m: Int, val a: Int, val s: Int) {
    companion object {
        /**
         * Parse a [MachinePart] from a string
         *
         * @param str The string representation of a machine part.
         * @return The [MachinePart] represented by the string.
         * @throws Exception If the string does not represent a [MachinePart].
         */
        fun fromString(str: String): MachinePart {
            val regex = Regex("""\{x=(\d+),m=(\d+),a=(\d+),s=(\d+)\}""")
            val matches = regex.find(str)
            val (xStr, mStr, aStr, sStr) = matches?.destructured
                ?: throw Exception("$str cannot be parsed to a [MachinePart]!")
            return MachinePart(
                xStr.toInt(),
                mStr.toInt(),
                aStr.toInt(),
                sStr.toInt()
            )
        }
    }

    // It's kind of nice to be able to index a part by the rating enum.
    operator fun get(category: RatingCategory) = when (category) {
        RatingCategory.X -> x
        RatingCategory.M -> m
        RatingCategory.A -> a
        RatingCategory.S -> s
    }

    val totalRating: Int get() = x + m + a + s
}

/**
 * This class represents a range of machine part ratings
 *
 * There are ultimately four dimensions of [MachinePart] rating. This class
 * represents a four-fold range of possible part ratings.
 *
 * @property x The range of the degree of coolness of the parts.
 * @property m The range of musicality of the parts.
 * @property a The range of the parts' aerodynamic qualities.
 * @property s The range from -shiny- to *!*!SHINY!*!*.
 */
data class MachinePartRange(
    val x: IntRange = 1..4000,
    val m: IntRange = 1..4000,
    val a: IntRange = 1..4000,
    val s: IntRange = 1..4000,
) {
    // Useful here, too.
    operator fun get(category: RatingCategory) = when (category) {
        RatingCategory.X -> x
        RatingCategory.M -> m
        RatingCategory.A -> a
        RatingCategory.S -> s
    }

    // Calculate and return the total number of individual combinations
    // represented by this [MachinePartRange]
    val totalCombinations: Long
        get() {
            val xCount = ((x.last - x.first) + 1).toLong()
            val mCount = ((m.last - m.first) + 1).toLong()
            val aCount = ((a.last - a.first) + 1).toLong()
            val sCount = ((s.last - s.first) + 1).toLong()
            return xCount * mCount * aCount * sCount
        }

    /**
     * Split a [MachinePartRange] around a given rating and value
     *
     * Any time a [MachinePartRange] is tested against a rule (at least, in
     * my input), a part of the range passes the test and another part fails.
     * This function splits the range into these two parts.
     *
     * @param category The rating range to be split.
     * @param value The numeric value to split around.
     * @return A pair of [MachinePartRange]s split on `rating` around `value`.
     */
    fun splitAt(
        category: RatingCategory,
        value: Int
    ): Pair<MachinePartRange, MachinePartRange> {
        // First we verify that the value actually falls within the rating range.
        val range = this[category]
        require(value in range) {
            throw Exception("Cannot split the range $range around $value!")
        }

        // Then we split the range. The split will always exclude the given
        // value from the bottom range and include it in the top range.
        val range1 = this[category].first until value
        val range2 = value..this[category].last

        // Return a pair of [MachinePartRange]s with the new range
        // values on the correct rating.
        return when (category) {
            RatingCategory.X -> (MachinePartRange(range1, m, a, s)
                    to MachinePartRange(range2, m, a, s))

            RatingCategory.M -> (MachinePartRange(x, range1, a, s)
                    to MachinePartRange(x, range2, a, s))

            RatingCategory.A -> (MachinePartRange(x, m, range1, s)
                    to MachinePartRange(x, m, range2, s))

            RatingCategory.S -> (MachinePartRange(x, m, a, range1)
                    to MachinePartRange(x, m, a, range2))
        }
    }
}

class Day19(val input: List<List<String>>) {

    // I feel like _I_ needed a workflow for the amount of modeling I did on
    // today's puzzle. It turned out well, though!
    private val parsed: Pair<WorkflowSet, List<MachinePart>>
        get() {
            val (workflowLines, machinePartLines) = input
            val workflowSet = WorkflowSet.fromInput(workflowLines)
            val machineParts = machinePartLines.map(MachinePart::fromString)
            return workflowSet to machineParts
        }

    // In part one, we check all the given machine parts against a verification
    // workflow and tally the ratings of the approved parts.
    fun solvePart1(): Int {
        val (workflows, parts) = parsed
        return parts.filter { part -> workflows.processPart(part) is RuleResult.Approve }
            .sumOf { it.totalRating }
    }

    // In part two, we stop caring about the individual parts altogether!
    fun solvePart2(): Long {
        val (workflows, _) = parsed
        return workflows.findAllApprovedPartRanges()
            .sumOf { it.totalCombinations }
    }
}
