package io.jadie

data class SimState(
    val field: Array<FloatArray>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SimState

        return field.contentDeepEquals(other.field)
    }

    override fun hashCode(): Int {
        return field.contentDeepHashCode()
    }
}
