package dev.ericburden.aoc2023

/**
 * Hashes a string to an integer using the described hashing algorithm
 *
 * This is what the puzzle told me to do!
 *
 * @param str The string to hash.
 * @return An integer derived from hashing the string
 */
fun hash(str: String): Int =
    str.fold(0) { acc, char -> ((acc + char.code) * 17) % 256 }

/**
 * This class represents one of the lenses
 *
 * @property label The label on the lens.
 * @property focalLength The focal length of the lens.
 */
data class Lens(val label: String, val focalLength: Int) {
    /**
     * Indicates whether the given label belongs to this lens
     *
     * @param label The label to check.
     * @return Does the label match the label on this lens?
     */
    fun hasLabel(label: String): Boolean = label == this.label
}

/**
 * Convenience!
 */
fun List<Lens>.boxed(): LensBox = LensBox(this)

/**
 * This class represents a box of lenses, in order
 *
 * @property lenses The lenses in this box, in order.
 */
data class LensBox(val lenses: List<Lens>) {
    companion object {
        /**
         * Create a new, empty [LensBox]
         *
         * @return An empty box for lenses.
         */
        fun new(): LensBox = LensBox(listOf())
    }

    /**
     * Remove a lens from the box by checking labels
     *
     * If there's no lens in the box with the given label, then that just makes
     * your job easier!
     *
     * @param label The label to check.
     * @return A copy of this [LensBox] without the offending lens.
     */
    fun removeByLabel(label: String): LensBox =
        lenses.filter { !it.hasLabel(label) }.boxed()

    /**
     * Insert a lens into the box
     *
     * If there's already a lens with the new lens' label, replace the old
     * lens with the new lens. Otherwise, append the new lens to the end of
     * the list of lenses.
     *
     * @param lens The new lens to add.
     * @return A copy of this [LensBox] with the new lens added.
     */
    fun insert(lens: Lens): LensBox {
        val containsLens = lenses.any { it.hasLabel(lens.label) }
        return if (containsLens) {
            lenses.map { if (it.hasLabel(lens.label)) lens else it }.boxed()
        } else {
            LensBox(lenses + lens)
        }
    }
}

/**
 * This class represents the full array of 256 [LensBox]es
 *
 * @property lensBoxes All the boxes for lenses.
 */
data class LensBoxArray(val lensBoxes: MutableList<LensBox>) {
    companion object {
        /**
         * Create an array of 256 empty boxes for lenses
         *
         * @return An empty [LensBoxArray]
         */
        fun new(): LensBoxArray =
            LensBoxArray(MutableList(256) { LensBox.new() })
    }

    /**
     * Remove the lens with the given label from its box
     *
     * The box to insert the lens into is determined by hashing this label.
     *
     * @param label The label of the lens to remove
     */
    fun remove(label: String) {
        val idx = hash(label)
        lensBoxes[idx] = lensBoxes[idx].removeByLabel(label)
    }

    /**
     * Insert a lens into its box
     *
     * The box to insert the lens into is determined by hashing its label.
     *
     * @param lens The lens to insert
     */
    fun insert(lens: Lens) {
        val idx = hash(lens.label)
        lensBoxes[idx] = lensBoxes[idx].insert(lens)
    }

    /**
     * Calculate the total focusing power of this array of boxes
     *
     * Each lens in a box contributes (box idx + 1) * (slot idx + 1) * focal length.
     *
     * @return The total focusing power of all boxes in this [LensBoxArray].
     */
    fun focusingPower(): Int {
        return lensBoxes.withIndex().sumOf { (idx, box) ->
            val boxNumber = idx + 1
            box.lenses.withIndex().sumOf { (idx, lens) ->
                val slot = idx + 1
                lens.focalLength * slot * boxNumber
            }
        }
    }
}

/**
 * This class represents a parsed operation from the input
 *
 * Turns out the input is a comma-separated list of operations to perform
 * on a [LensBoxArray]. Each operation is either an 'insert' spelled like
 * '<label>=<focal length>' or a 'remove' spelled like '<label>-'. An 'insert'
 * operation indicates that the lens specified should be inserted into the
 * [LensBoxArray] and a 'remove' operation indicates that the lens with the
 * specified label should be removed from the [LensBoxArray].
 */
sealed class Operation {
    // The two variants of [Operation]
    data class InsertLens(val lens: Lens) : Operation()
    data class RemoveLens(val label: String) : Operation()

    companion object {
        /**
         * Parse an [Operation] variant from a portion of the input file
         *
         * @param string The string to parse.
         * @return An operation parsed from the string.
         */
        fun fromString(string: String): Operation {
            return if (string.contains('=')) {
                val (label, focalLengthStr) = string.split('=')
                val focalLength = focalLengthStr.toInt()
                val lens = Lens(label, focalLength)
                InsertLens(lens)
            } else {
                val label = string.removeSuffix("-")
                RemoveLens(label)
            }
        }
    }

    /**
     * Perform this operation on a [LensBoxArray]
     *
     * Performs the operation defined by this variant of [Operation] on
     * the given [LensBoxArray]. Mutates the [LensBoxArray] in place.
     *
     * @param lensBoxArray The [LensBoxArray] to perform this operation on.
     */
    fun perform(lensBoxArray: LensBoxArray) {
        when (this) {
            is InsertLens -> lensBoxArray.insert(this.lens)
            is RemoveLens -> lensBoxArray.remove(this.label)
        }
    }
}

class Day15(input: String) {

    // It feels like there should be more to it than this...
    private val parsed = input.split(",").map { it.replace("\n", "") }.toList()

    // OK so far. Hash all the input strings into an Int and return
    // the summed result.
    fun solvePart1(): Int = parsed.sumOf { hash(it) }

    // Ah-HA! Part 2 was the whole enchilada today, including the "real parsing
    // of the input. Turns out, each of the comma-separated strings is an
    // instruction for an operation to perform on an array of 256 boxes of
    // lenses. For part 2, we _really_ parse the input into operations, create
    // a representation of the empty array of boxes, then simulate the result
    // of performing all the operations on the box array, in order. Finally,
    // we sum the "focusing power" of all the lenses in all the boxes for our
    // answer.
    fun solvePart2(): Int {
        val lensBoxArray = LensBoxArray.new()
        val operations = parsed.map(Operation::fromString)
        operations.forEach { it.perform(lensBoxArray) }
        return lensBoxArray.focusingPower()
    }
}
