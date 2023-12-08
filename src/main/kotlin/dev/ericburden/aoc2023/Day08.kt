package dev.ericburden.aoc2023

// More special utilities!
import dev.ericburden.aoc2023.Utils.repeating
import dev.ericburden.aoc2023.Utils.lcm

/**
 * This enum represents a direction, either left or right.
 */
enum class Direction {
    LEFT,
    RIGHT;

    companion object {
        fun fromChar(char: Char): Direction {
            return when (char) {
                'L' -> LEFT
                'R' -> RIGHT
                else -> throw IllegalArgumentException("$char cannot be parsed to a Direction!")
            }
        }
    }
}

/**
 * This class represents a "node" on the map.
 *
 * Each node essentially serves as a signpost to the next node,
 * identifying the name of either the next node to the left or
 * the next node to the right.
 *
 * @property left The name of the next node to the left.
 * @property right The name of the next node to the right.
 */
data class Signpost(val left: String, val right: String) {
    fun nextFromDirection(direction: Direction): String {
        return when (direction) {
            Direction.LEFT -> left
            Direction.RIGHT -> right
        }
    }
}

/**
 * This class represents an instance of our mysterious map.
 *
 * @property directions A private iterator that can continually
 * produce the next [Direction] to take.
 * @property map The mapping of "node" name to the signpost for
 * the next possible nodes.
 */
class NodeMap(private val directions: Iterator<Direction>, val map: Map<String, Signpost>) {
    companion object {
        /**
         * Parses a [NodeMap] from the input file chunks.
         *
         * @param input A list of "chunks" of the input file, where
         * each "chunk" is a list of lines.
         */
        fun fromInput(input: List<List<String>>): NodeMap {
            val (directionChunk, nodeChunk) = input
            val directions = directionChunk.single().map(Direction::fromChar).repeating()

            // '.associate' is analagous to 'map().toMap().'
            val map = nodeChunk.associate { line ->
                val re = """(\w+) = \((\w+), (\w+)\)""".toRegex()
                val matchResult =
                    re.find(line)
                        ?: throw IllegalArgumentException(
                            "$line is not formatted properly!"
                        )
                val (label, left, right) = matchResult.destructured
                label to Signpost(left, right)
            }

            return NodeMap(directions, map)
        }
    }

    /**
     * Advance from a position to some other position that satisfies a condition.
     *
     * Given the name of a node on the map, follow the directions and advance from
     * node to node until the current node satisfies some condition.
     *
     * @param start The name of the node to start at.
     * @param stop A predicate function that indicates when to stop advancing.
     * @return A pair of the number of steps taken and the name of the node stopped on.
     */
    fun advanceUntil(start: String, stop: (String) -> Boolean): Pair<Int, String> {
        var currentLocation = start
        var steps = 0

        while (!stop(currentLocation)) {
            currentLocation = this.advanceOnce(currentLocation)
            steps += 1
        }

        return steps to currentLocation
    }

    /**
     * Advance one step from a given node.
     *
     * Given the name of a node on the map, follow the directions and advance
     * a single step forward.
     *
     * @param start The name of the node to start at.
     * @return The name of the next node in sequence.
     */
    fun advanceOnce(start: String): String {
        val direction = directions.next()
        val node = map[start] ?: throw IllegalArgumentException("$start is not a node on the map!")
        return node.nextFromDirection(direction)
    }
}


class Day08(input: List<List<String>>) {

    // Parse that input!
    private val nodeMap = NodeMap.fromInput(input)

    // In part one, we follow the signs from our starting position
    // to our target and count the steps taken.
    fun solvePart1(): Int = nodeMap.advanceUntil("AAA") { it == "ZZZ" }.first

    // In part two we pretend to be some sort of quantum ghost and
    // try to take multiple paths simultaneously. This seems like it
    // might be _more_ traumatic than the sandstorm...
    fun solvePart2(example: Boolean = false): Long {
        // Predicates to select lists that end with 'A' or 'Z'.
        val endsWithA: (String) -> Boolean = { str -> str[str.length - 1] == 'A' }
        val endsWithZ: (String) -> Boolean = { str -> str[str.length - 1] == 'Z' }

        // Our starting locations are the ones that end with 'A'
        val startingLocations = nodeMap.map.keys.filter(endsWithA)
        val destinations = startingLocations.map { loc -> nodeMap.advanceUntil(loc, endsWithZ) }

        // Need to check and be sure that each 'path' starting from a node whose
        // name ends with 'A' is actually cyclical. If it _does_ take the same
        // number of steps to get back to the _same_ end node as it took to get
        // there originally, then we can calculate the number of steps needed to
        // get to _all_ end nodes as the least common multiple of the number of
        // steps in each path in a cycle. If not... then my only other option
        // (that I know of) is to turn on the brute force method of just taking
        // one step on each path then checking to see if all paths found an end.
        // Oddly enough, this enters an endless loop on the part two example
        // (it's that "XXX = (XXX, XXX)" line), so I need to skip this bit when
        // running my test on the example input. _Kind_ of a bummer that the apparent
        // intended solution fails on the example.
        if (!example) {
            val nextDestinations = destinations.map { (_, loc) ->
                val start = nodeMap.advanceOnce(loc)
                val (steps, end) = nodeMap.advanceUntil(start, endsWithZ)
                steps + 1 to end// Account for that one extra step from the end
            }
            assert(destinations == nextDestinations) // Cross your fingers!
        }

        // I happen to know that this works, at least for my input.
        return destinations.map { it.first.toLong() }.lcm()
    }
}
