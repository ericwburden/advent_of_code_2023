package dev.ericburden.aoc2023

import dev.ericburden.aoc2023.Utils.CardinalDirection
import kotlin.math.abs

/**
 * This class represents an instruction from the input file.
 *
 * Each line in the input file can be parsed to an instruction to dig 1x1m
 * sections into a grid, in any cardinal direction.
 *
 * @property direction The direction in which to dig.
 * @property length The number of spaces to dig.
 * @property color THe color of the trench walls.
 */
data class DigInstruction(
    val direction: CardinalDirection,
    val length: Long,
    val color: String
) {
    companion object {
        /**
         * Parse one line from the input file into a [DigInstruction]
         *
         * @param line A line from the input file.
         * @return The parsed [DigInstruction].
         */
        fun fromInputLine(line: String): DigInstruction {
            val (dirStr, lenStr, colStr) = line.split("\\s+".toRegex())
            val direction = when (dirStr) {
                "U" -> CardinalDirection.NORTH
                "R" -> CardinalDirection.EAST
                "D" -> CardinalDirection.SOUTH
                "L" -> CardinalDirection.WEST
                else -> throw Exception("Cannot parse $dirStr to a direction!")
            }
            val length = lenStr.toLongOrNull()
                ?: throw Exception("Cannot parse $lenStr to a number!")

            // Strip everything but the hex digits from color
            val color = colStr.trim('(', ')', '#')
            return DigInstruction(direction, length, color)
        }
    }

    /**
     * Recode a [DigInstruction] based on the part 2 parsing strategy.
     *
     * Turns out those colors _were_ useful after all, just not in any way I
     * would have guessed while solving part one. The color codes actually
     * hold the digging information, and we need to use that instead.
     *
     * @return A re-coded [DigInstruction].
     */
    fun recode(): DigInstruction {
        val firstFive = color.take(5)
        val lastChar = color.last()
        val length = firstFive.toLong(16) // This is super handy
        val direction = when (lastChar) {
            '0' -> CardinalDirection.EAST
            '1' -> CardinalDirection.SOUTH
            '2' -> CardinalDirection.WEST
            '3' -> CardinalDirection.NORTH
            else -> throw Exception("Could not parse $lastChar to a direction!")
        }
        return DigInstruction(direction, length, "#ffffff")
    }
}

// We're going to use polygons today! Our polygon will consist of vertices
// and edges.
data class Vertex(val x: Long, val y: Long)
data class Edge(val v1: Vertex, val v2: Vertex, val len: Long)

/**
 * Convert a list of [DigInstruction]s to a [Polygon]
 *
 * This function extends the functionality of a [List<DigInstruction>] by adding
 * the function `toPolygon`, which allows for trivially converting a list of
 * dig instructions into vertices and edges and, thus, a polygon.
 *
 * @return The resulting [Polygon].
 */
fun List<DigInstruction>.toPolygon(): Polygon {
    // We start at the origin. This is arbitrary, but it's as good a place to
    // start as any.
    val vertices = mutableListOf(Vertex(0, 0))
    val edges = mutableListOf<Edge>()

    // For each instruction, we calculate the location of the next vertex from
    // the dig instruction. We can then use that location, along with the last
    // vertex, to calculate the edge with the last vertex and the newly added
    // vertex.
    for (instruction in this) {
        val (lastRow, lastCol) = vertices.last()
        val vertex = when (instruction.direction) {
            CardinalDirection.NORTH -> Vertex(
                lastRow - instruction.length,
                lastCol
            )

            CardinalDirection.EAST -> Vertex(
                lastRow,
                lastCol + instruction.length
            )

            CardinalDirection.SOUTH -> Vertex(
                lastRow + instruction.length,
                lastCol
            )

            CardinalDirection.WEST -> Vertex(
                lastRow,
                lastCol - instruction.length
            )
        }
        vertices.add(vertex)
        edges.add(Edge(vertices.last(), vertex, instruction.length))
    }
    return Polygon(vertices, edges)
}

fun <T> Sequence<T>.append(value: T): Sequence<T> = sequence {
    yieldAll(this@append)
    yield(value)
}

/**
 * This class represents a 2-dimensional polygon.
 *
 * @property vertices A list of vertices in the polygon.
 * @property edges A list of edges in the polygon.
 */
data class Polygon(val vertices: List<Vertex>, val edges: List<Edge>) {

    // Calculate and return the area of the polygon using the "shoestring"
    // or "surveyor's" method. A nice explanation of this can be found
    // [here](https://www.themathdoctors.org/polygon-coordinates-and-areas/)
    val area: Long get() {
        var lastVertex = vertices.first()
        var leftShoelace = 0L
        var rightShoelace = 0L
        for (nextVertex in vertices.drop(1)) {
            val (x1, y1) = lastVertex
            val (x2, y2) = nextVertex
            leftShoelace += x1 * y2
            rightShoelace += x2 * y1
            lastVertex = nextVertex
        }
        return abs(leftShoelace - rightShoelace) / 2
    }

    // Calculate and return the perimeter of the polygon. It's the sum of
    // all the edge lengths.
    val perimeter: Long get() = edges.sumOf { (_, _, len) -> len }
}

/**
 * This class represents the dig site.
 *
 * Mostly just serves as a wrapper around a [Polygon] to account for the
 * thickness of the trench dug around the outside of the eventual lava lagoon.
 * We need to account for this because the trench counts as spaces where the
 * lava can go.
 *
 * @property polygon The inner polygon formed by digging around the lagoon area.
 */
data class DigSite(val polygon: Polygon) {

    // So, how does the thick line of the trench contribute to overall area
    // available for lava. Consider that the infinitesimal point in space
    // that defines the origin is the point in the exact center of the
    // first 1x1 cube dug. The area, as calculated, is thus bound by an
    // infinitely thin line running through the center of the trench cubes.
    // This means that, for your average side section of the trench, have that
    // trench cube is in the area and half of it is outside and needs to be
    // added back in. The exception here are corner trench cubes. For the four
    // corners, 3/4 of that cube is outside the bounded region. This means an
    // extra 1/4 of a trench cube for each, and for four corners... It's an
    // estra `+ 1`. The _inner_ corners (those bends and twists in the walls)
    // don't matter because for every cube 3/4 inside the bounded area, there's
    // a corresponding cube with only 1/4 inside the bounded area.
    val volume: Long get() = polygon.area + (polygon.perimeter / 2) + 1
}

// Conveniently convert a list of dig instructions to a [DigSite]
fun List<DigInstruction>.toDigSite() = DigSite(this.toPolygon())

class Day18(input: List<String>) {

    // Parse each line in the input into an instruction, and prepare to
    // follow them.
    private val parsed =
        input.filter { it.isNotEmpty() }.map(DigInstruction::fromInputLine)

    // In part one, we don't actually _follow_ the instructions so much as use
    // math as a shortcut!
    fun solvePart1(): Long = parsed.toDigSite().volume

    // In part two, it's a good thing we used math, because we would _never_
    // actually digging that pit and counting the inner area.
    fun solvePart2(): Long =parsed.map { it.recode() }.toDigSite().volume
}
