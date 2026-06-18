/// Represents quantum properties for a cell, used for complex heat dissipation models.
#[derive(Clone, Copy, Debug)]
pub struct Quantum {
    /// Decay factor (gamma).
    pub gamma: f64,
    /// Dissipation coefficient (kappa).
    pub kappa: f64,
    /// Exponent index for temperature.
    pub index: i32,
}

impl Quantum {
    /// Calculates the next value of gamma based on current temperature and time step.
    pub fn get_next(&self, temp: f64, d_t: f64) -> f64 {
        self.gamma * (-self.kappa * temp.powi(self.index) * d_t).exp()
    }
}
