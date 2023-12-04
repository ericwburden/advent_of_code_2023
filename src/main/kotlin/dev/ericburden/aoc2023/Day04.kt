package dev.ericburden.aoc2023

/**
 * This class represents one of the elve's scratchcards
 *
 * @property id The id number assigned to this card.
 * @property luckyNumbers The winning scratchard numbers.
 * @property myNumbers The numbers revealed for the elf.
 */
data class Card private constructor(val id: Int, val matches: Int) {
  companion object {
    /**
     * Parses a [Card] from a String
     *
     * @param input The string to be parsed.
     * @return A [Card] represented by the input.
     */
    fun fromString(input: String): Card {
      // Split a string like "Card 1: 1 2 | 2 3 4" into ["Card 1", "1 2", "2 3 4"]
      val parts = input.split("[:|]".toRegex()).map { it.trim() }
      require(parts.size == 3) { "$input cannot be parsed to a [Card]!" }

      val (cardPart, luckyNumbersPart, myNumbersPart) = parts

      // Remove the "Card   " prefix. Note, the example only included one space
      // after "Card", but the real input included multiple for formatting.
      val id =
          cardPart.replaceFirst("Card\\s+".toRegex(), "").toIntOrNull()
              ?: throw IllegalArgumentException("$cardPart cannot be parsed to a card id!")

      // Parse lists of space-separated numeric strings into integers.
      val luckyNumbers =
          luckyNumbersPart.split("\\s+".toRegex()).map {
            it.toIntOrNull() ?: throw IllegalArgumentException("$it is not a number!")
          }
      val myNumbers =
          myNumbersPart.split("\\s+".toRegex()).map {
            it.toIntOrNull() ?: throw IllegalArgumentException("$it is not a number!")
          }

      // Turns out, we don't care about the numbers at all. Just count how many
      // of 'myNumbers' are in `luckyNumbers' and keep track of that.`
      val matches = myNumbers.filter { luckyNumbers.contains(it) }.count()

      return Card(id, matches)
    }
  }

  /**
   * Calculate the score for this card
   *
   * The score is 1 for a single match, doubled for every subsequent match. Mathematically, this
   * works out to 2 ** (matches - 1) for one or more matches.
   *
   * @return The calculated score for this card
   */
  fun score(): Int {
    // Apparently, Kotlin doesn't have a "nice" way to exponentiate integers
    // without first casting it to a double. I don't _want_ to do type
    // conversion for exponents! So, I'm just doubling the value in a loop.
    var score = if (matches == 0) 0 else 1
    if (matches > 1) {
      repeat(matches - 1) { score = score * 2 }
    }

    return score
  }
}

class Day04(input: List<String>) {

  // Except for the trailing empty line, parse all the lines from
  // the input into [Card]s.
  private val parsed = input.filter { it.isNotEmpty() }.map(Card::fromString)

  // In Part 1, we score each card and return the sum of all the scores.
  fun solvePart1(): Int = parsed.sumOf { it.score() }

  // In Part 2, we re-learn that reading is key and calculate the number
  // of cards our elf friend ends up with.
  fun solvePart2(): Int {
    // Each winning card increases the number of copies of subsequent cards
    // by one. The number of matches on the winning card indicates how many
    // of the following cards we "win" new copies of. So, a card with four
    // matches gets us one extra copy of the next four cards. We start
    // by initializing a list of card counts. Since cards are 1-indexed,
    // we make a list a little bigger than we need and just zero the first
    // value (no "Card 0").
    val cardsCounted = MutableList(parsed.size + 1) { 1 }
    cardsCounted[0] = 0

    // Here, we increase subsequent cards by the number of existing cards,
    // so that 10 copies of a winning card with three matches will add ten
    // more copies to each of the following three card types.
    parsed.forEach { card ->
      for (i in 1..card.matches) {
        cardsCounted[card.id + i] += cardsCounted[card.id]
      }
    }

    return cardsCounted.sum()
  }
}
