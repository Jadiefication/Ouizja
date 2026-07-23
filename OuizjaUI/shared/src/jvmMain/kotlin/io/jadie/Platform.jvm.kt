package io.jadie

import io.jadie.sim.Material
import io.jadie.sim.simulate
import io.jadie.sim.toMaterialArray

/**
 * JVM implementation of the simulation runner.
 * 
 * This function parses the DSL configuration from [settings], builds the simulation,
 * and executes it in a loop, providing updates to the UI via [onUpdate].
 */
actual fun runSimulation(
    settings: SimSettings,
    onUpdate: (Array<DoubleArray>, Array<Array<Material>>, Int, Double) -> Unit,
) {
    val parser = DslParser()
    val builtSim = parser.parse(settings.dslConfig)
    val width = builtSim.length
    val height = builtSim.height
    val materialsFlat = builtSim.toMaterialArray()
    val materials =
        Array(height) { r ->
            Array(width) { c ->
                materialsFlat[r * width + c]
            }
        }

    builtSim.run(settings.iterations.toLong()) { it, i ->
        onUpdate(it.field, materials, i.toInt(), builtSim.ambient)
        false
    }
}
