package dev.ericburden.aoc2023

import kotlin.collections.mutableMapOf

// There are two possible items on the schematic, either a number that
// may span multiple grid spaces, or a non-numeric character called a
// 'symbol'. Each number needs to be uniquely identified, so we can
// numbers with duplicate values in the schematic representation below.
sealed class SchematicItem {
  data class Number(val id: Int, val number: Int) : SchematicItem()
  data class Symbol(val char: Char) : SchematicItem()
}

typealias Coordinate = Pair<Int, Int>

operator fun Coordinate.plus(other: Pair<Int, Int>): Coordinate {
  return Coordinate(first + other.first, second + other.second)
}

/**
 * This class represents the engine schematic
 *
 * @property map A mapping of coordinates to schematic items
 * @property numbers A list allowing for easy iteration over numeric schematic items
 * @property symbols A list allowing for easy iteration over symbol schematic items
 */
data class EngineSchematic
private constructor(
    val map: MutableMap<Coordinate, SchematicItem>,
    val numbers: List<Pair<SchematicItem.Number, List<Coordinate>>>,
    val symbols: List<Pair<SchematicItem.Symbol, Coordinate>>
) {
  companion object {
    /**
     * This factory function serves as the constructor
     *
     * Parses an [EngineSchematic] from the input string
     *
     * @param input The input string
     * @return An [EngineSchematic] represented by the input string
     */
    fun fromString(input: String): EngineSchematic {
      val map = mutableMapOf<Coordinate, SchematicItem>()
      val numbers = mutableListOf<Pair<SchematicItem.Number, List<Coordinate>>>()
      val symbols = mutableListOf<Pair<SchematicItem.Symbol, Coordinate>>()

      // Iterate over the lines with support for skipping forward when
      // a multi-digit number is encountered.
      for ((row, line) in input.trimEnd().split("\n").withIndex()) {
        var col = 0
        while (col < line.length) {
          when (line[col]) {
            '.' -> col += 1 // Skip periods
            in '0'..'9' -> {
              // If the current character is a numeric digit, parse the number,
              // add it to the [map], and skip ahead to the end of the
              // number string
              var buffer = StringBuilder()
              val first_col = col
              val id = (row * line.length) + col // The ID for this number
              while (col < line.length && line[col].isDigit()) {
                buffer.append(line[col])
                col += 1
              }

              // No need to check the string, we guaranteed it only contains digits
              val partNumber = SchematicItem.Number(id, buffer.toString().toInt())

              // We'll also keep a list of numbers and the coordinates associated with
              // the digits of that number.
              var numberCoords = mutableListOf<Coordinate>()
              for (digit_col in first_col until col) {
                val coordinate = Coordinate(row, digit_col)
                map.put(coordinate, partNumber)
                numberCoords.add(coordinate)
              }

              numbers.add(partNumber to numberCoords)
            }
            else -> {
              // Otherwise, we're dealing with a symbol. A symbol gets added to the
              // map and to the list of symbols and their coordinates.
              val symbol = SchematicItem.Symbol(line[col])
              val coordinate = Coordinate(row, col)
              map.put(coordinate, symbol)
              symbols.add(symbol to coordinate)
              col += 1
            }
          }
        }
      }

      return EngineSchematic(map, numbers, symbols)
    }
  }

  /**
   * Extract the true part numbers from the schematic
   *
   * Just because there's a number in the schematic doesn't mean it's a _part_ number. Which is
   * weird. A real part number is adjacent to a symbol. This function checks around each number for
   * a symbol and returns all [SchematicItem.Number]s with an adjacent symbol.
   *
   * @return A list of [SchematicItem.Number] objects
   */
  fun partNumbers(): List<SchematicItem.Number> {
    val offsets = listOf(-1 to -1, -1 to 0, -1 to 1, 0 to -1, 0 to 1, 1 to -1, 1 to 0, 1 to 1)
    val partNumbers = mutableListOf<SchematicItem.Number>()

    // For each number/list of coordinates pair in the [numbers] list,
    // check all around the number for a symbol. Technically, we're
    // re-checking several indices for numbers with multiple digits.
    // It's possible to keep track of coordinates checked, but I'm not
    // convinced it's worth the extra effort.
    for ((number, coordinates) in numbers) {
      number@ for (coordinate in coordinates) {
        for (offset in offsets) {
          // If there's a value at the neighbor coordinate and that value is
          // a [SchematicItem.Symbol], then this number really is
          // a part number! Add it to our list and move on.
          val maybeSymbol = map.get(coordinate + offset) as? SchematicItem.Symbol
          if (maybeSymbol != null) {
            partNumbers.add(number)
            break@number
          }
        }
      }
    }

    return partNumbers
  }

  /**
   * Calculate and return all gear ratios in the schematic
   *
   * Some symbols are gears. Each gear is associated with two numbers, and the 'gear ratio' is the
   * product of those two numbers.
   *
   * @return A list of all gear ratios represeted on the schematic
   */
  fun gearRatios(): List<Int> {
    val offsets = listOf(-1 to -1, -1 to 0, -1 to 1, 0 to -1, 0 to 1, 1 to -1, 1 to 0, 1 to 1)
    val ratios = mutableListOf<Int>()

    // For each symbol/coordinate pair in the symbols list, check around that
    // symbol for exactly two numbers. If two numbers are found, add their
    // product to the gear ratio list.
    symbol@ for ((symbol, coordinate) in symbols) {
      if (symbol.char != '*') continue // skip it, not a gear

      // We _do_ need to keep track of which numbers we've already seen,
      // since each number can have multiple digits and the symbol may
      // be adjacent to more than one of them.
      var numbersSeen = mutableSetOf<Int>()
      var numbersAdded = 0
      var ratio = 1

      // Check all around the symbol...
      for (offset in offsets) {
        // If the neighboring coordinate contains a value that can be
        // coerced to a [SchematicItem.Number], we haven't added that
        // number to the gear ratio yet, and it's not the third (or
        // greater) number we've found, then we add (by multiplying) that
        // number to the gear ratio and add the number's ID to the set
        // of seen numbers.
        val maybeNumber = map.get(coordinate + offset) as? SchematicItem.Number
        if (maybeNumber != null && !numbersSeen.contains(maybeNumber.id)) {
          if (numbersAdded >= 2) continue@symbol // more than two numbers
          ratio *= maybeNumber.number
          numbersSeen.add(maybeNumber.id)
          numbersAdded += 1
        }
      }

      // Only add the raio to ratios if we saw two numbers
      if (numbersAdded == 2) ratios.add(ratio)
    }

    return ratios
  }
}

class Day03(input: String) {

  // The real work is done here
  private val schematic = EngineSchematic.fromString(input)

  // In part one, we figure out which numbers really represent parts
  // and return the sum of all of them.
  fun solvePart1(): Int = schematic.partNumbers().sumOf { it.number }

  // In part two, we find the gears (which are a bit trickier) and return
  // the sum of all the gear ratios.
  fun solvePart2(): Int = schematic.gearRatios().sum()
}
