use crate::sim::cells::therms::{EnthalpyMilestones, ThermalProperties};
use crate::sim::mask::Status;
use crate::sim::mask::Status::{Fusing, Gas, Liquid, Solid, Vaporizing};
use crate::sim::material::Material;
use crate::sim::material::Material::{Air, Water};

pub const HEAT_TRANSFER_C: f64 = 5.0;
pub const STEFANS_C: f64 = 5.670e-8;

/// Utility trait for safe division, avoiding division by zero.
pub trait ThermUtils {
    /// Returns 1.0 if the value is 0.0, otherwise returns the value itself.
    fn safe(self) -> Self;

    fn get_temp(self, material: Material, status: &Status) -> Self;
    /// Returns the specific heat capacity of the cell in its current state.
    fn get_capacity(&self, material: Material, status: &Status) -> f64;

    /// Calculates the heat loss due to convective cooling (Newton's law of cooling).
    fn newton_cooling(d_t: f64, d_temp: f64) -> f64 {
        HEAT_TRANSFER_C * d_t * d_temp
    }

    /// Calculates the heat loss due to radiation in a vacuum (Stefan-Boltzmann law).
    fn vacuum_radiation(material: Material, temp: f64, d_t: f64, t_amb_fourth: f64) -> f64 {
        material.thermal_properties().emissivity * STEFANS_C * (temp.powi(4) - t_amb_fourth) * d_t
    }

    /// Calculates the enthalpy for a given temperature and material properties.
    fn calculate_forward_enthalpy(t: f64, props: &ThermalProperties) -> f64 {
        let t_melt = props.melting_point.unwrap_or(f64::MAX);
        let t_boil = props.boiling_point.unwrap_or(f64::MAX);

        if t < t_melt {
            return props.specific_heat_solid * t;
        }

        let h_at_melting = props.specific_heat_solid * t_melt;
        let h_fused = h_at_melting + props.latent_fusion.unwrap_or(0.0);

        if t < t_boil {
            let c_liquid = props
                .specific_heat_liquid
                .unwrap_or(props.specific_heat_solid);
            return h_fused + (c_liquid * (t - t_melt));
        }

        let delta_t_liquid = (t_boil - t_melt).max(0.0);
        let c_liquid = props
            .specific_heat_liquid
            .unwrap_or(props.specific_heat_solid);
        let h_at_boiling = h_fused + (c_liquid * delta_t_liquid);
        let h_vaporized = h_at_boiling + props.latent_vaporization.unwrap_or(0.0);

        let c_gas = props.specific_heat_gas.unwrap_or(props.specific_heat_solid);
        h_vaporized + (c_gas * (t - t_boil))
    }
}

impl ThermUtils for f64 {
    fn safe(self) -> Self {
        if self == 0.0 { 1.0 } else { self }
    }

    fn get_temp(self, material: Material, status: &Status) -> Self {
        let props = material.thermal_properties();
        if !props.volatile {
            return self / props.specific_heat_solid;
        };
        let milestones = EnthalpyMilestones::from_properties(&props);
        let is_sublimating_material = material == Air || material == Water;
        let skip_liquid = is_sublimating_material && (props.melting_point == props.boiling_point);

        if skip_liquid {
            let total_sublimation_latent =
                props.latent_fusion.unwrap_or(0.0) + props.latent_vaporization.unwrap_or(0.0);
            let h_sublimation_end = milestones.h_melting + total_sublimation_latent;

            if self < milestones.h_melting {
                return self / props.specific_heat_solid.safe();
            }
            if milestones.h_melting <= self && self < h_sublimation_end {
                return props.melting_point.unwrap_or(0.0);
            }
            if self >= h_sublimation_end {
                return props.boiling_point.unwrap_or(0.0)
                    + ((self - h_sublimation_end) / props.specific_heat_gas.unwrap_or(0.0).safe());
            }
        }

        match status {
            Solid => self / props.specific_heat_solid.safe(),
            Fusing => props.melting_point.unwrap_or(0.0),
            Liquid => {
                let c_liquid = props.specific_heat_liquid.unwrap_or(0.0);
                props.melting_point.unwrap_or(0.0) + ((self - milestones.h_fused) / c_liquid.safe())
            }
            Vaporizing => props.boiling_point.unwrap_or(0.0),
            Gas => {
                let c_gas = props.specific_heat_gas.unwrap_or(0.0);
                props.boiling_point.unwrap_or(0.0)
                    + ((self - milestones.h_vaporized) / c_gas.safe())
            }
        }
    }

    /// Returns the specific heat capacity of the cell in its current state.
    fn get_capacity(&self, material: Material, status: &Status) -> f64 {
        let props = material.thermal_properties();
        match status {
            Solid => props.specific_heat_solid,
            Liquid => props
                .specific_heat_liquid
                .unwrap_or(props.specific_heat_solid),
            Gas => props.specific_heat_gas.unwrap_or(props.specific_heat_solid),

            Fusing => props.specific_heat_solid,
            Vaporizing => props
                .specific_heat_liquid
                .unwrap_or(props.specific_heat_solid),
        }
    }
}
