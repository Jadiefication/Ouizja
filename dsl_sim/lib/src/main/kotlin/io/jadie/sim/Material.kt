package io.jadie.sim

enum class Material {
    COPPER {
        override val diffusivity: Double = 165.0
    },
    WATER {
        override val diffusivity: Double = 0.14
    },
    WOOD {
        override val diffusivity: Double = 0.08
    },
    ALUMINUM {
        override val diffusivity: Double = 97.0
    },
    IRON {
        override val diffusivity: Double = 23.0
    },
    GLASS {
        override val diffusivity: Double = 0.34
    },
    STONE {
        override val diffusivity: Double = 0.5
    },
    AIR {
        override val diffusivity: Double = 19.0
    },
    BARRIER {
        override val diffusivity: Double = 0.0

    };

    abstract val diffusivity: Double
}