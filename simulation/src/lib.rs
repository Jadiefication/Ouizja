//! Ouizja Simulation Core
//!
//! This crate provides the native implementation of the Ouizja thermal simulation.
//! It includes the physical model, grid management, and JNI bindings for Kotlin.
//!
//! The simulation is based on a grid of cells, each with its own thermal properties,
//! material, and physical state. Heat transfer is modeled using conduction, convection
//! (wind and buoyancy), and radiation.
//!
//! Main components:
//! - [`Grid`]: The main simulation grid and engine.
//! - [`Material`]: Enumeration of supported materials and their thermal properties.
//! - [`Status`]: The physical state of a cell (solid, liquid, gas, etc.).
//! - [`jni`]: JNI bindings for interacting with the simulation from Kotlin.

pub mod float;
pub mod jni;
pub mod sim;
