package io.jadie

import io.jadie.sim.Material
import io.jadie.sim.simulate

actual fun runSimulation() {
    val sim = simulate {
        grid(10, 10)
        globalMaterial(Material.IRON)
        globalTemperature(20.0)
        temp(100.0, 5, 5)
    }
    val result = sim.run(100)
    println("Simulation finished with temperature at (5,5): ${result.field[5][5]}")
}