package io.jadie.sim

enum class Type {
    SOLID {
        override val id: Int = 0
    },
    FUSING {
        override val id: Int = 3
    },
    FLUID {
        override val id: Int = 1
    },
    VAPORIZING {
        override val id: Int = 4
    },
    GAS {
        override val id: Int = 2
    };

    abstract val id: Int

    companion object {
        private val maxId = entries.toTypedArray().maxOf { it.id }

        private val lookupTable = Array(maxId + 1) { index ->
            entries.find { it.id == index } ?: SOLID
        }

        @JvmStatic
        fun fromId(id: Int): Type {
            if (id < 0 || id >= lookupTable.size) return SOLID
            return lookupTable[id]
        }
    }

}