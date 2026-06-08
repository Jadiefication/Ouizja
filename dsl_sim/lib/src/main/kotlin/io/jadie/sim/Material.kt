package io.jadie.sim

enum class Material {
    COPPER {
        override val type: Type = Type.SOLID
        override val id: Int = 0
    },
    WATER {
        override val type: Type = Type.FLUID
        override val id: Int = 1
    },
    WOOD {
        override val type: Type = Type.SOLID
        override val id: Int = 2
    },
    ALUMINUM {
        override val type: Type = Type.SOLID
        override val id: Int = 3
    },
    IRON {
        override val type: Type = Type.SOLID
        override val id: Int = 4
    },
    GLASS {
        override val type: Type = Type.SOLID
        override val id: Int = 5
    },
    STONE {
        override val type: Type = Type.SOLID
        override val id: Int = 6
    },
    AIR {
        override val type: Type = Type.GAS
        override val id: Int = 7
    },
    BARRIER {
        override val type: Type = Type.SOLID
        override val id: Int = 8
    };

    abstract val type: Type
    abstract val id: Int

    val diffusivity: Double
        get() = when(this) {
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