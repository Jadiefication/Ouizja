use crate::sim::cells::quantum::Quantum;
use crate::sim::mask::Status::{Fusing, Gas, Liquid, Solid, Vaporizing};
use crate::sim::material::Material;

/// Contains the physical and material metadata for a cell.
///
/// The `Mask` is a high-level representation of a cell's state, often used
/// during initialization or when preparing data for the native simulation.
#[derive(Clone, Copy, Debug)]
pub struct Mask {
    /// The current physical state of the cell (e.g., Solid, Liquid).
    pub status: Status,
    /// Whether the cell acts as a constant temperature heat source.
    pub source: bool,
    /// Thermal diffusivity coefficient (alpha) in m²/s.
    pub alpha: f64,
    /// The [`Material`] the cell consists of.
    pub material: Material,
    /// Optional [`Quantum`] properties for complex heat dissipation.
    pub quantum: Option<Quantum>,
}

/// Represents the physical state (phase) of a cell.
///
/// The simulation tracks phase changes based on enthalpy milestones.
/// Materials can transition between these states depending on their thermal properties.
#[derive(Clone, Copy, PartialEq, Debug)]
#[repr(u8)]
pub enum Status {
    /// Solid state. Low mobility.
    Solid = 0,
    /// Liquid state. Subject to convection.
    Liquid = 1,
    /// Gaseous state. High mobility and convection.
    Gas = 2,
    /// Currently transitioning from solid to liquid (latent heat of fusion).
    Fusing = 3,
    /// Currently transitioning from liquid to gas (latent heat of vaporization).
    Vaporizing = 4,
}

impl Status {
    /// Converts a numeric ID to a `Status` variant.
    ///
    /// # Panics
    /// Panics if the ID does not correspond to a valid `Status`.
    pub fn find_by_id(id: u8) -> Self {
        match id {
            0 => Solid,
            1 => Liquid,
            2 => Gas,
            3 => Fusing,
            4 => Vaporizing,
            _ => panic!("Invalid ID {}!", id),
        }
    }
}
