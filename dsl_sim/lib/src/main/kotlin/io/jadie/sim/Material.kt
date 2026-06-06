package io.jadie.sim

enum class Material {
    COPPER {
        override val diffusivity: Double = 165.0
        override val type: Type = Type.SOLID
    },
    WATER {
        override val diffusivity: Double = 0.14
        override val type: Type = Type.FLUID
    },
    WOOD {
        override val diffusivity: Double = 0.08
        override val type: Type = Type.SOLID
    },
    ALUMINUM {
        override val diffusivity: Double = 97.0
        override val type: Type = Type.SOLID
    },
    IRON {
        override val diffusivity: Double = 23.0
        override val type: Type = Type.SOLID
    },
    GLASS {
        override val diffusivity: Double = 0.34
        override val type: Type = Type.SOLID
    },
    STONE {
        override val diffusivity: Double = 0.5
        override val type: Type = Type.SOLID
    },
    AIR {
        override val diffusivity: Double = 19.0
        override val type: Type = Type.GAS
    },
    BARRIER {
        override val diffusivity: Double = 0.0
        override val type: Type = Type.SOLID
    };

    abstract val diffusivity: Double
    abstract val type: Type
}