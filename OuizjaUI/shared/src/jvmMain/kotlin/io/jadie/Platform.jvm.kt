package io.jadie

import io.jadie.sim.Material
import io.jadie.sim.simulate
import io.jadie.sim.toMaterialArray

actual fun runSimulation(
    settings: SimSettings,
    onUpdate: (Array<DoubleArray>, Array<Array<Material>>, Int) -> Unit,
) {
    val width = settings.width
    val height = settings.height
    val sim =
        simulate {
            grid(width, height)
            globalMaterial(settings.globalMaterial)
            globalTemperature(settings.globalTemp)

            if (width >= 25 && height >= 25) {
                temp(100.0, width / 2, height / 2)
            }
        }

    val materialsFlat = sim.toMaterialArray()
    val materials =
        Array(height) { r ->
            Array(width) { c ->
                materialsFlat[r * width + c]
            }
        }

    for (i in 0..settings.iterations) {
        val state = sim.run(1)
        onUpdate(state.field, materials, i)
    }
}
