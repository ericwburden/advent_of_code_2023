package dev.ericburden.aoc2023

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
}