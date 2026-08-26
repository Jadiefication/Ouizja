/// Represents quantum properties for a cell, used for complex heat dissipation models.
///
/// The quantum model simulates non-linear heat decay over time, which can be used to model
/// effects that don't follow standard classical thermal conduction alone.
#[derive(Clone, Copy, Debug)]
pub struct Quantum {
    /// Decay factor (gamma), representing the current "quantum" state or intensity.
    pub gamma: f64,
    /// Dissipation coefficient (kappa), controlling the rate of decay.
    pub kappa: f64,
    /// Exponent index for temperature dependence in the decay function.
    pub index: i32,
}

impl Quantum {
    /// Calculates the next value of gamma based on current temperature and time step.
    ///
    /// The formula used is: `gamma_new = gamma * exp(-kappa * temp^index * d_t)`
    ///
    /// # Arguments
    /// * `temp` - Current temperature of the cell.
    /// * `d_t` - Time step size.
    pub fn get_next(&self, temp: f64, d_t: f64) -> f64 {
        self.gamma * (-self.kappa * temp.powi(self.index) * d_t).exp()
    }
}
