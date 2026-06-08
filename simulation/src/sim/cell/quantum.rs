#[derive(Clone, Copy, Debug)]
pub struct Quantum {
    pub gamma: f64,
    pub kappa: f64,
    pub index: i32
}

impl Quantum {
    pub fn get_next(&self, temp: f64, d_t: f64) -> f64 {
        self.gamma * (-self.kappa * temp.powi(self.index) * d_t).exp()
    }
}