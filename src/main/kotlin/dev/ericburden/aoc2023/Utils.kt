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
}