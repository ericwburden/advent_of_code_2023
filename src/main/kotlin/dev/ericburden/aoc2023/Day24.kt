package dev.ericburden.aoc2023

import kotlin.math.roundToLong

/**
 * This class represents a rectangular region in 2D space.
 *
 * @property left The X-value of the left edge of the region.
 * @property right The X-value of the right edge of the region.
 * @property top The Y-value of the top edge of the region.
 * @property bottom The Y-value of the bottom edge of the region.
 */
data class Region2D(
    val left: Double, val right: Double, val top: Double, val bottom: Double
) {
    /**
     * Indicates whether an X/Y position is contained within the [Region2D]
     */
    fun contains(location: Pair<Double, Double>) =
        location.first in left..right && location.second in top..bottom
}

/**
 * Solve a system of linear equations using Gaussian Elimination
 *
 * Yep, this comes from Wikipedia too. Specifically:
 * https://en.wikipedia.org/wiki/Gaussian_elimination. As I understand it, this
 * works by converting a system of linear equation into a simpler series of
 * equations.
 *
 * For the example, solving for the X, Y, dX, and dY components, the matrix
 * of coefficients is simplified like so:
 *
 *  [[-2, -1, -6, -1,  -44],     >     [[1, 0, 0, 0, 24],   (X)
 *   [-1,  1, -6,  2,    9],     >      [0, 1, 0, 0, 13],   (Y)
 *   [ 0,  1, -6, -8,   -3],     >      [0, 0, 1, 0, -3],   (dX)
 *   [-3, -2, 12,  8, -126]]     >      [0, 0, 0, 1,  1]]   (dY)
 *
 * This is called the "reduced row echelon" form, and achieving this state means
 * that our constants on the right-hand side of the equation _is_ the value for
 * the unknown variable, like:
 *
 *    -2X - 1Y -  6dX - 1dY =  -44  ->  1X + 0Y + 0dX + 0dY = 24  ->  X = 24
 *    -1X + 1Y -  6dX + 2dY =    9  ->  0X + 1Y + 0dX + 0dY = 13  ->  Y = 13
 *     0X + 1Y -  6dX - 8dY =   -3  ->  0X + 0Y + 1dX + 0dY = -3  -> dX = -3
 *    -3X - 2Y + 12dX + 8dY = -126  ->  0X + 0Y + 0dX + 1dY =  1  -> dy =  1
 *
 * @param coefficients The coefficients of the system of linear equations.
 * @return The coefficients on the right-hand side of the simplified system
 * of equations.
 */
fun gaussianElimination(coefficients: List<MutableList<Double>>): List<Double> {
    val rows = coefficients.size
    val cols = coefficients.first().size

    // This only works on a square matrix (with one extra column for the
    // coefficient on the right-hand side of the equation).
    require(rows == cols - 1) {
        throw Exception(
            "The number of coefficients on the left side of the" +
                    "equation should be equal to the number of equations."
        )
    }

    // We operate on each row in the matrix of coefficients.
    for (row in coefficients.indices) {

        // Normalize the row starting with the diagonal value of each row.
        val pivot = coefficients[row][row]
        for (col in coefficients[row].indices) {
            coefficients[row][col] /= pivot
        }

        // Sweep the other rows with `row`
        for (otherRow in coefficients.indices) {
            if (row == otherRow) continue

            val factor = coefficients[otherRow][row]
            for (col in coefficients[otherRow].indices) {
                coefficients[otherRow][col] -= factor * coefficients[row][col]
            }
        }
    }

    return coefficients.map { it.last() }.toList()
}

/**
 * This class represents one of the hailstones.
 *
 * @property x The initial X-coordinate of the stone.
 * @property y The initial Y-coordinate of the stone.
 * @property z The initial Z-coordinate of the stone.
 * @property dx The initial X velocity of the stone.
 * @property dy The initial Y velocity of the stone.
 * @property dz The initial Z velocity of the stone.
 */
data class Hailstone(
    val x: Double,
    val y: Double,
    val z: Double,
    val dx: Double,
    val dy: Double,
    val dz: Double
) {
    companion object {
        /**
         * Parse a [Hailstone] from a line of the input file
         *
         * @param str The string representation of a [Hailstone].
         * @return The [Hailstone] represented by `str`.
         */
        fun fromString(str: String): Hailstone {
            val splitRegex = Regex("""[, @]+""")
            val numbers = str.split(splitRegex).map { it.toDouble() }

            // Still can't destructure a list with six item all at once.
            val (x, y, z) = numbers
            val (dx, dy, dz) = numbers.drop(3)

            return Hailstone(x, y, z, dx, dy, dz)
        }
    }

    /**
     * Find the X,Y intersection between the future paths of two hailstones
     *
     * Given that we can ignore both the time (partially) and the z-axis of
     * the paths of these hailstones, we're really looking at a system of
     * two equations and two unknowns. We can use the formula for finding the
     * intersection of two lines from:
     * https://en.wikipedia.org/wiki/Intersection_(geometry)#Two_line_segments.
     *
     * From what I understand, `t` (and `u`) represents the proportion along
     * the first line segment where it intersects with the second line segment.
     *
     * @param h2 The other hailstone whose path may intersect with this one.
     * @return The X/Y position of the intersection, if there is one. If the
     * lines are parallel, or the intersection occurred prior to the
     * hailstones' starting positions, return `null`.
     */
    private fun intersectionXYWith(h2: Hailstone): Pair<Double, Double>? {
        // These lines are parallel, they don't intersect
        val denominator = (dx * h2.dy) - (dy * h2.dx)
        if (denominator == 0.0) return null

        val h1 = this
        val t = ((h2.x - h1.x) * h2.dy - (h2.y - h1.y) * h2.dx) / denominator
        val u = ((h2.x - h1.x) * h1.dy - (h2.y - h1.y) * h1.dx) / denominator

        // Intersection is prior to the stones' initial position (negative
        // time).
        if (t < 0 || u < 0) return null

        // This is from the Wikipedia article as well!
        return (h1.x + t * h1.dx) to (h1.y + t * h1.dy)
    }

    fun intersectionIn2DRegion(h2: Hailstone, region2D: Region2D): Boolean {
        val intersection = intersectionXYWith(h2) ?: return false
        return region2D.contains(intersection)
    }
}

class Day24(input: List<String>) {

    // Parse each line from the input into a [Hailstone].
    private val parsed =
        input.filter { it.isNotEmpty() }.map(Hailstone::fromString)

    // For each unique pair of hailstones, identify whether the X/Y paths of the
    // hailstones will cross. Return the count of pairs that cross paths.
    fun solvePart1(region2D: Region2D): Int {
        var totalIntersectionsInRegion = 0
        for (i1 in 0 until parsed.size - 1) for (i2 in i1 until parsed.size) {
            val h1 = parsed[i1]
            val h2 = parsed[i2]
            if (h1.intersectionIn2DRegion(h2, region2D)) {
                totalIntersectionsInRegion += 1
            }
        }
        return totalIntersectionsInRegion
    }

    /**
     * Argh! It's geometry! And linear algebra! Yuck. Here's how this goes:
     *
     * Say the rock we want to throw to smash all the hailstones starts out at
     * xR, yR, zR, dxR, dyR, dzR, but we don't actually know what any of those
     * values is. We can start "simply" by identifying where on each axis
     * and when as time (t) the rock should collide with one hailstone
     * (with properties of, say: x, y, z, dx, dy, dz) as:
     *
     *      xR + (t * dxR) = x + (t * dx)
     *      yR + (t * dyR) = y + (t * dy)
     *      zR + (t * dzR) = z + (t * dz)
     *
     * Rearranging to solve for `t`, we get:
     *
     *      t = (xR - x)/(dx - dxR) = (yR - y)/(dy - dyR) = (zR - z)/(dz - dzR).
     *
     * For _just_ two axes (start with X/Y again), we can isolate the
     * values related to just the rock (which won't change from one hailstone
     * to another in order to solve the puzzle) by rearranging the relationship
     * between the X and Y axes like so:
     *
     *      (xR - x)/(dx - dxR) = (yR - y)/(dy - dyR)
     *      (xR - x)(dy - dyR)  = (yR - y)(dx - dxR)
     *      xR*dy - x*dy - xR*dyR + x*dyR = yR*dx - yR*dxR - y*dx + y*dxR
     *      yR*dxR - xR*dyR = yR*dx - y*dx + y*dxR - xR*dy + x*dy - x*dyR
     *
     * Because the terms (yR*dxR - xR*dyR) should be the same no matter which
     * hailstone we consider (in order to hit all the hailstones), we can
     * alternatively consider another hailstone with properties of, say:
     * x', y', z', dx', dy', dz', like so:
     *
     *      yR*dxR - xR*dyR = yR*dx' - y'*dx' + y'*dxR - xR*dy' + x'*dy' - x'*dyR
     *
     * Because (yR*dxR - xR*dyR) is unchanging, it must be true that:
     *
     *      yR*dx - y*dx + y*dxR - xR*dy + x*dy - x*dyR = yR*dx' - y'*dx' + y'*dxR - xR*dy' + x'*dy' - x'*dyR
     *      (dy'-dy)xR + (dx - dx')yR + (y - y')dxR + (x' - x)dyR = y*dx - x*dy -y'*dx' + x'dy'
     *
     * Since we need to solve for the properties of the rock, we can substitute
     * the actual values from any pair of hailstones into this equation. We'll
     * need at least four pairs of hailstones to solve for the four unknowns.
     *
     * We can perform the same rearrangement for the X and Z axes like so:
     *
     *      (xR - x)/(dx - dxR) = (zR - z)/(dz - dzR)
     *      (xR - x)(dz - dzR)  = (zR - z)(dx - dxR)
     *      xR*dz - x*dz - xR*dzR + x*dzR = zR*dx - zR*dxR - z*dx + z*dxR
     *      zR*dxR - xR*dzR = zR*dx  -  z*dx  + z*dxR  - xR*dz  + x*dz   - x*dzR
     *                      = zR*dx' - z'*dx' + z'*dxR - xR*dz' + x'*dz' - x'*dzR
     *      zR*dx - z*dx + z*dxR - xR*dz + x*dz  - x*dzR = zR*dx' - z'*dx' + z'*dxR - xR*dz' + x'*dz' - x'*dzR
     *      (dz'-dz)xR + (dx - dx')zR + (z - z')dxR + (x' - x)dzR = z*dx - x*dz -z'*dx' + x'dz'
     *
     * The neat thing is, if we already know xR and dxR from solving the first set
     * of equations, then we only have two unknowns remaining (zR and dzR), for
     * which we only need two pairs of hailstones by rearranging the system of
     * equations like:
     *
     *       (dx - dx')zR + (x' - x)dzR = z*dx - x*dz -z'*dx' + x'dz' - (dz'-dz)xR - (z - z')dxR
     *
     *  Now, let's get solving!
     */
    fun solvePart2(): Long {
        val (rockX, rockY, rockDX, rockDY) = gaussianElimination(
            parsed.take(5).windowed(2).map { (h1, h2) ->
                // This comes from:
                // (dy'-dy)xR + (dx - dx')yR + (y - y')dxR + (x' - x)dyR = y*dx - x*dy -y'*dx' + x'dy'
                mutableListOf(
                    h2.dy - h1.dy,  // (dy' - dy)
                    h1.dx - h2.dx,  // (dx - dx')
                    h1.y - h2.y,    // (y - y')
                    h2.x - h1.x,    // (x' - x')

                    // This is the right-hand side of the equation, or
                    // y*dx - x*dy -y'*dx' + x'dy'
                    ((h1.y * h1.dx) + (-h1.x * h1.dy) + (-h2.y * h2.dx) + (h2.x * h2.dy))
                )
            }).map { it.roundToLong() } // Prevents issues with precision

        val (rockZ, rockDZ) = gaussianElimination(
            parsed.take(3).windowed(2).map { (h1, h2) ->
                mutableListOf(
                    // This comes from:
                    // (dx - dx')zR + (x' - x)dzR = z*dx - x*dz -z'*dx' + x'dz' - (dz'-dz)xR - (z - z')dxR
                    h1.dx - h2.dx,  // (dx - dx')
                    h2.x - h1.x,    // (x' - x)

                    // This is the right-hand side again
                    //  z*dx - x*dz - z'*dx' + x'dz' - (dz'-dz)xR - (z - z')dxR
                    // @formatter:off
                    ( (h1.z  * h1.dx)                       // z*dx
                    - (h1.x  * h1.dz)                       // x*dz
                    - (h2.z  * h2.dx)                       // z'*dx'
                    + (h2.x  * h2.dz)                       // x'*dz'
                    - ((h2.dz - h1.dz) * rockX.toDouble())  // (dz'-dz)xR
                    - ((h1.z  - h2.z)  * rockDX))           // (z - z')dxR
                    //@formatter:on
                )
            }).map { it.roundToLong() }

        // We've solved for all the necessary parameters now, return the sum!
        return rockX + rockY + rockZ
    }
}
