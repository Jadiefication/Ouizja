<div align="center">

  <img alt="Ouizja logo" src=".github/logo.svg" width="160" height="160" />
  <h1>Ouizja</h1>
  <p>A high-performance thermal simulation engine developed in Rust with a Kotlin/Compose multiplatform interface.</p>

  <p>
    <a href="https://github.com/Jadiefication/Ouizja"><img alt="GitHub" src="https://img.shields.io/github/v/release/Jadiefication/Ouizja?include_prereleases"></a>
    <a href="https://rust-lang.org"><img alt="Rust" src="https://img.shields.io/badge/rust-2024-blue.svg?logo=rust"></a>
    <a href="LICENSE"><img alt="License" src="https://img.shields.io/badge/license-MIT-blue.svg"></a>
    <a href="https://github.com/Jadiefication/Ouizja/actions"><img alt="CI" src="https://github.com/Jadiefication/Ouizja/workflows/CI/badge.svg"></a>
  </p>
</div>

Ouizja is a comprehensive thermal simulation platform that combines the performance of Rust with a modern, reactive user interface. It provides:

- **Thermal Simulation Core**: Efficient heat transfer and thermodynamics simulation implemented in Rust.
- **Multi-threaded Execution**: Leverages `rayon` for parallelizing simulation computations.
- **JNI Integration**: Seamless communication between the Rust core and the Kotlin environment.
- **Modern UI**: A desktop application built with Compose Multiplatform for visualization and control.
- **DSL Simulation**: A domain-specific language for defining and running simulations.

Quick links

- Security policy: [SECURITY.md](SECURITY.md)
- Contributing guide: [CONTRIBUTING.md](CONTRIBUTING.md)
- Code of Conduct: [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)
- Support: [SUPPORT.md](SUPPORT.md)
- License: MIT ([LICENSE](LICENSE))
- Repository: [GitHub](https://github.com/Jadiefication/Ouizja)

## About the Project

Ouizja is a high-performance 2D thermal simulation engine with its own physics model embeddable in JVM-based applications. It offers a Rust backend for executing the simulation logic and a type-safe DSL on the Kotlin side.

### Why Ouizja?
- **Hybrid Architecture:** Offers low-level speeds with the developer experience of writing Kotlin.
- **Parallel Processing:** Utilizes Rust's `rayon` to handle large-scale thermal grids efficiently.
- **Modern Visualization:** Provides a reactive desktop UI built with Compose Multiplatform.

### Limitations
Through all that it claims to offer, Ouizja is an evolving project that has these limitations:
- **2D Grids:** Currently limited to two-dimensional heat transfer simulations.
- **Memory Bound:** Large grids are limited by available system memory.
- **Discrete Simulation:** Uses a grid-based approach which may not capture all continuous fluid dynamics.
- **Internal Model:** The physics model is custom-built for performance and may require tuning for specific real-world accuracy.

## Demo & Real-World Proof

You can run the Ouizja UI directly to visualize thermal simulations. This allows you to interactively modify parameters and see heat transfer in real-time.

### Running the Desktop UI

1. **Build and Run:**
   ```bash
   ./gradlew :OuizjaUI:desktopApp:run
   ```

2. **Build the Rust core manually (Optional):**
   ```bash
   cd simulation
   cargo build --release
   ```

### Using the Kotlin DSL (Alternative)

If you prefer working in a headless environment, you can use the Kotlin DSL to define and run simulations:

1. **Build the project:**
   ```bash
   ./gradlew build
   ```

2. **Run the tests/examples:**
   ```bash
   ./gradlew test
   ```

## Getting Started

If you want to run Ouizja to try out the thermal simulation:

### Prerequisites
- [Rust 1.80+](https://rustup.rs/)
- [JDK 17+](https://adoptium.net/)
- [Gradle](https://gradle.org/install/) (optional, uses wrapper)

### Local Setup & Build

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Jadiefication/Ouizja.git
   cd Ouizja
   ```

2. **Build and run the UI:**
   ```bash
   ./gradlew :OuizjaUI:desktopApp:run
   ```

3. **Using it in your own project:**
   You can use Ouizja as a dependency through JitPack. Add this to your `build.gradle.kts`:
   ```kotlin
   repositories {
       maven { url = uri("https://jitpack.io") }
   }
   dependencies {
       implementation("com.github.Jadiefication:Ouizja:Tag")
   }
   ```

## Code Examples & Programs

To see how to use the DSL, you can check out the test files which showcase how different thermal scenarios can be calculated:

- [**SimulationTest.kt**](dsl_sim/lib/src/test/kotlin/io/jadie/sim/SimulationTest.kt): Contains various tests showcasing DSL usage, including wind effects and material properties.
- [**LargeGridTemperatureTest.kt**](dsl_sim/lib/src/test/kotlin/io/jadie/sim/LargeGridTemperatureTest.kt): Focuses on performance and stability of large-scale simulations.

## Commands & Scripts

The project uses Cargo and Gradle as its build tools:

- `./gradlew :OuizjaUI:desktopApp:run`: Run the desktop UI.
- `cargo build`: Build the Rust simulation core.
- `./gradlew build`: Build the entire project.
- `cargo fmt`: Format the Rust codebase.

## Principles

#### Performance
Utilizes Rust's safety and speed, along with parallel processing, to handle large-scale thermal grids efficiently.

#### Interoperability
Focuses on a clean bridge between native performance and high-level application logic using JNI.

#### Cross-Platform
Designed with multiplatform support in mind, targeting desktop environments initially.

## Documentation

Starting points for learning about the code structure:

- `Ouizja::sim::grid::Grid` — Thermal grid management in Rust.
- `io.jadie.sim.Simulation` — Kotlin simulation wrapper and DSL.
- `io.jadie.App` — Main Compose application entry point.

## Testing

The test suite focuses on full coverage whilst maintaining ease-of-use.
- Run Rust tests: `cd simulation && cargo test`
- Run Kotlin tests: `./gradlew test`

## Contributing

Contributions are always welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).

## License

[MIT](LICENSE) — © 2025 Jadiefication
