package dev.ericburden.aoc2023

// This is a bit of a complicated type. Essentially, we are representing one
// translation from one resource type to another. For example, to the mapping of
// the 'seed-to-soil' group from the example:
//
//     seed-to-soil map:
//     50 98 2
//     52 50 48
//
// is represented as ("seed", ("soil", [(98..99, -48), (50..97, 2)])). This makes
// it a bit easier to use in the code below, as the list of them can be easily
// converted to a HashMap.
typealias ResourceRangeTranslationGroup = Pair<String, Pair<String, List<Pair<LongRange, Long>>>>

/**
 * This class represents the contents of the elf's Almanac
 *
 * @property seeds A list of the seed numbers in ("seed", <seed number>) format.
 * @property mappings A map used to determine what type of resource is required.
 */
data class Almanac(
    val seeds: List<Pair<String, Long>>,
    val mappings: Map<String, Pair<String, List<Pair<LongRange, Long>>>>
) {
  companion object {
    /**
     * Parse the input from a list of input chunks.
     *
     * @param chunks A list of line chunks from the input.
     * @return The parsed Almanac.
     */
    fun fromInputChunks(chunks: List<List<String>>): Almanac {
      // This bit isn't too bad. We parse the first line into  a list of
      // ("seed", <number>) pairs. This is so, when we attempt to go from one
      // resource to another later on, we've tagged this number with the type
      // of resource it represents.
      val seeds =
          chunks.first().first().removePrefix("seeds: ").split("\\s+".toRegex()).map {
            val seedNumber =
                it.toLongOrNull() ?: throw IllegalArgumentException("$it cannot be parsed to Long!")
            "seed" to seedNumber
          }

      // Parsing the "x-to-y map" chunks was a bit involved, so I've moved
      // that logic to its own function.
      val mappings = chunks.drop(1).filter { it.isNotEmpty() }.map { parseChunk(it) }.toMap()

      return Almanac(seeds, mappings)
    }

    /**
     * Parse one chunk of the input file
     *
     * This is where we're using the gnarly type alias defined at the top. The idea is to produce a
     * list that can be converted to a Map that can be used to look up, for each resource, what
     * numbers for the next resource it needs.catch
     *
     * @param lines The input lines for one chunk of the input file.
     * @return An object that can become an entry in the [Almanac.mappings] map.
     */
    fun parseChunk(lines: List<String>): ResourceRangeTranslationGroup {
      // Get which resource types we're dealing with.
      val (sourceType, targetType) = lines.first().removeSuffix(" map:").split("-to-")

      // For each subsequent line in the chunk, we extract two pieces of information:
      // 1. The range of the source resource
      // 2. The magnitude of the offset of the destination resource range from the source
      //    range. This works because the source range and destination ranges are
      //    constrained to be the same size by the input.
      val rangeMappings =
          lines
              .filter { it.isNotEmpty() }
              .drop(1)
              .map { line ->
                val (destinationRangeStart, sourceRangeStart, rangeLength) =
                    line.split("\\s+".toRegex()).map {
                      // Yes, I'm being paranoid.
                      it.toLongOrNull()
                          ?: throw IllegalArgumentException("$it cannot be parsed to Long!")
                    }
                val sourceRange = sourceRangeStart until (sourceRangeStart + rangeLength)
                val rangeShift = destinationRangeStart - sourceRangeStart
                sourceRange to rangeShift // Pair<LongRange, Long>
              }
              .sortedBy { it.first.start }
      return sourceType to (targetType to rangeMappings)
    }
  }

  /**
   * Given a resource, traces it's requirements all the way to a location
   *
   * This is why I'm tagging the resource numbers like ("seed", 1). So I can pass that pair to this
   * function and have it recursively search through the [Almanac] until the corresponding location
   * is found.
   *
   * @param resource A pair of ("resource type", <number>) to find the location for.
   * @return A pair of ("location", <location number>)
   */
  fun resourceToLocation(resource: Pair<String, Long>): Pair<String, Long> {
    val (type, id) = resource
    if (type == "location") return resource

    // Get the mappings of source type ranges to destination type ranges
    val (destinationType, possibleDestinations) =
        mappings.get(type)
            ?: throw IllegalArgumentException("Could not find a destination for $type.")

    // If there is a matching mapping, then map the source number to the
    // destination number and return it.
    try {
      val destination =
          possibleDestinations
              .filter { (range, _) -> id in range }
              .map { (_, offset) -> id + offset }
              .single()
      return this.resourceToLocation(destinationType to destination)
    } catch (e: Exception) {
      // If there is no mapping, then we know that the destination number
      // is the same as the source number.
      return this.resourceToLocation(destinationType to id)
    }
  }

  /**
   * Given a range of resource numbers, return all the possible location ranges
   *
   * In Part 2, the numbers are just too big for us to map each seed to its location individually.
   * Instead, we need to map the resources by the entire range. The catch here is that the input
   * range won't always slot neatly inside an output range and may instead encompass several ranges.
   * For that reason, we return a list of ("resource type", <range>) pairs.
   *
   * @param resourceRange A pair of ("resource type", <range>) to find location ranges for.
   * @return A list of all the location ranges the input range maps to.
   */
  fun resourceRangeToLocationRanges(
      resourceRange: Pair<String, LongRange>
  ): List<Pair<String, LongRange>> {
    val (type, sourceRange) = resourceRange
    if (type == "location") return listOf(resourceRange) // Base case

    val (destinationType, possibleDestinations) =
        mappings.get(type)
            ?: throw IllegalArgumentException("Could not find a destination for $type.")
    // Still paranoid

    // This will house all the destination ranges we find.
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

    // Now with our list of destination ranges, we need to recursively search
    // for the ranges of the final locations.
    return destinationRanges
        .map { this.resourceRangeToLocationRanges(destinationType to it) }
        .flatten()
  }
}

class Day05(input: List<List<String>>) {

  // And this was the easy part...
  private val parsed = Almanac.fromInputChunks(input)

  // In part one, we have a few seeds to follow all the way to their
  // ideal planting locations. Easy enough.
  fun solvePart1(): Long =
      parsed.seeds.map { parsed.resourceToLocation(it) }.minOf { (_, id) -> id }

  // In part two, we have a _tremendous_ number of seeds to track down.
  // Instead, we need to keep track of ranges of numbers to finish in a
  // reasonable amount of time.
  fun solvePart2(): Long {
    // Convert the seed numbers to ranges as specified in the puzzle text.
    val seedRanges =
        parsed.seeds.chunked(2) { (first, second) ->
          val (_, rangeStart) = first
          val (_, rangeLength) = second
          "seed" to (rangeStart until (rangeStart + rangeLength))
        }

    // For all the possible location ranges, find the smallest possible location.
    return seedRanges
        .map { seedRange -> parsed.resourceRangeToLocationRanges(seedRange) }
        .flatten()
        .minOf { (_, range) -> range.start }
  }
}
