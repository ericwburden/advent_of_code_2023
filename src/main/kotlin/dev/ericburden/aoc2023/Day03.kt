package dev.ericburden.aoc2023

import kotlin.collections.mutableMapOf

sealed class SchematicItem {
  data class Number(val id: Int, val number: Int) : SchematicItem()
  data class Symbol(val char: Char) : SchematicItem()
}

typealias Coordinate = Pair<Int, Int>

data class EngineSchematic(
    val map: MutableMap<Coordinate, SchematicItem>,
    val numbers: MutableMap<SchematicItem.Number, List<Coordinate>>,
    val symbols: List<Pair<SchematicItem.Symbol, Coordinate>>
) {
  companion object {
    fun fromString(input: String): EngineSchematic {
      val map = mutableMapOf<Coordinate, SchematicItem>()
      val numbers = mutableMapOf<SchematicItem.Number, List<Coordinate>>()
      val symbols = mutableListOf<Pair<SchematicItem.Symbol, Coordinate>>()

      for ((row, line) in input.trimEnd().split("\n").withIndex()) {
        // Iterate over the line with support for skipping forward when
        // a multi-digit number is encountered
        var col = 0
        while (col < line.length) {
          var current_char = line[col]

          if (current_char == '.') {
            // If the current character is a period, skip this column
            col += 1
          } else if (current_char.isDigit()) {
            // If the current character is numeric digit, parse the number,
            // add it to the [map], and skip ahead to the end of the
            // number string
            var buffer = StringBuilder()
            val first_col = col
            val id = (row * line.length) + col // The ID for this number
            while (current_char.isDigit()) {
              buffer.append(current_char)
              col += 1
              if (col >= line.length) break // Safety measure
              current_char = line[col]
            }

            // No need to check the string, we guaranteed it only contains digits
            val partNumber = SchematicItem.Number(id, buffer.toString().toInt())
            var numberCoords = mutableListOf<Coordinate>()
            for (inner_col in first_col until col) {
              val coordinate = Coordinate(row, inner_col)
              map.put(coordinate, partNumber)
              numberCoords.add(coordinate)
            }
            numbers.put(partNumber, numberCoords)
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

  private val schematic = EngineSchematic.Companion.fromString(input)

  fun solvePart1(): Int = schematic.partNumbers().sumOf { it.number }

  fun solvePart2(): Int = schematic.gearRatios().sum()
}
