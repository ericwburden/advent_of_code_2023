package dev.ericburden.aoc2023

/**
 * This class represents on cubic component of a [BrickOfSand]
 *
 * @property x This cube's x-coordinate.
 * @property y This cube's y-coordinate.
 * @property z This cube's z-coordinate.
 */
data class SandCube(val x: Int, val y: Int, val z: Int)

/**
 * This class represents a solid brick of sand
 *
 * I have no idea how the elves were able to get the sand to actually stay
 * in "brick" form. Christmas magic?
 *
 * @property cubes The cubes that, stuck together, comprise this brick of sand.
 */
data class BrickOfSand(val cubes: List<SandCube>) {
    companion object {
        /**
         * Parse a [BrickOfSand] from an input line
         *
         * @param str The input line representing a brick of sand.
         * @return The [BrickOfSand] represented by `str`.
         * @throws Exception When the input cannot be parsed.
         */
        fun fromString(str: String): BrickOfSand {
            val regex = Regex("""(\d),(\d),(\d+)~(\d),(\d),(\d+)""")
            val matches =
                regex.find(str)?.groupValues?.drop(1)?.map { it.toInt() }
                    ?: throw Exception("Could not parse $str into a [BrickOfSand]!")

            // Apparently you can't destructure 6 things at once from a list.
            val (x1, y1, z1) = matches
            val (x2, y2, z2) = matches.drop(3)

            // Depending on _which_ dimension changes from the first cube to the
            // last cube, produce a list of cubes from the front to the back
            // of this brick.
            val cubes = when {
                x2 > x1 -> (x1..x2).map { x -> SandCube(x, y1, z1) }
                x1 > x2 -> (x2..x1).map { x -> SandCube(x, y1, z1) }
                y2 > y1 -> (y1..y2).map { y -> SandCube(x1, y, z1) }
                y1 > y2 -> (y2..y1).map { y -> SandCube(x1, y, z1) }
                z2 > z1 -> (z1..z2).map { z -> SandCube(x1, y1, z) }
                z1 > z2 -> (z2..z1).map { z -> SandCube(x1, y1, z) }
                else -> listOf(SandCube(x1, y1, z1)) // Tiny brick!
            }

            return BrickOfSand(cubes)
        }
    }

    /**
     * Drop this brick by a given amount along the z-axis
     *
     * This is the function used to simulate dropping the brick from where it
     * starts to where it lands.
     *
     * @param amount How many steps along the z-axis to drop this brick.
     * @return A copy of this brick, dropped along the z-axis.
     */
    fun dropBy(amount: Int): BrickOfSand {
        val cubes = cubes.map { (x, y, z) -> SandCube(x, y, z - amount) }
        return BrickOfSand(cubes)
    }
}

/**
 * This class represents the pile of sand bricks
 *
 * The topographical map is mostly used for building up the pile of bricks,
 * although it was nice to be able to print it out to verify that bricks were
 * going where I thought they were.
 *
 * @property bricks A list of the bricks in the pile, in the order that the
 * bricks are added to the pile.
 * @property topoMap A 2D grid representing the height of the pile at each
 * x/y-coordinate.
 * @property spaceMap A mapping of each occupied cube to the brick that owns
 * that cube.
 */
data class SandBrickPile(
    val bricks: MutableList<BrickOfSand> = mutableListOf(),
    val topoMap: List<MutableList<Int>> = List(10) { MutableList(10) { 1 } },
    val spaceMap: MutableMap<SandCube, BrickOfSand> = mutableMapOf()
) {
    /**
     * Add a brick to the pile
     *
     * Drop a brick from it's starting position until it settles. Update the
     * list of bricks, topographic map, and spatial map of bricks to accommodate.
     *
     * @param brick The brick to drop
     */
    fun addBrick(brick: BrickOfSand) {
        // Figure out how far we can drop this brick by checking the height
        // of the topographic map underneath this falling brick. Then, drop
        // this brick by the minimum distance between a brick cube and the
        // tallest stack underneath.
        val dropToZ = brick.cubes.maxOf { (x, y, _) -> topoMap[y][x] }
        val dropByAmt = brick.cubes.minOf { (_, _, z) -> z - dropToZ }
        val droppedBrick = brick.dropBy(dropByAmt)

        // Add it to the list
        bricks.add(droppedBrick)

        // Update the topographical map
        droppedBrick.cubes.forEach { (x, y, z) -> topoMap[y][x] = z + 1 }

        // Update the spatial map
        droppedBrick.cubes.forEach { cube -> spaceMap[cube] = droppedBrick }
    }

    /**
     * Identify the bricks above the current brick (in contact)
     *
     * Get all the bricks resting on the brick being considered. A brick
     * that's long on the z-axis would include itself, so those need to
     * be filtered out.
     *
     * @param brick The brick to check for bricks above.
     * @return The list of bricks above the current brick.
     */
    private fun bricksAbove(brick: BrickOfSand) =
        brick.cubes.mapNotNull { (x, y, z) ->
            spaceMap[SandCube(x, y, z + 1)]
        }.filter { it != brick }

    /**
     * Identify the bricks below the current brick (in contact)
     *
     * Get all the bricks supporting the brick being considered. A brick
     * that's long on the z-axis would include itself, so those need to
     * be filtered out.
     *
     * @param brick The brick to check for bricks above.
     * @return The list of bricks above the current brick.
     */
    private fun bricksBelow(brick: BrickOfSand) =
        brick.cubes.mapNotNull { (x, y, z) ->
            spaceMap[SandCube(x, y, z - 1)]
        }.filter { it != brick }

    /**
     * Check to see if the target brick can be safely disintegrated
     *
     * A brick can be safely disintegrated if removing it wouldn't cause any
     * other bricks to fall and create a cascade of sandy blocks of doom.
     */
    fun canDisintegrate(brick: BrickOfSand): Boolean {
        // If all of the bricks above are resting on at least one other brick,
        // this brick can be disintegrated. The `all` function will return true
        // if `bricksAbove` is empty, as well.
        return bricksAbove(brick).all { brickAbove ->
            bricksBelow(brickAbove).any { brickBelow -> brickBelow != brick }
        }
    }

    /**
     * Count the number of bricks that would fall if the target brick is removed
     *
     * @param keystone The brick being removed.
     */
    fun bricksDependingOn(keystone: BrickOfSand): Int {
        // We're going to do a depth-first search starting with the keystone
        // and working up the pile, adding bricks that will fall to our set
        // of fallen bricks.
        val stack = mutableListOf(keystone)
        val fallenBricks = mutableSetOf<BrickOfSand>()

        while (stack.isNotEmpty()) {
            // Get the current brick to check off the top of the stack. If
            // we've somehow already checked this one, skip it. Otherwise,
            // add it to the set of fallen bricks.
            val brick = stack.removeLast()
            if (brick in fallenBricks) continue
            fallenBricks.add(brick)

            // Now get all the bricks that will fall if `brick` is
            // removed
            val nowFalling =
                bricksAbove(brick).filter { brickAbove ->
                    bricksBelow(brickAbove).all { it in fallenBricks }
                }

            // For each brick that is now falling, add it to the stack for
            // future consideration, unless it's already been considered.
            for (newFallingBrick in nowFalling) {
                if (newFallingBrick in fallenBricks) continue
                stack.add(newFallingBrick)
            }
        }

        // Don't forget we don't count the original brick being removed
        return fallenBricks.size - 1
    }
}

/**
 * Convert a list of sand bricks to a pile of sand bricks
 */
fun List<BrickOfSand>.pileUp(): SandBrickPile {
    val pile = SandBrickPile()
    // We need to sort the incoming bricks by the z axis so that we don't end
    // up trying to drop a brick inside a pile that's already piled up past where
    // that brick starts.
    sortedBy { brick -> brick.cubes.minOf { it.z } }.forEach { pile.addBrick(it) }
    return pile
}

class Day22(input: List<String>) {

    // Parse that list into bricks!
    private val parsed =
        input.filter { it.isNotEmpty() }.map(BrickOfSand::fromString)

    // For part one, we pile up the bricks and see how many we could eliminate
    // without unsettling the pile.
    fun solvePart1(): Int {
        val pile = parsed.pileUp()
        return pile.bricks.count { brick -> pile.canDisintegrate(brick) }
    }

    // For part two, we pile up the bricks (again) and start dropping bricks to
    // see how many fall!
    fun solvePart2(): Int {
        val pile = parsed.pileUp()
        return pile.bricks.sumOf { brick -> pile.bricksDependingOn(brick) }
    }
}
