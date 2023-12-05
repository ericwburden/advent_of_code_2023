package dev.ericburden.aoc2023

data class Input(
    val seeds: List<Pair<String, Long>>,
    val mappings: Map<String, Pair<String, List<Pair<LongRange, Long>>>>
) {
  companion object {
    fun fromInputChunks(chunks: List<List<String>>): Input {
      val seeds =
          chunks.first().first().removePrefix("seeds: ").split("\\s+".toRegex()).map {
            val seedNumber =
                it.toLongOrNull() ?: throw IllegalArgumentException("$it cannot be parsed to Long!")
            "seed" to seedNumber
          }

      val mappings =
          chunks
              .drop(1)
              .filter { it.isNotEmpty() }
              .map { lines ->
                val (sourceType, targetType) = lines.first().removeSuffix(" map:").split("-to-")
                val rangeMappings =
                    lines
                        .filter { it.isNotEmpty() }
                        .drop(1)
                        .map { line ->
                          val (destinationRangeStart, sourceRangeStart, rangeLength) =
                              line.split("\\s+".toRegex()).map {
                                it.toLongOrNull()
                                    ?: throw IllegalArgumentException(
                                        "$it cannot be parsed to Long!"
                                    )
                              }
                          val sourceRange = sourceRangeStart until (sourceRangeStart + rangeLength)
                          val rangeShift = destinationRangeStart - sourceRangeStart
                          sourceRange to rangeShift // Pair<LongRange, Long>
                        }
                        .sortedBy { it.first.start }
                sourceType to (targetType to rangeMappings)
              }
              .toMap()

      return Input(seeds, mappings)
    }
  }

  fun resourceToLocation(resource: Pair<String, Long>): Pair<String, Long> {
    val (type, id) = resource
    if (type == "location") return resource

    val (destinationType, possibleDestinations) =
        mappings.get(type)
            ?: throw IllegalArgumentException("Could not find a destination for $type.")

    try {
      val destination =
          possibleDestinations
              .filter { (range, _) -> id in range }
              .map { (_, offset) -> id + offset }
              .single()
      return this.resourceToLocation(destinationType to destination)
    } catch (e: Exception) {
      return this.resourceToLocation(destinationType to id)
    }
  }

  fun resourceRangeToLocationRanges(
      resourceRange: Pair<String, LongRange>
  ): List<Pair<String, LongRange>> {
    val (type, sourceRange) = resourceRange
    if (type == "location") return listOf(resourceRange)

    val (destinationType, possibleDestinations) =
        mappings.get(type)
            ?: throw IllegalArgumentException("Could not find a destination for $type.")

    var destinationRanges = mutableListOf<LongRange>()

    // If the `sourceRange` starts before the first `possibleDestinationRange`, then we need
    // to add that non-overlapping part to the destinations as-is
    val (firstPossibleDestinationRange, _) = possibleDestinations.first()
    if (sourceRange.start < firstPossibleDestinationRange.start) {
      val prefixRangeEnd = minOf(sourceRange.endInclusive + 1, firstPossibleDestinationRange.start)
      val prefixRange = sourceRange.start until prefixRangeEnd
      destinationRanges.add(prefixRange)
    }

    // Now, check over all the `possibleDestinations` and, for every destinationRange
    // that overlaps `range`, apply the offset to the overlapping portion of `range`
    // and add it to the destinations
    for ((destinationRange, destinationOffset) in
        possibleDestinations.sortedBy { it.first.start }) {
      if (sourceRange.start <= destinationRange.endInclusive &&
              destinationRange.start <= sourceRange.endInclusive
      ) {
        val overlappingRangeStart = maxOf(sourceRange.start, destinationRange.start)
        val overlappingRangeEnd = minOf(sourceRange.endInclusive, destinationRange.endInclusive) + 1
        val overlappingRange =
            (overlappingRangeStart + destinationOffset) until
                (overlappingRangeEnd + destinationOffset)
        destinationRanges.add(overlappingRange)
      }
    }

    // Finally, if `sourceRange` extends past the end of the last `destinationRange`,
    // then we need to add that non-overlapping part to the destinations as-is.
    var (lastPossibleDestinationRange, _) = possibleDestinations.last()
    if (sourceRange.endInclusive > lastPossibleDestinationRange.endInclusive) {
      val suffixRangeStart = maxOf(sourceRange.start, lastPossibleDestinationRange.endInclusive + 1)
      val suffixRange = suffixRangeStart until sourceRange.endInclusive + 1
      destinationRanges.add(suffixRange)
    }

    return destinationRanges
        .map { this.resourceRangeToLocationRanges(destinationType to it) }
        .flatten()
  }
}

class Day05(input: List<List<String>>) {

  private val parsed = Input.fromInputChunks(input)

  fun solvePart1(): Long =
      parsed.seeds.map { parsed.resourceToLocation(it) }.minOf { (_, id) -> id }

  fun solvePart2(): Long {
    val seedRanges =
        parsed.seeds.chunked(2) { (first, second) ->
          val (_, rangeStart) = first
          val (_, rangeLength) = second
          "seed" to (rangeStart until (rangeStart + rangeLength))
        }

    return seedRanges.map { parsed.resourceRangeToLocationRanges(it) }.flatten().minOf { (_, range)
      ->
      range.start
    }
  }
}
