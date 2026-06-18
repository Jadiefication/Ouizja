use crate::sim::cell::quantum::Quantum;
use crate::sim::material::Material;

/// Contains the physical and material metadata for a cell.
#[derive(Clone, Copy, Debug)]
pub struct Mask {
    /// The current physical state of the cell.
    pub status: Status,
    /// Whether the cell is a constant heat source.
    pub source: bool,
    /// Thermal diffusivity coefficient.
    pub alpha: f64,
    /// The material the cell is made of.
    pub material: Material,
    /// Optional quantum properties for the cell.
    pub quantum: Option<Quantum>,
}

/// Represents the physical state (phase) of a cell.
#[derive(Clone, Copy, PartialEq, Debug)]
#[repr(u8)]
pub enum Status {
    /// Solid state.
    Solid = 0,
    /// Liquid state.
    Liquid = 1,
    /// Gaseous state.
    Gas = 2,
    /// Currently transitioning from solid to liquid.
    Fusing {
        /// Energy absorbed so far for fusion.
        l_energy: f64,
    } = 3,
    /// Currently transitioning from liquid to gas.
    Vaporizing {
        /// Energy absorbed so far for vaporization.
        l_energy: f64,
    } = 4,
}
