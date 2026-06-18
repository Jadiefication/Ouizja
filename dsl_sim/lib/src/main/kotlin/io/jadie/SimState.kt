package io.jadie

import io.jadie.sim.Type

/**
 * Represents the state of a simulation at a specific point in time.
 *
 * @property field A 2D array of temperatures for each cell in the grid.
 * @property quantum A collection of triples representing position (x, y) and an associated value (gamma).
 * @property states A 2D array of the physical state ([Type]) for each cell in the grid.
 */
data class SimState(
    val field: Array<DoubleArray>,
    // [x, y, gamma]
    val quantum: Array<Triple<Int, Int, Double>>,
    val states: Array<Array<Type>>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SimState

        if (!field.contentDeepEquals(other.field)) return false
        if (!quantum.contentEquals(other.quantum)) return false
        if (!states.contentDeepEquals(other.states)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = field.contentDeepHashCode()
        result = 31 * result + quantum.contentHashCode()
        result = 31 * result + states.contentDeepHashCode()
        return result
    }
}
