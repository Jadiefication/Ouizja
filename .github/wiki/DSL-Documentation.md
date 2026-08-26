# DSL Documentation

## Getting Started with DSL

Ouizja exposes a top-level function for initialization of the DSL in which it exposes its configuration methods. Since the function takes a lambda any valid JVM code is runnable in the DSL and is ran when the simulation is built.

### Initialization

```kotlin
val sim = simulate {
    grid([WIDTH], [HEIGHT])
}
```

## Configuration Methods

### Materials

- **`globalMaterial(Material.[TYPE])`**: Sets the default material for the entire grid.
- **`material(Material.[TYPE], [X1], [X2], [Y1], [Y2])`**: Sets material for a rectangular region.
- **`circle(centerX, centerY, radius, Material.[TYPE])`**: Sets material for a circular region.

**Supported Materials**: `COPPER`, `WATER`, `WOOD`, `ALUMINUM`, `IRON`, `GLASS`, `STONE`, `AIR`, `BARRIER`.

```kotlin
simulate {
    grid(100, 100)
    globalMaterial(Material.AIR)
    material(Material.IRON, 40, 60, 40, 60)
    circle(50, 50, 10, Material.COPPER)
}
```

### Temperature and Heat Sources

- **`globalTemperature([VALUE])`**: Initial temperature in Kelvin for all cells.
- **`temp([VALUE], [X], [Y])`**: Specific cell temperature.
- **`temp([VALUE], [X1], [X2], [Y1], [Y2])`**: Rectangular temperature region.
- **`source([X], [Y])`**: Marks a cell as a constant heat source.
- **`circle(centerX, centerY, radius, [VALUE])`**: Circular temperature region.
- **`circle(centerX, centerY, radius, source = [BOOLEAN])`**: Circular source/sink region.

```kotlin
simulate {
    grid(100, 100)
    globalTemperature(293.15)
    circle(50, 50, 5, 1000.0)
    circle(50, 50, 5, source = true)
}
```

### Environment and Advanced

- **`ambient([VALUE])`**: Sets the environment temperature (affects radiation and Newton cooling).
- **`wind([X] to [Y], [TEMP])`**: Adds a wind vector affecting fluid advection.
- **`noise(noiseModule, scale) { value, x, y -> ... }`**: Applies a noise distribution (requires `NoiseModule`).

```kotlin
simulate {
    grid(128, 128)
    ambient(293.15)
    wind(2.0 to 1.0, 300.0)
}
```

## Execution

- **`run([ITERATIONS])`**:
  - Runs the given simulation ITERATIONS amount and then returns a `BuiltSim` for simple DX inspection.
- **`run([ITERATIONS], [PREDICATE])`**:
  - Runs the given simulation ITERATIONS amount and on each step checks if the PREDICATE evaluates to true, in that case the simulation shortcircuits and returns the `BuiltSim` at that stage. 

```kotlin
val result = sim.run(100)
val result2 = sim.run(100) { sim, i ->
    sim.field[i][i] > 500
}
```
