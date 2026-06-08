use crate::sim::cell::therms::ThermalProperties;
use crate::sim::material::Material::{Air, Aluminum, Barrier, Copper, Glass, Iron, Stone, Water, Wood};

#[derive(Clone, Copy, Debug, PartialEq, Eq)]
#[repr(u8)]
pub enum Material {
    Copper = 0,
    Water = 1,
    Wood = 2,
    Aluminum = 3,
    Iron = 4,
    Glass = 5,
    Stone = 6,
    Air = 7,
    Barrier = 8,
}

impl Material {
    pub fn find_by_id(id: u8) -> Material {
        match id {
            0 => Copper,
            1 => Water,
            2 => Wood,
            3 => Aluminum,
            4 => Iron,
            5 => Glass,
            6 => Stone,
            7 => Air,
            _ => Barrier
        }
    }

    pub fn thermal_properties(&self) -> ThermalProperties {
        match self {
            Copper => ThermalProperties {
                specific_heat_solid: 385.0,
                specific_heat_liquid: Some(490.0),
                specific_heat_gas: Some(380.0),
                latent_fusion: Some(205_000.0),
                latent_vaporization: Some(4_730_000.0),
                melting_point: Some(1358.15), // 1085°C
                boiling_point: Some(2835.15), // 2562°C
                diffusivity: 165.0,
            },
            Water => ThermalProperties {
                specific_heat_solid: 2108.0,
                specific_heat_liquid: Some(4184.0),
                specific_heat_gas: Some(1996.0),
                latent_fusion: Some(334_000.0),
                latent_vaporization: Some(2_260_000.0),
                melting_point: Some(273.15),  // 0°C
                boiling_point: Some(373.15),  // 100°C
                diffusivity: 0.14
            },
            Aluminum => ThermalProperties {
                specific_heat_solid: 900.0,
                specific_heat_liquid: Some(1180.0),
                specific_heat_gas: Some(465.0),
                latent_fusion: Some(397_000.0),
                latent_vaporization: Some(10_900_000.0),
                melting_point: Some(933.47),  // 660.3°C
                boiling_point: Some(2792.15), // 2519°C
                diffusivity: 97.0
            },
            Iron => ThermalProperties {
                specific_heat_solid: 450.0,
                specific_heat_liquid: Some(820.0),
                specific_heat_gas: Some(570.0),
                latent_fusion: Some(247_000.0),
                latent_vaporization: Some(6_080_000.0),
                melting_point: Some(1811.15), // 1538°C
                boiling_point: Some(3134.15), // 2861°C
                diffusivity: 23.0
            },
            Air => ThermalProperties {
                specific_heat_solid: 1005.0,
                specific_heat_liquid: Some(1005.0),
                specific_heat_gas: Some(1005.0),
                latent_fusion: Some(23_000.0),
                latent_vaporization: Some(200_000.0),
                melting_point: Some(194.65),
                boiling_point: Some(194.65),
                diffusivity: 19.0
            },
            Glass => ThermalProperties {
                specific_heat_solid: 840.0,
                specific_heat_liquid: None,
                specific_heat_gas: None,
                latent_fusion: None,
                latent_vaporization: None,
                melting_point: None,
                boiling_point: None,
                diffusivity: 0.34
            },
            Wood => ThermalProperties {
                specific_heat_solid: 1700.0,
                specific_heat_liquid: None,
                specific_heat_gas: None,
                latent_fusion: None,
                latent_vaporization: None,
                melting_point: None,
                boiling_point: None,
                diffusivity: 0.08
            },
            Stone => ThermalProperties {
                specific_heat_solid: 840.0,
                specific_heat_liquid: None,
                specific_heat_gas: None,
                latent_fusion: None,
                latent_vaporization: None,
                melting_point: None,
                boiling_point: None,
                diffusivity: 0.5
            },
            Barrier => ThermalProperties {
                specific_heat_solid: f64::MAX,
                specific_heat_liquid: None,
                specific_heat_gas: None,
                latent_fusion: None,
                latent_vaporization: None,
                melting_point: None,
                boiling_point: None,
                diffusivity: 0.0
            }
        }
    }
}