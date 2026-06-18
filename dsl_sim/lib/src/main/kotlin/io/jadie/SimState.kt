package io.jadie

import io.jadie.sim.Type

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
