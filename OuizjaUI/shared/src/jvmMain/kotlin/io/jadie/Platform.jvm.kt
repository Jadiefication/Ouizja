package io.jadie

import io.jadie.sim.Material
import io.jadie.sim.simulate
import io.jadie.sim.toMaterialArray

actual fun runSimulation(
    settings: SimSettings,
    onUpdate: (Array<DoubleArray>, Array<Array<Material>>, Int) -> Unit,
) {
    val parser = DslParser()
    val simulationBuilder = parser.parse(settings.dslConfig)

    val builtSim =
        simulate {
            grid(simulationBuilder.length, simulationBuilder.height)
            ambient(simulationBuilder.ambient)

            // Transfer internal masks if they were set
            if (simulationBuilder.materialMask.isNotEmpty()) {
                this.materialMask = simulationBuilder.materialMask
            }
            if (simulationBuilder.temps.isNotEmpty()) {
                this.temps = simulationBuilder.temps
            }
            if (simulationBuilder.sourceMask.isNotEmpty()) {
                this.sourceMask = simulationBuilder.sourceMask
            }
        }

    val width = builtSim.length
    val height = builtSim.height
    val materialsFlat = builtSim.toMaterialArray()
    val materials =
        Array(height) { r ->
            Array(width) { c ->
                materialsFlat[r * width + c]
            }
        }

    for (i in 0..settings.iterations) {
        val state = builtSim.run(1)
        onUpdate(state.field, materials, i)
    }
}
