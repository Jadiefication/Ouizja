use crate::sim::cells::quantum::Quantum;
use crate::sim::mask::Status::{Fusing, Gas, Liquid, Solid, Vaporizing};
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
    Fusing = 3,
    /// Currently transitioning from liquid to gas.
    Vaporizing = 4,
}

impl Status {
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
