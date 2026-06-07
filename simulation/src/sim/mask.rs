use crate::sim::material::Material;

#[derive(Clone, Copy)]
pub struct Mask {
    pub status: Status,
    pub source: bool,
    pub alpha: f64,
    pub material: Material
}

#[derive(Clone, Copy, PartialEq)]
pub enum Status {
    Solid,
    Liquid,
    Gas,
    Fusing {
        l_energy: f64
    },
    Vaporizing {
        l_energy: f64
    }
}