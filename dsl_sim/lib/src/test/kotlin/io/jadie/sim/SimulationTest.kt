package io.jadie.sim

import io.jadie.SimState
import kotlin.test.*

class SimulationTest {

    @Test
    fun testGridInitialization() {
        val sim = simulate {
            grid(10, 20)
        }
        assertEquals(10, sim.length)
        assertEquals(20, sim.height)
    }

    @Test
    fun testGlobalMaterial() {
        val sim = simulate {
            grid(2, 2)
            globalMaterial(Material.COPPER)
        }
        val expectedAlpha = DoubleArray(4) { Material.COPPER.diffusivity }
        assertContentEquals(expectedAlpha, sim.alphaMask)
    }

    @Test
    fun testLocalMaterial() {
        val sim = simulate {
            grid(2, 2)
            material(Material.IRON, 0, 0, 0, 1)
        }
        // index = x * height + y
        // (0,0) -> 0*2+0 = 0
        // (0,1) -> 0*2+1 = 1
        // (1,0) -> 1*2+0 = 2
        // (1,1) -> 1*2+1 = 3
        val expectedAlpha = doubleArrayOf(
            Material.IRON.diffusivity, Material.IRON.diffusivity,
            Material.BARRIER.diffusivity, Material.BARRIER.diffusivity
        )
        assertContentEquals(expectedAlpha, sim.alphaMask)
    }

    @Test
    fun testGlobalTemperature() {
        val sim = simulate {
            grid(2, 2)
            globalTemperature(100.0)
        }
        val expectedTemps = DoubleArray(4) { 100.0 }
        assertContentEquals(expectedTemps, sim.temps)
    }

    @Test
    fun testLocalTemperature() {
        val sim = simulate {
            grid(2, 2)
            temp(50.0, 1, 0)
        }
        // (1,0) -> 1*2+0 = 2
        val expectedTemps = doubleArrayOf(0.0, 0.0, 50.0, 0.0)
        assertContentEquals(expectedTemps, sim.temps)
    }

    @Test
    fun testSource() {
        val sim = simulate {
            grid(2, 2)
            source(1, 1)
        }
        // (1,1) -> 1*2+1 = 3
        val expectedSource = booleanArrayOf(false, false, false, true)
        assertContentEquals(expectedSource, sim.sourceMask)
    }

    @Test
    fun testBarrier() {
        val sim = simulate {
            grid(2, 2)
            globalMaterial(Material.COPPER)
            barrier(0, 0, 0, 0)
        }
        // (0,0) -> 0
        val expectedAlpha = doubleArrayOf(
            Material.BARRIER.diffusivity, Material.COPPER.diffusivity,
            Material.COPPER.diffusivity, Material.COPPER.diffusivity
        )
        assertContentEquals(expectedAlpha, sim.alphaMask)
    }

    @Test
    fun testTransform() {
        val oldSim = simulate {
            grid(2, 2)
            globalTemperature(10.0)
        }
        val newState = SimState(
            arrayOf(
                doubleArrayOf(1.0, 2.0),
                doubleArrayOf(3.0, 4.0)
            )
        )
        val newSim = transform(oldSim, newState)

        assertEquals(oldSim.height, newSim.height)
        assertEquals(oldSim.length, newSim.length)
        assertContentEquals(oldSim.alphaMask, newSim.alphaMask)
        assertContentEquals(oldSim.sourceMask, newSim.sourceMask)
        assertContentEquals(doubleArrayOf(1.0, 2.0, 3.0, 4.0), newSim.temps)
    }

    @Test
    fun testSimulationEquality() {
        val sim1 = simulate {
            grid(2, 2)
            globalMaterial(Material.COPPER)
            globalTemperature(100.0)
            source(0, 0)
        }
        val sim2 = simulate {
            grid(2, 2)
            globalMaterial(Material.COPPER)
            globalTemperature(100.0)
            source(0, 0)
        }
        val sim3 = simulate {
            grid(2, 2)
            globalMaterial(Material.IRON)
            globalTemperature(100.0)
            source(0, 0)
        }

        assertEquals(sim1, sim2)
        assertEquals(sim1.hashCode(), sim2.hashCode())
        assertNotEquals(sim1, sim3)
    }

    @Test
    fun testSimulationRun() {
        val sim = simulate {
            grid(5, 5)
            globalMaterial(Material.COPPER)
            globalTemperature(20.0)
            temp(100.0, 2, 2)
            source(2, 2)
        }

        val iterations = 10L
        val result = sim.run(iterations)

        assertNotNull(result)
        assertEquals(5, result.field.size)
        assertEquals(5, result.field[0].size)

        // Since (2,2) is a source, it should remain at 100.0
        assertEquals(100.0, result.field[2][2], 0.001)

        // Other cells should have warmed up from 20.0
        assertTrue(result.field[2][1] > 20.0, "Cell (2,1) should have warmed up")
        assertTrue(result.field[1][2] > 20.0, "Cell (1,2) should have warmed up")
    }

    @Test
    fun testTinyGrid() {
        val sim = simulate {
            grid(1, 1)
            globalTemperature(50.0)
            source(0, 0)
        }
        assertEquals(1, sim.length)
        assertEquals(1, sim.height)
        val result = sim.run(10)
        assertEquals(50.0, result.field[0][0])
    }

    @Test
    fun testUniformGridStability() {
        val sim = simulate {
            grid(3, 3)
            globalMaterial(Material.IRON)
            globalTemperature(25.0)
        }
        val result = sim.run(100)
        for (row in result.field) {
            for (temp in row) {
                assertEquals(25.0, temp, 0.0001, "Uniform grid should remain stable")
            }
        }
    }

    @Test
    fun testMultipleSources() {
        val sim = simulate {
            grid(5, 5)
            globalMaterial(Material.ALUMINUM)
            globalTemperature(0.0)
            temp(100.0, 0, 0)
            source(0, 0)
            temp(100.0, 4, 4)
            source(4, 4)
        }
        val result = sim.run(50)
        assertEquals(100.0, result.field[0][0])
        assertEquals(100.0, result.field[4][4])
        // Center should be warmed up by both
        assertTrue(result.field[2][2] > 0.0)
    }

    @Test
    fun testBarrierStopsHeat() {
        val sim = simulate {
            grid(3, 1) // 3x1 grid: Source - Barrier - Cold
            globalTemperature(0.0)
            temp(100.0, 0, 0)
            source(0, 0)
            barrier(1, 1, 0, 0)
            material(Material.COPPER, 2, 2, 0, 0)
        }

        // Before run check
        assertEquals(Material.BARRIER.diffusivity, sim.alphaMask[1])

        val result = sim.run(100)
        assertEquals(100.0, result.field[0][0])
        assertEquals(0.0, result.field[1][0], "Barrier should not change temp (alpha=0)")
        assertEquals(0.0, result.field[2][0], "Heat should not pass through barrier")
    }

    @Test
    fun testTransformPreservesStructure() {
        val sim1 = simulate {
            grid(2, 2)
            globalMaterial(Material.IRON)
            source(0, 0)
            globalTemperature(10.0)
        }

        val state = SimState(
            arrayOf(
                doubleArrayOf(100.0, 100.0),
                doubleArrayOf(100.0, 100.0)
            )
        )

        val sim2 = transform(sim1, state)

        assertEquals(sim1.length, sim2.length)
        assertEquals(sim1.height, sim2.height)
        assertContentEquals(sim1.alphaMask, sim2.alphaMask)
        assertContentEquals(sim1.sourceMask, sim2.sourceMask)
        assertContentEquals(doubleArrayOf(100.0, 100.0, 100.0, 100.0), sim2.temps)
    }

    @Test
    fun testMaterialLayering() {
        val sim = simulate {
            grid(4, 4)
            globalMaterial(Material.WOOD) // default
            material(Material.IRON, 0, 1, 0, 3) // Left half
        }

        // Check Iron area
        for (x in 0..1) {
            for (y in 0..3) {
                assertEquals(Material.IRON.diffusivity, sim.alphaMask[x * 4 + y])
            }
        }
        // Check Wood area
        for (x in 2..3) {
            for (y in 0..3) {
                assertEquals(Material.WOOD.diffusivity, sim.alphaMask[x * 4 + y])
            }
        }
    }
}
