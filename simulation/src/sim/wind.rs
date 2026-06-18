use haje::vec::vec2::Vec2;

/// Represents a wind force affecting the simulation.
#[derive(Clone)]
pub struct Wind {
    /// The force vector of the wind.
    pub(crate) force: Vec2<f64>,
    /// The temperature of the wind.
    pub(crate) temp: f64,
}
