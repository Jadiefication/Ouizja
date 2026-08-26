# UI Usage Guide

## Interface Overview

Ouizja uses a modern UI style to focus on simplicity and ease-of-use. It consists of a side panel and a visualization grid.

### Side Panel Controls

- **DSL Editor**:
  - Main driver of the simulation where it's settings are defined through the DSL.
- **Simulation Controls**:
  - Controls the simulation, can stop, start and restart it.
- **Iteration Settings**:
  - Defines the amount of iterations the simulation should be ran for.
- **View Mode Toggle**:
  - Switches between showcasing the temperature grid and material grid.

### Visualization Grid

- **Temperature Heatmap**:
  - Shows each cell alongside a color attributed to its temperature.
- **Material Distribution**:
  - Shows each cell alongside a color attributed to its material.
- **Real-time Inspection**:
  - Upon hovering on a cell its information becomes visible in the inspection tool.

## DSL Configuration in UI

### Usage

```kotlin
grid(100, 100)
globalMaterial(Material.AIR)
circle(50, 50, 10, 1000.0)
```

## DSL Differences: UI vs Library

### Syntax and Wrapping

- **Implicit `simulate` Block**:
  - The UI passed the code to the backend when it's ran in the simulate DSL block.

### Execution Environment

- **Runtime Interpretation**:
  - The simulation uses `javax.script` to pass it to a KTS evaluator for simple evaluation of the simulation's settings.
- **Predefined Imports**:
  - The simulation automatically imports all the things under `io.jadie.sim.*` to control the simulation.
- **Return Type Requirement**:
  - The simulation under the hood then runs itself and returns a BuiltSim to the UI each iteration to update itself.

The UI evaluates the settings syntax at Runtime, if it failes to compiles it throws an exception.
