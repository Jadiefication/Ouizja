package io.jadie.sim

import io.jadie.OuizjaLoader
import io.jadie.SimState
import org.spongepowered.noise.module.NoiseModule

/**
 * DSL class for configuring a simulation.
 */
class Simulation {
    var length = 256
    var height = 256
    var materialMask = mutableListOf<Material>()
    var temps = mutableListOf<Double>()
    var sourceMask = mutableListOf<Boolean>()
    var quantum = mutableListOf<Double>()
    val winds = mutableListOf<Double>()

    /**
     * The ambient temperature of the simulation in Kelvin.
     */
    var ambient: Double = 293.15

    /**
     * Sets the grid dimensions.
     */
    fun grid(
        length: Int,
        height: Int,
    ) {
        this.length = length
        this.height = height
    }

    /**
     * Sets the material for the entire grid.
     */
    fun globalMaterial(material: Material) {
        materialMask = MutableList(length * height) { material }
    }

    /**
     * Sets the material for a specific rectangular area.
     */
    fun material(
        material: Material,
        fromX: Int,
        toX: Int,
        fromY: Int,
        toY: Int,
    ) {
        if (materialMask.size != length * height) {
            materialMask =
                MutableList(length * height) { index ->
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

    /**
     * Sets the temperature for the entire grid.
     */
    fun globalTemperature(temp: Double) {
        temps = MutableList(length * height) { temp }
    }

    /**
     * Sets the temperature at a specific cell.
     */
    fun temp(
        temp: Double,
        x: Int,
        y: Int,
    ) {
        if (temps.size != length * height) {
            temps =
                MutableList(length * height) { index ->
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

    /**
     * Sets the temperature for a specific rectangular area.
     */
    fun temp(
        temp: Double,
        fromX: Int,
        toX: Int,
        fromY: Int,
        toY: Int,
    ) {
        if (temps.size != length * height) {
            temps =
                MutableList(length * height) { index ->
                    val x = index / height
                    val y = index % height
                    if (x in fromX..toX && y in fromY..toY) {
                        temp
                    } else {
                        0.0
                    }
                }
        } else {
            for (index in temps.indices) {
                val x = index / height
                val y = index % height
                if (x in fromX..toX && y in fromY..toY) {
                    temps[index] = temp
                }
            }
        }
    }

    /**
     * Adds a heat barrier in a specific rectangular area.
     */
    fun barrier(
        fromX: Int,
        toX: Int,
        fromY: Int,
        toY: Int,
    ) {
        material(Material.BARRIER, fromX, toX, fromY, toY)
    }

    /**
     * Marks a specific cell as a heat source.
     */
    fun source(
        x: Int,
        y: Int,
    ) {
        if (sourceMask.size != length * height) {
            sourceMask =
                MutableList(length * height) { index ->
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

    /**
     * Adds a wind effect to the simulation.
     *
     * @param force A [Pair] representing the wind force vector.
     * @param temp The temperature of the wind.
     */
    fun wind(
        force: Pair<Double, Double>,
        temp: Double = 0.0,
    ) {
        winds.addAll(listOf(force.first, force.second, temp))
    }

    /**
     * Configures a quantum superposition effect.
     */
    fun superposition(
        x: Int,
        y: Int,
        kappa: Double,
        index: Int,
    ) {
        quantum.addAll(listOf(x.toDouble(), y.toDouble(), kappa, index.toDouble()))
    }

    /**
     * Sets cells within a circular area as heat sources or sinks.
     */
    fun circle(
        centerX: Int,
        centerY: Int,
        radius: Int,
        source: Boolean,
    ) {
        val r_2 = radius * radius
        if (sourceMask.size != length * height) {
            sourceMask =
                MutableList(length * height) { index ->
                    val xIndex = index / height
                    val yIndex = index % height

                    val d_x = (xIndex + 0.5) - centerX
                    val d_y = (yIndex + 0.5) - centerY

                    if (d_x * d_x + d_y * d_y <= r_2) {
                        source
                    } else {
                        !source
                    }
                }
        } else {
            for (index in sourceMask.indices) {
                val xIndex = index / height
                val yIndex = index % height

                val d_x = (xIndex + 0.5) - centerX
                val d_y = (yIndex + 0.5) - centerY

                if (d_x * d_x + d_y * d_y <= r_2) {
                    sourceMask[index] = source
                }
            }
        }
    }

    fun circle(
        centerX: Int,
        centerY: Int,
        radius: Int,
        temp: Double,
    ) {
        val r_2 = radius * radius
        if (temps.size != length * height) {
            temps =
                MutableList(length * height) { index ->
                    val xIndex = index / height
                    val yIndex = index % height

                    val d_x = (xIndex + 0.5) - centerX
                    val d_y = (yIndex + 0.5) - centerY

                    if (d_x * d_x + d_y * d_y <= r_2) {
                        temp
                    } else {
                        0.0
                    }
                }
        } else {
            for (index in temps.indices) {
                val xIndex = index / height
                val yIndex = index % height

                val d_x = (xIndex + 0.5) - centerX
                val d_y = (yIndex + 0.5) - centerY

                if (d_x * d_x + d_y * d_y <= r_2) {
                    temps[index] = temp
                }
            }
        }
    }

    /**
     * Sets the material for cells within a circular area.
     */
    fun circle(
        centerX: Int,
        centerY: Int,
        radius: Int,
        material: Material,
    ) {
        val r_2 = radius * radius
        if (materialMask.size != length * height) {
            materialMask =
                MutableList(length * height) { index ->
                    val xIndex = index / height
                    val yIndex = index % height

                    val d_x = (xIndex + 0.5) - centerX
                    val d_y = (yIndex + 0.5) - centerY

                    if (d_x * d_x + d_y * d_y <= r_2) {
                        material
                    } else {
                        Material.BARRIER
                    }
                }
        } else {
            for (index in materialMask.indices) {
                val xIndex = index / height
                val yIndex = index % height

                val d_x = (xIndex + 0.5) - centerX
                val d_y = (yIndex + 0.5) - centerY

                if (d_x * d_x + d_y * d_y <= r_2) {
                    materialMask[index] = material
                }
            }
        }
    }

    /**
     * Applies a noise function to the simulation.
     *
     * @param noise The noise module to use.
     * @param scale The scale of the noise.
     * @param apply A lambda that defines how to apply the noise value to a cell.
     */
    fun <T : NoiseModule> noise(
        noise: T,
        scale: Double = 1.0,
        apply: Simulation.(Double, Int, Int) -> Unit,
    ) {
        for (x in 0 until length) {
            for (y in 0 until height) {
                val nAverage =
                    (
                        noise.get((x - 1) * scale, y * scale, 0.0) +
                            noise.get((x + 1) * scale, y * scale, 0.0) +
                            noise.get(x * scale, (y - 1) * scale, 0.0) +
                            noise.get(x * scale, (y + 1) * scale, 0.0) +
                            noise.get(x * scale, y * scale, 0.0)
                    ) / 5.0

                apply(nAverage, x, y)
            }
        }
    }

    /**
     * Sets the ambient temperature.
     */
    fun ambient(new: Double) {
        ambient = new
    }

    /**
     * Returns the total number of cells in the grid.
     */
    fun size(): Int = length * height
}

/**
 * A built and ready-to-run simulation instance.
 */
data class BuiltSim(
    val height: Int,
    val length: Int,
    val sourceMask: BooleanArray,
    val materialMask: IntArray,
    var quantum: DoubleArray,
    val winds: DoubleArray,
    var temps: DoubleArray,
    val ambient: Double,
) {
    /**
     * Runs the simulation for a specified number of iterations and returns the final state.
     */
    fun run(iterations: Long): SimState {
        val sim =
            OuizjaLoader.createSim(
                temps,
                sourceMask,
                materialMask,
                quantum,
                winds,
                length,
                height,
                ambient,
            )

        val state = OuizjaLoader.runSim(iterations, sim, length, height)
        OuizjaLoader.freeSim(sim)
        return state
    }

    /**
     * Runs the simulation in steps, executing the predicate after each step.
     * Stops if the predicate returns true.
     */
    fun run(
        iterations: Long,
        predicate: (SimState) -> Boolean,
    ) {
        for (i in 0..iterations) {
            val state = run(1)

            if (predicate(state)) {
                break
            } else {
                val newField = DoubleArray(length * height)
                for (x in 0..<length) {
                    for (y in 0..<height) {
                        val i = x * height + y
                        val value = state.field[y][x]
                        newField[i] = value
                    }
                }

                val newQuantum =
                    buildList {
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

/**
 * Builds a simulation using the provided [builder] DSL.
 */
fun simulate(builder: Simulation.() -> Unit): BuiltSim {
    val simulation = Simulation()
    simulation.builder()
    val targetSize = simulation.size()
    if (simulation.sourceMask.size != targetSize) {
        val oldMask = simulation.sourceMask
        simulation.sourceMask =
            MutableList(targetSize) { index ->
                if (index < oldMask.size) {
                    oldMask[index]
                } else {
                    false
                }
            }
    }
    if (simulation.materialMask.size != targetSize) {
        val oldMask = simulation.materialMask
        simulation.materialMask =
            MutableList(targetSize) { index ->
                if (index < oldMask.size) {
                    oldMask[index]
                } else {
                    Material.BARRIER
                }
            }
    }
    if (simulation.temps.size != targetSize) {
        val oldMask = simulation.temps
        simulation.temps =
            MutableList(targetSize) { index ->
                if (index < oldMask.size) {
                    oldMask[index]
                } else {
                    0.0
                }
            }
    }
    val built =
        BuiltSim(
            simulation.height,
            simulation.length,
            simulation.sourceMask.toBooleanArray(),
            simulation.materialMask.map { it.id }.toIntArray(),
            simulation.quantum.toDoubleArray(),
            simulation.winds.toDoubleArray(),
            simulation.temps.toDoubleArray(),
            simulation.ambient,
        )
    return built
}

/**
 * Returns a boolean mask where `true` indicates a non-solid cell.
 */
val BuiltSim.nonSolidMask: BooleanArray
    get() =
        materialMask
            .map { id ->
                val material = Material.entries.find { it.id == id } ?: Material.BARRIER
                material.type != Type.SOLID
            }.toBooleanArray()

/**
 * Converts the material mask ID array to an array of [Material] objects.
 */
fun BuiltSim.toMaterialArray(): Array<Material> =
    materialMask
        .map { id ->
            Material.entries.find {
                it.id == id
            } ?: Material.BARRIER
        }.toTypedArray()

/**
 * Creates a new [BuiltSim] by applying the state from [newState] to [oldState].
 */
fun transform(
    oldState: BuiltSim,
    newState: SimState,
): BuiltSim {
    val newField = DoubleArray(oldState.length * oldState.height)
    for (x in 0..<oldState.length) {
        for (y in 0..<oldState.height) {
            val i = x * oldState.height + y
            val value = newState.field[y][x]
            newField[i] = value
        }
    }

    val newQuantum =
        buildList {
            newState.quantum.forEach { value ->
                this.addAll(listOf(value.first.toDouble(), value.second.toDouble(), value.third))
            }
        }.toDoubleArray()

    val newSim =
        BuiltSim(
            oldState.height,
            oldState.length,
            oldState.sourceMask,
            oldState.materialMask,
            newQuantum,
            oldState.winds,
            newField,
            oldState.ambient,
        )
    return newSim
}
