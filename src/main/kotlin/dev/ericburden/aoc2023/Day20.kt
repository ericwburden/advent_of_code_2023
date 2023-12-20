package dev.ericburden.aoc2023

import dev.ericburden.aoc2023.Utils.lcm

/**
 * This enum represents the two kinds of pulses
 */
enum class PulseKind {
    LOW, HIGH
}

/**
 * This class represents a pulse sent through the [ModuleArray]
 *
 * @property source The label of the module sending the pulse.
 * @property destination The label of the module to receive the pulse.
 * @property kind The kind of pulse being sent.
 */
data class Pulse(
    val source: String,
    val destination: String,
    val kind: PulseKind
)

/**
 * This class serves as the base class for all modules
 *
 * @property label The label for this module, giving its name.
 * @property destinations A list of labels for modules to send pulses to.
 */
abstract class AbstractModule(
    val label: String,
    val destinations: List<String>
) {
    /**
     * Receive a pulse and send pulses to all destination modules
     *
     * This function is responsible for this module receiving a pulse,
     * processing it in some way, then sending resulting pulses to all of
     * this module's destinations.
     *
     * @param pulse The pulse sent to this module.
     * @return The list of pulses sent by this module.
     */
    abstract fun relay(pulse: Pulse): List<Pulse>

    abstract override fun toString(): String
}

/**
 * This class represents the Broadcaster module
 *
 * Broadcaster modules only receive pulses from the "button" and forward that
 * pulse on to its destinations.
 *
 * @property label The label for this module, giving its name.
 * @property destinations A list of labels for modules to send pulses to.
 */
class Broadcaster(label: String, destinations: List<String>) :
    AbstractModule(label, destinations) {
    override fun relay(pulse: Pulse) =
        destinations.map { Pulse(label, it, PulseKind.LOW) }

    override fun toString(): String =
        "$label -> ${destinations.joinToString()}"
}

/**
 * This class represents the FlipFlop module
 *
 * The FlipFlop module maintains an inner ON or OFF state, which affects
 * how the module responds to pulses. FlipFlop modules ignore HIGH pulses.
 * A LOW pulse will cause the FlipFlop's state to toggle from ON to OFF or
 * from OFF to ON. When the FlipFlop toggles ON, it sends a HIGH pulse to
 * all destinations. When the FlipFlop toggles OFF, it sends a LOW pulse to
 * all destinations.
 *
 * @property label The label for this module, giving its name.
 * @property destinations A list of labels for modules to send pulses to.
 * @property state The internal state of the FlipFlop.
 */
class FlipFlop(
    label: String,
    destinations: List<String>,
    private var state: FlipFlopState = FlipFlopState.OFF
) : AbstractModule(label, destinations) {
    enum class FlipFlopState { ON, OFF }

    override fun relay(pulse: Pulse) = when (pulse.kind) {
        PulseKind.HIGH -> listOf()
        PulseKind.LOW -> when (state) {
            FlipFlopState.OFF -> {
                state = FlipFlopState.ON
                destinations.map { Pulse(label, it, PulseKind.HIGH) }
            }

            FlipFlopState.ON -> {
                state = FlipFlopState.OFF
                destinations.map { Pulse(label, it, PulseKind.LOW) }
            }
        }
    }

    override fun toString(): String =
        "%$label -> ${destinations.joinToString()}"
}

/**
 * This class represents the Conjuction module
 *
 * The Conjunction module maintains a memory of the last pulse kind received
 * from each of its inputs. When receiving a pulse, first the Conjunction
 * updates its memory. If all pulses in memory are HIGH pulses, the Conjunction
 * emits a LOW pulse. Otherwise, it emits a HIGH pulse.
 *
 * @property label The label for this module, giving its name.
 * @property destinations A list of labels for modules to send pulses to.
 * @property memory The internal state of the Conjunction, tracking the kind of
 * pulse last received from all its inputs. Defaults to LOW for all inputs.
 */
class Conjunction(
    label: String,
    destinations: List<String>,
    private val memory: MutableMap<String, PulseKind> = mutableMapOf()
) : AbstractModule(label, destinations) {
    override fun relay(pulse: Pulse): List<Pulse> {
        // First update the memory for the input
        memory[pulse.source] = pulse.kind

        // Then determine the kind of pulse to send
        val sendKind = if (memory.values.all { it == PulseKind.HIGH }) {
            PulseKind.LOW
        } else {
            PulseKind.HIGH
        }

        return destinations.map { Pulse(label, it, sendKind) }
    }

    // Used to register an input to this module.
    fun registerInput(label: String) {
        memory[label] = PulseKind.LOW
    }

    override fun toString(): String {
        val registeredInputs =
            memory.asSequence().joinToString { (lbl, pk) -> "$lbl: $pk" }
        return "&$label -> ${destinations.joinToString()} ($registeredInputs)"
    }
}

/**
 * This class represents the full array of modules
 *
 * @property modules A map of module labels to [AbstractModule]s.
 */
data class ModuleArray(val modules: Map<String, AbstractModule>) {
    companion object {
        /**
         * Parse a [ModuleArray] from the lines of the input file
         *
         * Each line in the input represents the relationship between a sender
         * module and its receivers. This function parses those lines into
         * [AbstractModule]s and maps the labels of the modules to the
         * [AbstractModule]s they represent.
         *
         * @param input Lines from the input file.
         * @return The [ModuleArray] represented by the input.
         * @throws Exception When a line from the input cannot be parsed.
         */
        fun fromInput(input: List<String>): ModuleArray {
            val modules = mutableMapOf<String, AbstractModule>()
            for (line in input) {
                // Get the strings left and right of the " -> "
                val (leftStr, rightStr) = line.split(" -> ")

                // Split the destination labels on commas
                val destinations = rightStr.split(", ")

                // Produce the appropriate kind of [AbstractModule] based on the
                // string to the left of the " -> ".
                val (label, module) = when {
                    leftStr == "broadcaster" -> (leftStr
                            to Broadcaster(leftStr, destinations))

                    leftStr.startsWith("%") -> {
                        val label = leftStr.removePrefix("%")
                        label to FlipFlop(label, destinations)
                    }

                    leftStr.startsWith("&") -> {
                        val label = leftStr.removePrefix("&")
                        label to Conjunction(label, destinations)
                    }

                    else -> throw Exception("Cannot parse $line to a module mapping!")
                }
                modules[label] = module
            }

            // At this point, the [Conjunction] modules don't know what their
            // inputs are, so we loop through the mapping of modules and
            // register each input to a Conjunction module with the
            // receiving Conjunction module.
            for ((inputLabel, module) in modules) {
                for (destination in module.destinations) {
                    val destinationModule = modules[destination]
                    if (destinationModule !is Conjunction) continue
                    destinationModule.registerInput(inputLabel)
                }
            }

            return ModuleArray(modules)
        }
    }

    /**
     * Send a pulse through the [ModuleArray]
     *
     * Since each pulse is equipped with the name of the sender and receiver,
     * we can pass it directly to the [ModuleArray] and route it to its
     * intended receiver. If the named destination isn't in the module array,
     * we can just ignore that pulse.
     *
     * @param pulse The [Pulse] to route through the [ModuleArray].
     * @return The list of pulses that result from routing the input pulse to
     * its intended destination.
     */
    private fun route(pulse: Pulse): List<Pulse> {
        val destinationModule = modules[pulse.destination]
            ?: return listOf()
        return destinationModule.relay(pulse)
    }

    /**
     * Push. The. Button!!!
     *
     * Simulates pushing the button module one time, which sends a LOW pulse to
     * the [Broadcaster] module. The resulting pulses are created and consumed
     * until no more pulses are generated. Returns a list of all pulses sent
     * through the [ModuleArray] as a result of pushing the button.
     *
     * @return A list of pulses generated by pushing the button.
     */
    fun pushTheButton(): List<Pulse> {
        val initialPulse = Pulse("button", "broadcaster", PulseKind.LOW)
        val queue = ArrayDeque(listOf(initialPulse))
        val sentPulses = mutableListOf<Pulse>()

        while (queue.isNotEmpty()) {
            val pulse = queue.removeLast()
            sentPulses.add(pulse)
            for (nextPulse in route(pulse)) queue.addFirst(nextPulse)
        }

        return sentPulses
    }

    /**
     * Find the cycle length of a single Conjunction module
     *
     * In part two, we need to identify the periodicity with which several
     * [Conjunction] modules send a HIGH signal. Given the label for a
     * conjunction module, simulate pushing the button while keeping an eye
     * on that conjunction module, returning the number of button pushes it
     * regularly takes to produce a HIGH pulse.
     *
     * @param label The label for the [Conjunction] module to monitor.
     * @return The number of button presses in the cycle that produces a HIGH
     * pulse from the monitored [Conjunction] module.
     */
    fun findConjunctionModuleCycleLength(label: String): Long {
        // Start by making sure we've actually got a [Conjunction] module that's
        // in the [ModuleArray].
        val checkedModule =
            modules[label] ?: throw Exception("There is no $label module!")
        require(checkedModule is Conjunction) { throw Exception("$checkedModule is not a Conjunction!") }

        // Continue by pushing the button until the indicated module sends a LOW
        // signal. I included this part in case there were some button pushes
        // needed to get to the point where the cycle starts. In that case, I
        // would have needed to account for those pushes later. Turns out, that
        // wasn't the case, but I left this in to explain my reasoning.
        var isPrimed = false
        while (!isPrimed) {
            isPrimed =
                pushTheButton().any { (s, _, k) -> s == label && k == PulseKind.HIGH }
        }

        // Starting at the state right after the monitored conjunction module
        // sent a HIGH pulse, we again push the button repeatedly until the
        // monitored module sends another HIGH pulse. This is the presumed
        // cycle length.
        var cycleLength = 1L
        while (!pushTheButton().any { (s, _, k) -> s == label && k == PulseKind.HIGH }) {
            cycleLength += 1
        }

        // To confirm this cycle length is stable, we run the cycle one more time
        // and compare this count to the previous count. If the two cycle counts
        // weren't equal, we'd need to figure out something else to solve this
        // puzzle.
        var confirmationCycleLength = 1L
        while (!pushTheButton().any { (s, _, k) -> s == label && k == PulseKind.HIGH }) {
            confirmationCycleLength += 1
        }

        // I wasn't initially convinced that the only state I needed to keep up with
        // was the output from the module of interest. If I wasn't able to get
        // a consistent cycle length just checking the output of the module under
        // review, I'd have needed to try something else.
        require(cycleLength == confirmationCycleLength) {
            throw Exception("Need more info to calculate cycle length for $label!")
        }

        return cycleLength
    }
}

class Day20(val input: List<String>) {

    // Parse the input file into an array of [AbstractModule]s
    private val parsed: ModuleArray get() = ModuleArray.fromInput(input)

    // In part one, we push the button, sending pulses through the array
    // of modules and count the kinds of pulses that propagate through.
    fun solvePart1(): Int {
        var lowPulses = 0
        var highPulses = 0
        val moduleArray = parsed
        repeat(1000) {
            moduleArray.pushTheButton().forEach { pulse ->
                when (pulse.kind) {
                    PulseKind.LOW -> lowPulses += 1
                    PulseKind.HIGH -> highPulses += 1
                }
            }
        }
        return lowPulses * highPulses
    }


    /*
    My input includes the following lines:
          &hj -> rx
          &ks -> hj
          &jf -> hj
          &qs -> hj
          &zk -> hj

    So, what does this mean? For one, it means that there is a single
    Conjunction module `hj` attached to `rx` with four other Conjunction
    modules feeding into it. That means a _lot_ of button presses are likely
    needed to have all four of those Conjunction modules coincide to send a
    HIGH pulse to `hj` at the same time. And, since it's Advent of Code and
    we're likely dealing with a huge number, let's look for cycles!
    */
    fun solvePart2(): Long {
        // Start by finding the input that feeds into 'rx'
        val rxInput = parsed.modules.asSequence()
            .filter { (_, module) -> "rx" in module.destinations }
            .map { (label, _) -> label }.single()

        // Then get the cycle length for all the inputs that feed into the `rxInput`.
        // The least common multiple of those cycle lengths is the number of
        // button pushes needed to get them to all sync up.
        return parsed.modules.asSequence()
            .filter { (_, module) -> rxInput in module.destinations }
            .map { (label, _) -> parsed.findConjunctionModuleCycleLength(label) }
            .toList()
            .lcm()
    }
}
