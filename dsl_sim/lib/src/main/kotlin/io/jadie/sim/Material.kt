package io.jadie.sim

/**
 * Defines the materials available for cells in the simulation.
 * 
 * Each material has a default [Type] (physical state) and a [diffusivity] coefficient
 * that determines how quickly heat spreads through it.
 */
enum class Material {
    /** Highly conductive metal. */
    COPPER {
        override val type: Type = Type.SOLID
        override val id: Int = 0
    },
    /** Common volatile liquid. */
    WATER {
        override val type: Type = Type.FLUID
        override val id: Int = 1
    },
    /** Organic insulating material. */
    WOOD {
        override val type: Type = Type.SOLID
        override val id: Int = 2
    },
    /** Lightweight conductive metal. */
    ALUMINUM {
        override val type: Type = Type.SOLID
        override val id: Int = 3
    },
    /** Common structural metal. */
    IRON {
        override val type: Type = Type.SOLID
        override val id: Int = 4
    },
    /** Transparent insulating material. */
    GLASS {
        override val type: Type = Type.SOLID
        override val id: Int = 5
    },
    /** Natural mineral material. */
    STONE {
        override val type: Type = Type.SOLID
        override val id: Int = 6
    },
    /** Atmospheric gas. */
    AIR {
        override val type: Type = Type.GAS
        override val id: Int = 7
    },

    /**
     * A material that acts as a heat barrier (diffusivity = 0).
     * 
     * Used for bounding the simulation or creating perfectly insulated regions.
     */
    BARRIER {
        override val type: Type = Type.SOLID
        override val id: Int = 8
    }, ;

    /**
     * Initial physical state of the material.
     */
    abstract val type: Type

    /**
     * Numeric identifier used for JNI communication.
     */
    abstract val id: Int

    /**
     * Thermal diffusivity of the material.
     */
    val diffusivity: Double
        get() =
            when (this) {
                COPPER -> 165.0
                WATER -> 0.14
                WOOD -> 0.08
                ALUMINUM -> 97.0
                IRON -> 23.0
                GLASS -> 0.34
                STONE -> 0.5
                AIR -> 19.0
                BARRIER -> 0.0
            }
}
