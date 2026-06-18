package io.jadie.sim

/**
 * Represents the physical state of a cell in the simulation.
 */
enum class Type {
    /**
     * Solid state.
     */
    SOLID {
        override val id: Int = 0
    },

    /**
     * Transitioning from solid to fluid.
     */
    FUSING {
        override val id: Int = 3
    },

    /**
     * Fluid state.
     */
    FLUID {
        override val id: Int = 1
    },

    /**
     * Transitioning from fluid to gas.
     */
    VAPORIZING {
        override val id: Int = 4
    },

    /**
     * Gas state.
     */
    GAS {
        override val id: Int = 2
    }, ;

    /**
     * Numeric identifier used for JNI communication.
     */
    abstract val id: Int

    companion object {
        private val maxId = entries.toTypedArray().maxOf { it.id }

        private val lookupTable =
            Array(maxId + 1) { index ->
                entries.find { it.id == index } ?: SOLID
            }

        /**
         * Returns the [Type] corresponding to the given [id].
         * Defaults to [SOLID] if the id is invalid.
         */
        @JvmStatic
        fun fromId(id: Int): Type {
            if (id < 0 || id >= lookupTable.size) return SOLID
            return lookupTable[id]
        }
    }
}
