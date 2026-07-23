package io.jadie.sim

/**
 * Represents the physical state (phase) of a cell in the simulation.
 * 
 * The simulation tracks transitions between these states based on enthalpy and 
 * material thermal properties.
 */
enum class Type {
    /**
     * Solid state. Stationary, heat moves primarily by conduction.
     */
    SOLID {
        override val id: Int = 0
    },

    /**
     * Transitioning from solid to fluid (latent heat of fusion being absorbed).
     */
    FUSING {
        override val id: Int = 3
    },

    /**
     * Fluid state. Subject to advection and convection.
     */
    FLUID {
        override val id: Int = 1
    },

    /**
     * Transitioning from fluid to gas (latent heat of vaporization being absorbed).
     */
    VAPORIZING {
        override val id: Int = 4
    },

    /**
     * Gas state. Highly mobile, subject to wind and buoyant forces.
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
