package io.jadie.sim

import kotlin.test.*

class LargeGridTemperatureTest {
    private fun getIndex(
        x: Int,
        y: Int,
        height: Int,
    ): Int = x * height + y

    @Test
    fun testLargeGridReproduction() {
        val h = 128
        val sim =
            simulate {
                // 1. Grid domain setup
                grid(128, 128)
                ambient(293.15) // Use 293.15 as ambient to match globalTemperature

                // 2. Global initial states
                globalMaterial(Material.STONE)
                globalTemperature(293.15)

                // 3. Perimeter insulating walls (5-cell border)
                barrier(fromX = 0, toX = 127, fromY = 0, toY = 4) // Bottom
                barrier(fromX = 0, toX = 127, fromY = 123, toY = 127) // Top
                barrier(fromX = 0, toX = 4, fromY = 0, toY = 127) // Left
                barrier(fromX = 123, toX = 127, fromY = 0, toY = 127) // Right

                // 4. Central heat deflector barrier
                barrier(fromX = 44, toX = 84, fromY = 60, toY = 63)

                // 5. Hot core (Circle radius 12 at 850.0 K)
                circle(centerX = 32, centerY = 32, radius = 12, temp = 850.0)
                circle(centerX = 32, centerY = 32, radius = 12, source = true)

                // 6. Cold sink (Circle radius 10 at 100.0 K)
                circle(centerX = 96, centerY = 96, radius = 10, temp = 100.0)

                // 7. Point source thermal injection
                source(x = 64, y = 20)
                temp(temp = 1200.0, x = 64, y = 20)

                // 8. Wind force vector: (dx = 0.5, dy = -0.2) at 310.0 K
                wind(force = Pair(0.5, -0.2), temp = 310.0)

                // 9. Quantum superposition coupling nodes
                superposition(x = 48, y = 48, kappa = 0.05, index = 0)
                superposition(x = 80, y = 48, kappa = 0.05, index = 1)
            }

        // Verify Point Source (x=64, y=20)
        val pointIndex = getIndex(64, 20, h)
        assertEquals(1200.0, sim.temps[pointIndex], "Point source temperature should be 1200.0")
        assertTrue(sim.sourceMask[pointIndex], "Point source should be marked as source")

        // Verify Hot Core (32, 32) radius 12
        val coreCenterIndex = getIndex(32, 32, h)
        assertEquals(850.0, sim.temps[coreCenterIndex], "Core center temperature should be 850.0")
        assertTrue(sim.sourceMask[coreCenterIndex], "Core center should be marked as source")

        // Verify Cold Sink (96, 96) radius 10
        val sinkCenterIndex = getIndex(96, 96, h)
        assertEquals(100.0, sim.temps[sinkCenterIndex], "Sink center temperature should be 100.0")

        // Verify barriers (5-cell border)
        // Bottom border (0,0) to (127,4)
        assertEquals(Material.BARRIER.id, sim.materialMask[getIndex(0, 0, h)])
        assertEquals(Material.BARRIER.id, sim.materialMask[getIndex(0, 4, h)])
        assertEquals(Material.BARRIER.id, sim.materialMask[getIndex(127, 0, h)])
        assertEquals(Material.BARRIER.id, sim.materialMask[getIndex(127, 4, h)])

        // Center area (64, 64) should be STONE
        val index64_64 = getIndex(64, 64, h)
        val actualMaterialId = sim.materialMask[index64_64]
        val actualMaterial = Material.entries.find { it.id == actualMaterialId }
        assertEquals(
            Material.STONE.id,
            actualMaterialId,
            "Material at (64, 64) should be STONE, but was $actualMaterial (id=$actualMaterialId)",
        )

        // Central heat deflector barrier (44, 60) to (84, 68)
        assertEquals(Material.BARRIER.id, sim.materialMask[getIndex(44, 60, h)])
        assertEquals(Material.BARRIER.id, sim.materialMask[getIndex(84, 63, h)])

        // 10. Run simulation
        val iterations = 5000L
        val result = sim.run(iterations)

        // Verify state after simulation
        // Point Source (64, 20) should still be 1200.0 (it's a source)
        assertEquals(1200.0, result.field[64][20], 0.001, "Point source should maintain temp")

        // Hot Core center (32, 32) should still be 850.0
        assertEquals(850.0, result.field[32][32], 0.001, "Hot core center should maintain temp")

        // Cold Sink center (96, 96) should still be 100.0 (Wait, circle(source=true) was only called for Hot Core)
        // In the DSL:
        // circle(centerX = 96, centerY = 96, radius = 10, temp = 100.0)
        // It's NOT a source, so it should start warming up if surroundings are warmer,
        // but it's in a STONE globalMaterial (293.15).
        // Actually, it should stay exactly 100.0 if it WERE a source.
        // Let's check if it changed.
        assertTrue(result.field[96][96] >= 100.0, "Cold sink should not get colder than 100.0")

        // Heat should have spread to (64, 21) from Point Source (64, 20)
        assertTrue(result.field[64][21] > 293.15, "Heat should spread from point source to (64, 21). Was ${result.field[64][21]}")

        // Heat should have spread from Hot Core
        assertTrue(result.field[32][45] > 293.15, "Heat should spread from hot core to (32, 45). Was ${result.field[32][45]}")

        // 11. Verify Barrier Isolation
        // Barrier is at fromX = 44, toX = 84, fromY = 60, toY = 68
        // Point source is at (64, 20). Hot core center at (32, 32).
        // Let's check a point "behind" the barrier from the hot core.
        // Hot core is at (32, 32). Barrier is at y=60..68.
        // Point (64, 80) is behind the barrier.
        // It should still be near ambient if the barrier works.
        assertTrue(result.field[64][80] < 300.0, "Point (64, 80) should stay relatively cool due to barrier. Was ${result.field[64][80]}")
    }

    @Test
    fun testTemperaturePreservation() {
        val h = 10
        val sim =
            simulate {
                grid(10, 10)
                globalTemperature(293.15)
                temp(1000.0, 5, 5)
                circle(centerX = 2, centerY = 2, radius = 1, temp = 500.0)
                temp(temp = 800.0, fromX = 7, toX = 9, fromY = 7, toY = 9)
            }

        assertEquals(1000.0, sim.temps[getIndex(5, 5, h)], "Single point temp should be preserved")
        assertEquals(500.0, sim.temps[getIndex(2, 2, h)], "Circle temp should be set")
        assertEquals(800.0, sim.temps[getIndex(7, 7, h)], "Rect temp should be set")
        assertEquals(293.15, sim.temps[getIndex(0, 0, h)], "Background temp should be preserved")
    }

    @Test
    fun testSourcePreservation() {
        val h = 10
        val sim =
            simulate {
                grid(10, 10)
                source(5, 5)
                circle(centerX = 2, centerY = 2, radius = 1, source = true)
            }

        assertTrue(sim.sourceMask[getIndex(5, 5, h)], "Single point source should be preserved")
        assertTrue(sim.sourceMask[getIndex(2, 2, h)], "Circle source should be set")
        assertFalse(sim.sourceMask[getIndex(0, 0, h)], "Non-source should remain false")
    }
}
