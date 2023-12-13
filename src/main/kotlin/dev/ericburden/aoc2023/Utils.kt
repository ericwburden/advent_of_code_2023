package dev.ericburden.aoc2023

import kotlin.math.abs

internal object Utils {
    /**
     * Raise an integer to the power of [exponent]
     * 
     * @param exponent The power to raise this integer to.
     * @return The original integer raised to the power of [exponent].
     */
    fun Int.pow(exponent: Int): Int {
        if (exponent == 0) {
            return 1
        }
    
        var result = 1
        var base = this
        var exp = exponent
    
        // Handle negative exponents
        if (exp < 0) {
            base = 1 / base
            exp = -exp
        }
    
        while (exp > 0) {
            if (exp % 2 == 1) {
                result *= base
            }
            base *= base
            exp /= 2
        }
    
        return result
    }

    /**
     * Calculate the greatest common divisor between two numbers.
     *
     * @param a The first number.
     * @param b The second number.
     * @return The greatest common divisor.
     */
    fun gcd(a: Long, b: Long): Long = if (b == 0L) abs(a) else gcd(b, a % b)

    /**
     * Calculate the least common multiple between two numbers.
     *
     * @param a The first number.
     * @param b The second number.
     * @return The least common multiple.
     */
    fun lcm(a: Long, b: Long): Long = abs(a * b) / gcd(a, b)

    /**
     * Calculate the greatest common divisor amongst a list of numbers.
     *
     * This function extends the functionality of a [List<Long>] by adding
     * a `gcd` function that calculates the greatest common divisor amongst
     * all numbers.
     *
     * @return The greatest common divisor amongst the listed numbers.
     */
    fun List<Long>.gcd(): Long {
        require(this.isNotEmpty()) { "List must not be empty" }
        return this.reduce { acc, number -> gcd(acc, number) }
    }

    /**
     * Calculate the least common multiple amongst a list of numbers.
     *
     * This function extends the functionality of a [List<Long>] by adding
     * a `lcm` function that calculates the least common multiple amongst
     * all numbers.
     *
     * @return The least common multiple amongst the listed numbers.
     */
    fun List<Long>.lcm(): Long {
        require(this.isNotEmpty()) { "List must not be empty" }
        return this.reduce { acc, number -> lcm(acc, number) }
    }

    /**
     * Return an iterator that outputs the contents of the list infinitely.
     *
     * This function extends the functionality of a [List<T>] by adding
     * a 'repeating` function that returns an iterator that returns the
     * values of the list, in order, starting over when the list is
     * exhausted.
     *
     * @return A RepeatingList over the list.
     */
    fun<T> List<T>.repeating(): Iterator<T> = RepeatingList(this)

    /**
     * This class represents an iterator that repeats the values in a list.
     *
     * @property list The list to repeat.
     */
    class RepeatingList<T>(private val list: List<T>) : Iterator<T> {
        private var currentIndex = 0

        override fun hasNext(): Boolean {
            return list.isNotEmpty()
        }

        override fun next(): T {
            if (list.isEmpty()) {
                throw NoSuchElementException("List is empty")
            }

            val element = list[currentIndex]
            currentIndex = (currentIndex + 1) % list.size
            return element
        }
    }


    /**
     * Transposes a rectangular matrix
     *
     * This function extends the functionality of a [List<List<T>>] by adding a
     * `transpose` function which returns a transposed copy of the original
     * list of lists. All the sub-lists must be the same length. For example:
     *
     *  [[1, 2, 3],             [[1, 4, 7],
     *   [4, 5, 6],     ->       [2, 5, 8],
     *   [7, 8, 9]]              [3, 6, 9]]
     *
     *  @return The transposed list of lists.
     */
    fun <T> List<List<T>>.transpose(): List<List<T>> {
        if (isEmpty() || this.any { it.size != this[0].size }) {
            throw IllegalArgumentException("Inner lists must have the same size")
        }

        return List(this[0].size) { col ->
            List(size) { row ->
                this[row][col]
            }
        }
    }

    /**
     * Converts a String to an Integer by interpreting it as a binary value
     *
     * This function extends the functionality of [String] by adding a `toBinary`
     * function that interprets the characters in the string as 1's and 0's then
     * converts it to the [Int] represented by the binary string.
     *
     * @param one The character to interpret as 1.
     * @param zero The character to interpret as 0.
     * @return The String as binary and converted to an Int.
     */
    fun String.toBinary(one: Char = '#', zero: Char = '.'): Int =
        toList().joinToString("") { char ->
            when (char) {
                one -> "1"
                zero -> "0"
                else -> {
                    throw Exception("$char has not be specified as either '1' or '0'!")
                }
            }
        }.toInt(radix = 2)

    /**
     * Returns the difference of the count of the set bits between two [Int]s
     *
     * @param other The other integer to compare to.
     * @return The number of differing set bits between two integers.
     */
    infix fun Int.bitdiff(other: Int): Int {
        return Integer.bitCount(this xor other)
    }
}