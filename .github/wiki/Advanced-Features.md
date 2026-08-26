# Advanced Simulation Features

## Fluid Dynamics and Wind

Materials in Ouizja's thermal simulations can either be SOLID, GAS or LIQUID, where as long as it's not SOLID we can use external forces to apply advection onto our grid. Ouizja's solution to these external forces are winds that are defined with a direction vector and a temperature it carries.

### Configuring Wind

- **`wind([X] to [Y], [TEMP])`**:
  - Creates a wind with the direction vector (X, Y) and a carried temperature TEMP.

```kotlin
simulate {
    grid(10, 10)
    globalMaterial(Material.AIR)
    wind(5.0 to 2.0, 300.0)
}
```

## Quantum States

Ouizja also supports putting cells into a form of superposition to simulate quantum state decay caused by heat. Because of this Ouizja can also be used to simulate how a cell's quantum state can survive under varying temperatures.

### Adding Superpositions

- **`superposition(x, y, kappa, index, gamma)`**:
    - `x`, `y`: Cell coordinates.
    - `kappa`: Dissipation coefficient.
    - `index`: Temperature exponent for decay.
    - `gamma`: Initial intensity (default `1.0`).

**Decay Formula**: `gamma_new = gamma * exp(-kappa * temp^index * delta_t)`

```kotlin
simulate {
    grid(100, 100)
    // High dissipation quantum cell
    superposition(x = 50, y = 50, kappa = 0.05, index = 2, gamma = 1.0)
}
```

## Barrier and Insulation

To create the perfect insulation, you can define a barrier. The barrier acts like a perfect insulator where it doesn't let any heat passthrough.

### Defining Barriers

- **`barrier([X1], [X2], [Y1], [Y2])`**:
  - Defines a rectangular barrier area from (X1, Y1) to (X2, Y2)

```kotlin
simulate {
    grid(10, 10)
    barrier(0, 10, 5, 5) // Horizontal wall
}
```
