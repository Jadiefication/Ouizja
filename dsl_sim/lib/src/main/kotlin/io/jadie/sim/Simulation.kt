package io.jadie.sim

import io.jadie.OuizjaLoader
import io.jadie.SimState

class Simulation {

    internal var length = 256
    internal var height = 256
    internal var materialMask = mutableListOf<Material>()
    internal var temps = mutableListOf<Double>()
    internal var sourceMask = mutableListOf<Boolean>()
    internal var quantum = mutableListOf<Double>()
    internal val winds = mutableListOf<Double>()

    fun grid(length: Int, height: Int) {
        this.length = length
        this.height = height
    }

    fun globalMaterial(material: Material) {
        materialMask = MutableList(length * height) { material }
    }

    fun material(
        material: Material,
        fromX: Int,
        toX: Int,
        fromY: Int,
        toY: Int
    ) {
        if (materialMask.size != length * height) {
            materialMask = MutableList(length * height) { index ->
                val x = index / height
                val y = index % height
                if (x in fromX..toX && y in fromY..toY) {
                    material
                } else {
                    Material.BARRIER
                }
            }
        } else {
            for (index in materialMask.indices) {
                val x = index / height
                val y = index % height
                if (x in fromX..toX && y in fromY..toY) {
                    materialMask[index] = material
                }
            }
        }
    }

    fun globalTemperature(temp: Double) {
        temps = MutableList(length * height) { temp }
    }

    fun temp(
        temp: Double,
        x: Int,
        y: Int
    ) {
        if (temps.size != length * height) {
            temps = MutableList(length * height) { index ->
                val xIndex = index / height
                val yIndex = index % height
                if (xIndex == x && yIndex == y) {
                    temp
                } else {
                    0.0
                }
            }
        } else {
            val index = x * height + y
            if (index in temps.indices) {
                temps[index] = temp
            }
        }
    }

    fun barrier(
        fromX: Int,
        toX: Int,
        fromY: Int,
        toY: Int
    ) {
        material(Material.BARRIER, fromX, toX, fromY, toY)
    }

    fun source(
        x: Int,
        y: Int
    ) {
        if (sourceMask.size != length * height) {
            sourceMask = MutableList(length * height) { index ->
                val xIndex = index / height
                val yIndex = index % height
                xIndex == x && yIndex == y
            }
        } else {
            val index = x * height + y
            if (index in sourceMask.indices) {
                sourceMask[index] = true
            }
        }
    }

    fun wind(
        force: Pair<Double, Double>,
        temp: Double = 0.0
    ) {
        winds.addAll(listOf(force.first, force.second, temp))
    }

    fun superposition(
        x: Int,
        y: Int,
        kappa: Double,
        index: Int
    ) {
        quantum.addAll(listOf(x.toDouble(), y.toDouble(), kappa, index.toDouble()))
    }

    fun size(): Int {
        return length * height
    }
}

data class BuiltSim(
    internal val height: Int,
    internal val length: Int,
    internal val sourceMask: BooleanArray,
    internal val materialMask: IntArray,
    internal var quantum: DoubleArray,
    internal val winds: DoubleArray,
    internal var temps: DoubleArray,
) {
    fun run(iterations: Long): SimState {
        val sim = OuizjaLoader.createSim(
            temps,
            sourceMask,
            materialMask,
            quantum,
            winds,
            length,
            height,
        )

        val state = OuizjaLoader.runSim(iterations, sim, length, height)
        OuizjaLoader.freeSim(sim)
        return state
    }

    fun run(iterations: Long, predicate: (SimState) -> Boolean) {
        for (i in 0..iterations) {
            val state = run(1)

            if (predicate(state)) {
                break
            } else {
                val newField = buildList {
                    state.field.forEach { arr ->
                        arr.forEach { value ->
                            this.add(value)
                        }
                    }
                }.toDoubleArray()

                val newQuantum = buildList {
                    state.quantum.forEach { value ->
                        this.addAll(listOf(value.first.toDouble(), value.second.toDouble(), value.third))
                    }
                }.toDoubleArray()

                this.temps = newField
                this.quantum = newQuantum
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BuiltSim

        if (height != other.height) return false
        if (length != other.length) return false
        if (!sourceMask.contentEquals(other.sourceMask)) return false
        if (!materialMask.contentEquals(other.materialMask)) return false
        if (!quantum.contentEquals(other.quantum)) return false
        if (!winds.contentEquals(other.winds)) return false
        if (!temps.contentEquals(other.temps)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = height
        result = 31 * result + length
        result = 31 * result + sourceMask.contentHashCode()
        result = 31 * result + materialMask.contentHashCode()
        result = 31 * result + quantum.contentHashCode()
        result = 31 * result + winds.contentHashCode()
        result = 31 * result + temps.contentHashCode()
        return result
    }
}

fun simulate(builder: Simulation.() -> Unit): BuiltSim {
    val simulation = Simulation()
    simulation.builder()
    val targetSize = simulation.size()
    if (simulation.sourceMask.size != targetSize) {
        val oldMask = simulation.sourceMask
        simulation.sourceMask = MutableList(targetSize) { index ->
            if (index < oldMask.size) {
                oldMask[index]
            } else {
                false
            }
        }
    }
    if (simulation.materialMask.size != targetSize) {
        val oldMask = simulation.materialMask
        simulation.materialMask = MutableList(targetSize) { index ->
            if (index < oldMask.size) {
                oldMask[index]
            } else {
                Material.BARRIER
            }
        }
    }
    if (simulation.temps.size != targetSize) {
        val oldMask = simulation.temps
        simulation.temps = MutableList(targetSize) { index ->
            if (index < oldMask.size) {
                oldMask[index]
            } else {
                0.0
            }
        }
    }
    val built = BuiltSim(
        simulation.height,
        simulation.length,
        simulation.sourceMask.toBooleanArray(),
        simulation.materialMask.map { it.id }.toIntArray(),
        simulation.quantum.toDoubleArray(),
        simulation.winds.toDoubleArray(),
        simulation.temps.toDoubleArray()
    )
    return built
}

val BuiltSim.nonSolidMask: BooleanArray
    get() = materialMask.map { id ->
        val material = Material.entries.find { it.id == id } ?: Material.BARRIER
        material.type != Type.SOLID
    }.toBooleanArray()

fun BuiltSim.toMaterialArray(): Array<Material> {
    return materialMask.map { id -> Material.entries.find { it.id == id } ?: Material.BARRIER }.toTypedArray()
}

fun transform(oldState: BuiltSim, newState: SimState): BuiltSim {
    val newField = buildList {
        newState.field.forEach { arr ->
            arr.forEach { value ->
                this.add(value)
            }
        }
    }.toDoubleArray()

    val newQuantum = buildList {
        newState.quantum.forEach { value ->
            this.addAll(listOf(value.first.toDouble(), value.second.toDouble(), value.third))
        }
    }.toDoubleArray()

    val newSim = BuiltSim(
        oldState.height,
        oldState.length,
        oldState.sourceMask,
        oldState.materialMask,
        newQuantum,
        oldState.winds,
        newField,
    )
    return newSim
}