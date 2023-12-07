package dev.ericburden.aoc2023

// I made my own!
import dev.ericburden.aoc2023.Utils.pow

/**
 * This enumeration represents the various types of Camel Cards
 *
 * @param strength The strength of the card when it comes to sorting.
 */
enum class CamelCard(val strength: Int) {
    ACE(14),
    KING(13),
    QUEEN(12),
    JACK(11),
    TEN(10),
    NINE(9),
    EIGHT(8),
    SEVEN(7),
    SIX(6),
    FIVE(5),
    FOUR(4),
    THREE(3),
    TWO(2),
    JOKER(1); // For Part Two

    companion object {
        /**
         * Parse a [CamelCard] from a [Char]
         *
         * @param char The Char value to parse.
         * @return The corresponding [Camel Card].
         */
        fun fromChar(char: Char): CamelCard {
            return when (char) {
                'A' -> ACE
                'K' -> KING
                'Q' -> QUEEN
                'J' -> JACK
                'T' -> TEN
                '9' -> NINE
                '8' -> EIGHT
                '7' -> SEVEN
                '6' -> SIX
                '5' -> FIVE
                '4' -> FOUR
                '3' -> THREE
                '2' -> TWO
                else -> throw IllegalArgumentException("$char does not represent a CamelCard!")
            }
        }
    }
}

/**
 * This enumeration represents the various kinds of hands
 *
 * @param strength The strength of this kind of hand when it comes to sorting.
 */
enum class HandKind(val strength: Int) {
    FiveOfAKind(700_000_000),
    FourOfAKind(600_000_000),
    FullHouse(500_000_000),
    ThreeOfAKind(400_000_000),
    TwoPair(300_000_000),
    OnePair(200_000_000),
    HighCard(100_000_000);

    companion object {
        /**
         * Classify a hand of [CamelCard]s into a [HandKind]
         *
         * Check a list of camel cards for special cases, like four of a kind, and return the
         * highest-strength classification possible for that list of cards. The classifications are
         * the variants of [HandKind].
         *
         * @param cards The list of cards to be classified.
         * @return The [HandKind] representing the classification of the cards.
         */
        fun classify(cards: List<CamelCard>): HandKind {
            var counts = MutableList(15) { 0 }
            cards.forEach { card -> counts[card.strength] += 1 }

            val maxCount = counts.max()
            val pairCount = counts.filter { it == 2 }.count()
            return when (maxCount) {
                5 -> FiveOfAKind
                4 -> FourOfAKind
                3 -> if (pairCount > 0) FullHouse else ThreeOfAKind
                2 -> if (pairCount == 2) TwoPair else OnePair
                else -> HighCard
            }
        }

        /**
         * Classify a hand of cards containing Jokers
         *
         * This could have been included in the `HandKind.classify` function, but I separated it out
         * because it's only used in part 2. For hands that contain Jokers (and treat them as
         * wildcards), we determine what the hand _would be_ without the jokers, then promote the
         * classification one step for each Joker found.
         *
         * @param cards A list of cards where Joker are wild.
         * @return The [HandKind] representing the classification of the cards.
         */
        fun classifyWithJokers(cards: List<CamelCard>): HandKind {
            // What kind of hand would it be without the Jokers?
            var handKind = classify(cards.filter { it != CamelCard.JOKER })

            // For every Joker in the hand, increase the value of the hand by one step,
            // always choosing the next possible kind with the highest strength possible.
            val jokerCount = cards.filter { it == CamelCard.JOKER }.count()
            repeat(jokerCount) {
                handKind =
                        when (handKind) {
                            FiveOfAKind -> FiveOfAKind // Best we can do
                            FourOfAKind -> FiveOfAKind
                            FullHouse -> FourOfAKind
                            ThreeOfAKind -> FourOfAKind
                            TwoPair -> FullHouse
                            OnePair -> ThreeOfAKind
                            HighCard -> OnePair
                        }
            }

            return handKind
        }
    }
}

/**
 * This class represents a hand of [CamelCard]s
 *
 * Each hand contains five cards, and the strength of that hand can be derived from the identity and
 * order of those cards. Each hand also includes the accompanying bid, used to calculate the final
 * result for both parts of today's puzzle.
 *
 * @param cards The list of [CamelCard]s in hand.
 * @param bid The value of the bid.
 * @param kind The [HandKind] classification of the hand.
 * @param strength The strength of this hand when it comes to sorting.
 */
data class CamelCardHand
private constructor(val cards: List<CamelCard>, val bid: Int, val kind: HandKind) {
    companion object {
        /**
         * Parse a [CamelCardHand] from a line of the input string.
         *
         * Each line includes the cards (as a string where each character represents a card) and a
         * bid.
         *
         * @param string The input line to be parsed.
         * @return A hand of Camel Cards.
         */
        fun fromString(string: String): CamelCardHand {
            val (cardString, bidString) = string.split(" ")
            val cards = cardString.map { CamelCard.fromChar(it) }
            val bid = bidString.toInt()
            val kind = HandKind.classify(cards)
            return CamelCardHand(cards, bid, kind)
        }
    }

    // The strength of a hand is derived from the individual cards in it, their order,
    // and what kind of hand is formed by those cards.
    val strength: Int
        get() {
            val maxCardIdx = cards.size - 1
            val cardStrength =
                    cards.zip(maxCardIdx downTo 0).sumOf { (card, exp) ->
                        card.strength * 15.pow(exp)
                    }
            return cardStrength + kind.strength
        }

    fun replaceJacksWithJokers(): CamelCardHand {
        val cards = cards.map { if (it == CamelCard.JACK) CamelCard.JOKER else it } // maybe STL?
        val kind = HandKind.classifyWithJokers(cards)
        return CamelCardHand(cards, bid, kind)
    }
}

class Day07(input: List<String>) {

    private val parsed = input.filter { it.isNotEmpty() }.map { CamelCardHand.fromString(it) }

    fun solvePart1(): Int =
            parsed.sortedBy { it.strength }.withIndex().sumOf { (idx, hand) ->
                (idx + 1) * hand.bid
            }

    fun solvePart2(): Int =
            parsed.map { it.replaceJacksWithJokers() }.sortedBy { it.strength }.withIndex().sumOf {
                    (idx, hand) ->
                (idx + 1) * hand.bid
            }
}
