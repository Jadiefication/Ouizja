use crate::sim::cell::quantum::Quantum;
use crate::sim::material::Material;

#[derive(Clone, Copy, Debug)]
pub struct Mask {
    pub status: Status,
    pub source: bool,
    pub alpha: f64,
    pub material: Material,
    pub quantum: Option<Quantum>
}

#[derive(Clone, Copy, PartialEq, Debug)]
#[repr(u8)]
pub enum Status {
    Solid = 0,
    Liquid = 1,
    Gas = 2,
    Fusing {
        l_energy: f64
    } = 3,
    Vaporizing {
        l_energy: f64
    } = 4
}