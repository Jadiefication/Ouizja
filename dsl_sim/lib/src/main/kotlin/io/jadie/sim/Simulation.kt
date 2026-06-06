package io.jadie.sim

import io.jadie.OuizjaLoader
import io.jadie.SimState

class Simulation {

    var length = 256
    var height = 256
    var alphaMask = mutableListOf<Material>()
    var temps = mutableListOf<Double>()
    var sourceMask = mutableListOf<Boolean>()

    fun grid(length: Int, height: Int) {
        this.length = length
        this.height = height
    }

    fun globalMaterial(material: Material) {
        alphaMask = buildList {
            for (i in 0..height) {
                for (j in 0..length) {
                    this[i * height + j] = material
                }
            }
        }.toMutableList()
    }

    fun material(
        material: Material,
        fromX: Int,
        toX: Int,
        fromY: Int,
        toY: Int
    ) {
        if (alphaMask.size != length * height) {
            alphaMask = buildList {
                for (i in 0..height) {
                    for (j in 0..length) {
                        this[i * height + j] = if (
                            i in fromX..toX && j in fromY..toY) {
                            material
                        } else {
                            Material.BARRIER
                        }
                    }
                }
            }.toMutableList()
        } else {
            alphaMask.mapIndexed { index, currentAlpha ->
                val x = index / height
                val y = index % height

                if (x in fromX..toX && y in fromY..toY) {
                    material.diffusivity
                } else {
                    currentAlpha
                }
            }
        }
    }

    fun globalTemperature(temp: Double) {
        temps = buildList {
            for (i in 0..height) {
                for (j in 0..length) {
                    this[i * height + j] = temp
                }
            }
        }.toMutableList()
    }

    fun temp(
        temp: Double,
        x: Int,
        y: Int
    ) {
        if (temps.size != length * height) {
            temps = buildList {
                for (i in 0..height) {
                    for (j in 0..length) {
                        this[i * height + j] = if (
                            i == x && j == y
                        ) {
                            temp
                        } else {
                            0.0
                        }
                    }
                }
            }.toMutableList()
        } else {
            temps.mapIndexed { index, currentTemp ->
                val xIndex = index / height
                val yIndex = index % height

                if (xIndex == x && yIndex == y) {
                    temp
                } else {
                    currentTemp
                }
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
            sourceMask = buildList {
                for (i in 0..height) {
                    for (j in 0..length) {
                        this[i * height + j] = i == x && j == y
                    }
                }
            }.toMutableList()
        } else {
            sourceMask.mapIndexed { index, currentVal ->
                val xIndex = index / height
                val yIndex = index % height

                if (xIndex == x && yIndex == y) {
                    true
                } else {
                    currentVal
                }
            }
        }
    }
}

data class BuiltSim(
    val height: Int,
    val length: Int,
    val alphaMask: DoubleArray,
    val sourceMask: BooleanArray,
    val temps: DoubleArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BuiltSim

        if (height != other.height) return false
        if (length != other.length) return false
        if (!alphaMask.contentEquals(other.alphaMask)) return false
        if (!temps.contentEquals(other.temps)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = height
        result = 31 * result + length
        result = 31 * result + alphaMask.contentHashCode()
        result = 31 * result + temps.contentHashCode()
        return result
    }

    fun run(iterations: Long): SimState {
        val sim = OuizjaLoader.createSim(
            temps,
            sourceMask,
            alphaMask,
            length,
            height
        )

        val state = OuizjaLoader.runSim(iterations, sim, length, height)
        OuizjaLoader.freeSim(sim)
        return state
    }
}

fun simulate(builder: Simulation.() -> Unit): BuiltSim {
    val simulation = Simulation()
    simulation.builder()
    val built = BuiltSim(
        simulation.height,
        simulation.length,
        simulation.alphaMask.map { it.diffusivity }.toDoubleArray(),
        simulation.sourceMask.toBooleanArray(),
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
        newField
    )
    return newSim
}