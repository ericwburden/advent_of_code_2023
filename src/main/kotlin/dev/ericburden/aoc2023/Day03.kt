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
          if (line[col] == '.') {
            // If the current character is a period, skip this column
            col += 1
          } else if (line[col].isDigit()) {
            // If the current character is numeric digit, parse the number,
            // add it to the [map], and skip ahead to the end of the
            // number string
            var buffer = StringBuilder()
            val first_col = col
            val id = (row * line.length) + col // The ID for this number
            while (line[col].isDigit()) {
              buffer.append(line[col])
              col += 1
              if (col >= line.length) break // Safety measure
            }

            // No need to check the string, we guaranteed it only contains digits
            val partNumber = SchematicItem.Number(id, buffer.toString().toInt())
            var numberCoords = mutableListOf<Coordinate>()
            for (inner_col in first_col until col) {
              val coordinate = Coordinate(row, inner_col)
              map.put(coordinate, partNumber)
              numberCoords.add(coordinate)
            }
            numbers.add(partNumber to numberCoords)
          } else {
            // Otherwise, we're dealing with a symbol.
            val symbol = SchematicItem.Symbol(line[col])
            val coordinate = Coordinate(row, col)
            map.put(coordinate, symbol)
            symbols.add(symbol to coordinate)
            col += 1
          }
        }
      }

      return EngineSchematic(map, numbers, symbols)
    }
  }

  fun partNumbers(): List<SchematicItem.Number> {
    val offsets = listOf(-1 to -1, -1 to 0, -1 to 1, 0 to -1, 0 to 1, 1 to -1, 1 to 0, 1 to 1)
    val partNumbers = mutableListOf<SchematicItem.Number>()

    for ((number, coordinates) in numbers) {
      number@ for (coordinate in coordinates) {
        for (offset in offsets) {
          val row = coordinate.first + offset.first
          val col = coordinate.second + offset.second
          val checkAt = Coordinate(row, col)

          val maybeSymbol = map.get(checkAt) as? SchematicItem.Symbol
          if (maybeSymbol != null) {
            partNumbers.add(number)
            break@number
          }
        }
      }
    }

    return partNumbers
  }

  fun gearRatios(): List<Int> {
    val offsets = listOf(-1 to -1, -1 to 0, -1 to 1, 0 to -1, 0 to 1, 1 to -1, 1 to 0, 1 to 1)
    val ratios = mutableListOf<Int>()

    symbol@ for ((symbol, coordinate) in symbols) {
      if (symbol.char != '*') continue // skip it, not a gear
      var ratio = 1
      var numbersSeen = mutableSetOf<SchematicItem.Number>()
      var numbersAdded = 0

      for (offset in offsets) {
        val row = coordinate.first + offset.first
        val col = coordinate.second + offset.second
        val checkAt = Coordinate(row, col)
        val maybeNumber = map.get(checkAt) as? SchematicItem.Number

        if (maybeNumber != null && !numbersSeen.contains(maybeNumber)) {
          if (numbersAdded >= 2) continue@symbol // more than two numbers
          ratio *= maybeNumber.number
          numbersSeen.add(maybeNumber)
          numbersAdded += 1
        }
      }

      if (numbersAdded == 2) ratios.add(ratio)
    }

    return ratios
  }
}

class Day03(input: String) {

  private val schematic = EngineSchematic.fromString(input)

  fun solvePart1(): Int = schematic.partNumbers().sumOf { it.number }

  fun solvePart2(): Int = schematic.gearRatios().sum()
}
