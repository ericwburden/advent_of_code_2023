package dev.ericburden.aoc2023

/**
 * This is a data class that represents a set of colored cubes
 *
 * @property red The number of red cubes
 * @property green The number of green cubes
 * @property blue The number of blue cubes
 * @constructor Create a new [CubeSet] with the given number of [red], [blue], and [green] cubes.
 */
data class CubeSet(val red: Int, val green: Int, val blue: Int) {
  companion object {
    /**
     * Parse a [CubeSet] from a String
     *
     * @param input The string to be parsed
     * @throws IllegalArgumentException When the input string is not formatted properly
     * @return A [CubeSet]
     */
    fun fromString(input: String): CubeSet {
      var red = 0
      var green = 0
      var blue = 0

      // Split a string like "1 red, 2 green, 3 blue" on commas and trim each.
      // For each number/color combination, split a string like "1 red" into
      // [1, "red"] and check to be sure we got the parts we were expecting. If
      // so, increase the number of cubes indicated by the color. Once all color
      // cubes are counted, return a [CubeSet] with those counts.
      val colorCounts = input.split(",").map { it.trim() }
      for (colorCount in colorCounts) {
        val parts = colorCount.split(" ")
        require(parts.size == 2) { "$colorCount does not match expected format!" }

        val count =
            parts[0].toIntOrNull()
                ?: throw IllegalArgumentException("$colorCount does not contain a valid count!")
        val color = parts[1].lowercase()

        when (color) {
          "red" -> red += count
          "green" -> green += count
          "blue" -> blue += count
          else -> throw IllegalArgumentException("$colorCount does not contain a valid color!")
        }
      }

      return CubeSet(red, green, blue)
    }
  }

  /**
   * Determines whether this [CubeSet] is valid
   *
   * A [CubSet] is valid if the number of cubes in it can be contained in the [max] cubeset it is
   * being compared to.
   *
   * @param max A [CubeSet] to compare against
   * @return A boolean indicating whether this [CubeSet] is valid
   */
  fun isValid(max: CubeSet): Boolean {
    return red <= max.red && green <= max.green && blue <= max.blue
  }

  /**
   * Calculate the power of this [CubeSet]
   *
   * Power is the product of the number of red, green, and blue cubes
   *
   * @return The calculated power
   */
  fun power(): Int {
    return red * green * blue
  }
}

/**
 * This class represents one round of the [Game]
 *
 * Each round, the elf reveals a handful of cubes. This class holds the [id] of the game and
 * maintains a list of the [handfuls] of cubes revealed by the elf.
 *
 * @property id The unique game identifier.
 * @property handfules The [CubSet]s revealed by the elf.
 * @constructor Creates a new [CubSet] with the given [id] and [handfuls] of revealed cubes.
 */
data class Game(val id: Int, val handfuls: List<CubeSet>) {
  companion object {
    /**
     * Parse a [Game] from a line of the input file
     *
     * @param input A line from the input file.
     * @throws IllegalArgumentException When the input is malformed.
     * @return A [Game] parsed from the input line.
     */
    fun fromString(input: String): Game {
      // Input in the format of "Game 1: 3 blue, 4 red; 1 red, 2 green, 6 blue; 2 green".
      // Split on the colon to get ["Game 1", "3 blue, 4 red; 1 red, 2 green, 6 blue; 2 green"].
      val mainParts = input.split(":").map { it.trim() }
      require(mainParts.size == 2) { "$input is not a valid Game format!" }
      val (gamePart, cubePart) = mainParts

      // Get the Game ID from a string like "Game 1". If this part of the input line
      // is not formed correctly, throw an exception.
      val gameParts = gamePart.split(" ").map { it.trim() }
      require(gameParts[0] == "Game") {
        throw IllegalArgumentException("$gamePart is not a valid Game format!")
      }
      val id =
          gameParts[1].toIntOrNull()
              ?: throw IllegalArgumentException("$gamePart does not contain a valid id!")

      // Get the handfuls of cubes revealed. For each string like "1 red, 2 gree, 6 blue",
      // parse that string into a [CubeSet] and set `handfules` to that list of cubesets.
      val handfuls = cubePart.split(";").map(CubeSet::fromString)

      return Game(id, handfuls)
    }
  }

  /**
   * Indicate whether this [Game] is valid
   *
   * A valid game is one where every revealed handful of cubes can be contained within the [max]
   * cubeset.
   *
   * @param max A [CubeSet] to compare against
   * @return A Boolean indicating whether the [Game] is valid.
   */
  fun isValid(max: CubeSet): Boolean {
    return handfuls.all { it.isValid(max) }
  }

  /**
   * Identify the smallest [CubSet] that can contain all [handfuls]
   *
   * Calculate the smallest number of red, green, and blue cubes that could be represented by the
   * handfuls of cubes revealed by the elf.
   *
   * @return The smallest possible [CubeSet]
   */
  fun localMinimum(): CubeSet {
    var red = 0
    var green = 0
    var blue = 0

    // The smallest possible cubeset must contain at least as many red,
    // green, and blue cubes as revealed in any given handful of cubes.
    for (handful in handfuls) {
      red = maxOf(red, handful.red)
      green = maxOf(green, handful.green)
      blue = maxOf(blue, handful.blue)
    }

    return CubeSet(red, green, blue)
  }
}

class Day02(input: List<String>) {

  private val maxCubeSet = CubeSet(12, 13, 14)

  // Parse the input by constructing a Game from each non-empty line.
  private val parsed = input.filter { !it.isEmpty() }.map(Game::fromString)

  // In part 1, we compare our games to a static maximum cubeset, determine
  // which games are valid, and return the sum of their game ids.
  fun solvePart1(): Int = parsed.filter { it.isValid(maxCubeSet) }.sumOf { it.id }

  // In part 2, we identify the smallest valid set of cubes for each game, calculate
  // the 'power' of that minimum set, then return the sum of all all the power from
  // all the games.
  fun solvePart2(): Int = parsed.sumOf { it.localMinimum().power() }
}
