package dev.ericburden.aoc2023

import java.io.File
import java.net.URI

internal object Resources {
  fun resourceAsText(fileName: String): String = File(fileName.toURI()).readText()

  fun resourceAsLines(fileName: String): List<String> = File(fileName.toURI()).readLines()

  fun resourceAsLineChunks(fileName: String, delimiter: String = "\n\n"): List<List<String>> =
      resourceAsText(fileName).split(delimiter).map { it.lines().takeWhile { line -> line.isNotEmpty() }.toList() }

  fun resourceAsString(fileName: String, delimiter: String = ""): String =
      resourceAsLines(fileName).reduce { a, b -> "$a$delimiter$b" }

  fun resourceAsListOfInt(fileName: String): List<Int> =
      resourceAsLines(fileName).map { it.toInt() }

  fun resourceAsListOfLong(fileName: String): List<Long> =
      resourceAsLines(fileName).map { it.toLong() }

    fun resourceAsGridOfChar(fileName: String): List<List<Char>> =
        resourceAsLines(fileName).map { it.toList() }

    fun resourceAsGridOfDigits(fileName: String): List<List<Int>> =
        resourceAsLines(fileName).map { it.map { char -> char.digitToInt() } }

  private fun String.toURI(): URI =
      Resources.javaClass.classLoader.getResource(this)?.toURI()
          ?: throw IllegalArgumentException("Cannot find Resource: $this")
}
