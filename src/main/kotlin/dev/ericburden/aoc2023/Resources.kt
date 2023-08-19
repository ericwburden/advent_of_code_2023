package dev.ericburden.aoc2023

import java.io.File
import java.net.URI

internal object Resources {
  fun resourceAsText(fileName: String): String = File(fileName.toURI()).readText()

  fun resourceAsLines(fileName: String): List<String> = File(fileName.toURI()).readLines()

  fun resourceAsLineChunks(fileName: String, delimiter: String = "\n\n"): List<List<String>> =
      resourceAsText(fileName).trim().split(delimiter).map { it.lines().toList() }

  fun resourceAsString(fileName: String, delimiter: String = ""): String =
      resourceAsLines(fileName).reduce { a, b -> "$a$delimiter$b" }

  fun resourceAsListOfInt(fileName: String): List<Int> =
      resourceAsLines(fileName).map { it.toInt() }

  fun resourceAsListOfLong(fileName: String): List<Long> =
      resourceAsLines(fileName).map { it.toLong() }

  private fun String.toURI(): URI =
      Resources.javaClass.classLoader.getResource(this)?.toURI()
          ?: throw IllegalArgumentException("Cannot find Resource: $this")
}
