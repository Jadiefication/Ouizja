package io.jadie.sim

import io.jadie.OuizjaLoader
import io.jadie.SimState

class Simulation {

    internal var length = 256
    internal var height = 256
    internal var alphaMask = mutableListOf<Material>()
    internal var temps = mutableListOf<Double>()
    internal var sourceMask = mutableListOf<Boolean>()
    internal val winds = mutableListOf<Double>()

    fun grid(length: Int, height: Int) {
        this.length = length
        this.height = height
    }

    fun globalMaterial(material: Material) {
        alphaMask = MutableList(length * height) { material }
    }

    fun material(
        material: Material,
        fromX: Int,
        toX: Int,
        fromY: Int,
        toY: Int
    ) {
        if (alphaMask.size != length * height) {
            alphaMask = MutableList(length * height) { index ->
                val x = index / height
                val y = index % height
                if (x in fromX..toX && y in fromY..toY) {
                    material
                } else {
                    Material.BARRIER
                }
            }
        } else {
            for (index in alphaMask.indices) {
                val x = index / height
                val y = index % height
                if (x in fromX..toX && y in fromY..toY) {
                    alphaMask[index] = material
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
        force: Pair<Double, Double>
    ) {
        winds.addAll(listOf(force.first, force.second))
    }

    fun size(): Int {
        return length * height
    }
}

data class BuiltSim(
    val height: Int,
    val length: Int,
    val alphaMask: DoubleArray,
    val sourceMask: BooleanArray,
    val nonSolidMask: BooleanArray,
    val winds: DoubleArray,
    val temps: DoubleArray
) {
    fun run(iterations: Long): SimState {
        val sim = OuizjaLoader.createSim(
            temps,
            sourceMask,
            alphaMask,
            nonSolidMask,
            winds,
            length,
            height,
        )

        val state = OuizjaLoader.runSim(iterations, sim, length, height)
        OuizjaLoader.freeSim(sim)
        return state
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BuiltSim

        if (height != other.height) return false
        if (length != other.length) return false
        if (!alphaMask.contentEquals(other.alphaMask)) return false
        if (!sourceMask.contentEquals(other.sourceMask)) return false
        if (!nonSolidMask.contentEquals(other.nonSolidMask)) return false
        if (!temps.contentEquals(other.temps)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = height
        result = 31 * result + length
        result = 31 * result + alphaMask.contentHashCode()
        result = 31 * result + sourceMask.contentHashCode()
        result = 31 * result + nonSolidMask.contentHashCode()
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
    if (simulation.alphaMask.size != targetSize) {
        val oldMask = simulation.alphaMask
        simulation.alphaMask = MutableList(targetSize) { index ->
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
        simulation.alphaMask.map { it.diffusivity }.toDoubleArray(),
        simulation.sourceMask.toBooleanArray(),
        simulation.alphaMask.map { it.type != Type.SOLID }.toBooleanArray(),
        simulation.winds.toDoubleArray(),
        simulation.temps.toDoubleArray(),
    )
    return built
}

fun transform(oldState: BuiltSim, newState: SimState): BuiltSim {
    val newField = buildList {
        newState.field.forEach { arr ->
            arr.forEach { value ->
                this.add(value)
            }
        }
    }.toDoubleArray()
    val newSim = BuiltSim(
        oldState.height,
        oldState.length,
        oldState.alphaMask,
        oldState.sourceMask,
        oldState.nonSolidMask,
        oldState.winds,
        newField
    )
    return newSim
}