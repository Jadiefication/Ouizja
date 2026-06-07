use crate::sim::cell::ThermalProperties;

#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum Material {
    Copper,
    Water,
    Wood,
    Aluminum,
    Iron,
    Glass,
    Stone,
    Air,
}

impl Material {
    pub fn thermal_properties(&self) -> ThermalProperties {
        match self {
            Material::Copper => ThermalProperties {
                specific_heat_solid: 385.0,
                specific_heat_liquid: Some(490.0),
                specific_heat_gas: Some(380.0),
                latent_fusion: Some(205_000.0),
                latent_vaporization: Some(4_730_000.0),
                melting_point: Some(1358.15), // 1085°C
                boiling_point: Some(2835.15), // 2562°C
            },
            Material::Water => ThermalProperties {
                specific_heat_solid: 2108.0,
                specific_heat_liquid: Some(4184.0),
                specific_heat_gas: Some(1996.0),
                latent_fusion: Some(334_000.0),
                latent_vaporization: Some(2_260_000.0),
                melting_point: Some(273.15),  // 0°C
                boiling_point: Some(373.15),  // 100°C
            },
            Material::Aluminum => ThermalProperties {
                specific_heat_solid: 900.0,
                specific_heat_liquid: Some(1180.0),
                specific_heat_gas: Some(465.0),
                latent_fusion: Some(397_000.0),
                latent_vaporization: Some(10_900_000.0),
                melting_point: Some(933.47),  // 660.3°C
                boiling_point: Some(2792.15), // 2519°C
            },
            Material::Iron => ThermalProperties {
                specific_heat_solid: 450.0,
                specific_heat_liquid: Some(820.0),
                specific_heat_gas: Some(570.0),
                latent_fusion: Some(247_000.0),
                latent_vaporization: Some(6_080_000.0),
                melting_point: Some(1811.15), // 1538°C
                boiling_point: Some(3134.15), // 2861°C
            },
            Material::Air => ThermalProperties {
                specific_heat_solid: 1005.0,
                specific_heat_liquid: Some(1005.0),
                specific_heat_gas: Some(1005.0),
                latent_fusion: Some(23_000.0),
                latent_vaporization: Some(200_000.0),
                melting_point: Some(194.65),
                boiling_point: Some(194.65),
            },
            Material::Glass => ThermalProperties {
                specific_heat_solid: 840.0,
                specific_heat_liquid: None,
                specific_heat_gas: None,
                latent_fusion: None,
                latent_vaporization: None,
                melting_point: None,
                boiling_point: None,
            },
            Material::Wood => ThermalProperties {
                specific_heat_solid: 1700.0,
                specific_heat_liquid: None,
                specific_heat_gas: None,
                latent_fusion: None,
                latent_vaporization: None,
                melting_point: None,
                boiling_point: None,
            },
            Material::Stone => ThermalProperties {
                specific_heat_solid: 840.0,
                specific_heat_liquid: None,
                specific_heat_gas: None,
                latent_fusion: None,
                latent_vaporization: None,
                melting_point: None,
                boiling_point: None,
            },
        }
    }
}