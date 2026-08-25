<div align="center">

  <img alt="Ouizja logo" src=".github/logo.svg" width="160" height="160" />
  <h1>Ouizja</h1>
  <p></p>
    A thermal diffusion engine powered by a Rust backend with a KMP UI
  <p>
    <a href="https://github.com/Jadiefication/Ouizja"><img alt="GitHub" src="https://img.shields.io/github/v/release/Jadiefication/Ouizja?include_prereleases"></a>
    <a href="https://rust-lang.org"><img alt="Rust" src="https://img.shields.io/badge/rust-2024-blue.svg?logo=rust"></a>
    <a href="LICENSE"><img alt="License" src="https://img.shields.io/badge/license-MIT-blue.svg"></a>
    <a href="https://github.com/Jadiefication/Ouizja/actions"><img alt="CI" src="https://github.com/Jadiefication/Ouizja/workflows/CI/badge.svg"></a>
  </p>
</div>

Ouizja is a project that utilizes a Rust backend alongside with a KMP UI to showcase heat spreading in a 2D grid. It's features include:

- **Performant Rust Core**: Utilizes Rust alognside Rayon to achieve massive performance increases.
- **Seamless JNI Integration**: Simple use of the Rust core in JVM applications with its DSL.
- **Modern UI**: An app to visualize the termal engine built with KMP.

Quick links

- Security policy: [SECURITY.md](SECURITY.md)
- Contributing guide: [CONTRIBUTING.md](CONTRIBUTING.md)
- Code of Conduct: [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)
- Support: [SUPPORT.md](SUPPORT.md)
- License: MIT ([LICENSE](LICENSE))
- Repository: [GitHub](https://github.com/Jadiefication/Ouizja)

## About the Project

Ouizja is a thermal diffusion engine that showcases the spread of heat through space in a 2D grid. It uses a Rust core to achieve performance gains normal JVM apps can't and then communicates it through JNI to the JVM for seamless DX.

### Why Ouizja?

- **Performance-strived Architecture**: Gives a great DX alongside its native performance.
- **Simple Visualization**: Offers a simple but reactive UI to showcase the 2D spatial grid.

### Limitations

- **2D Grid**: Due to RAM constraints the engine is built to only handle 2D grids.
- **RAM Limitations**: Size of the grid is constrained by available RAM.
- **Grid-Based Simulation**: Fluid dynamics can't be properly handled in grid-based simulations.
- **Efficiency-Based Physics Model**: The simulation is meant to present a middle ground between real-world physics and possible computational simulation, values might need to be tweaked for extra precision.

## Demo & Real-World Proof

### Running the Desktop UI

1. **Build and Run:**
   ```bash
   ./gradlew :OuizjaUI:desktopApp:run
   ```

### Using the Kotlin DSL


1. **Build the project:**
   ```bash
   ./gradlew build
   ```

2. **Run the tests/examples:**
   ```bash
   ./gradlew test
   ```

## Getting Started

For trying out Ouizja(by using the internal tests or development), follow these steps:

### Prerequisites
- [Rust 1.80+](https://rustup.rs/)
- [JDK 17+](https://adoptium.net/)
- [Gradle](https://gradle.org/install/) (optional, uses wrapper)

### Local UI Setup & Build

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Jadiefication/Ouizja.git
   cd Ouizja
   ```

2. **Build and run the UI:**
   ```bash
   ./gradlew :OuizjaUI:desktopApp:run
   ```

### Local Development

1. **Add it as a Dependency:**

   ```kotlin
   repositories {
       maven { url = uri("https://jitpack.io") }
   }
   dependencies {
       implementation("com.github.Jadiefication:Ouizja:Tag")
   }
   ```

## Code Examples & Programs

- [**SimulationTest.kt**](dsl_sim/lib/src/test/kotlin/io/jadie/sim/SimulationTest.kt): Showcases different uses of the available functions in small scoped simulations.
- [**LargeGridTemperatureTest.kt**](dsl_sim/lib/src/test/kotlin/io/jadie/sim/LargeGridTemperatureTest.kt): Focuses on showcasing performance and handling of largely scoped simulations.

## Commands & Scripts

- `cd OuizjaUI && ./gradlew :desktopApp:run`: Open the App.
- `cd simulation && cargo build`: Build the Rust Core.
- `cd dsl_sim && ./gradlew build`: Build the Kotlin DSL.

## Principles

#### Performance
By using Rust it can achieve native performance, alongside Rayon to parallerly process the grid.

#### Cross-Platform
By leveraging the JVM it can run on all platforms with native performance by leveraging Rust.

## Documentation

- `Ouizja::sim::grid::Grid`: The Grid memory definition alongside it's processing functions.
- `io.jadie.sim.Simulation`: Kotlin simulation wrapper and DSL.
- `io.jadie.App`: KMP starting point.

## Testing

- Run Rust tests: `cd simulation && cargo test`
- Run Kotlin tests: `cd dsl_sim && ./gradlew test`

## Contributing

Contributions are always welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

## License

[MIT](LICENSE) — © 2026 Jadiefication
