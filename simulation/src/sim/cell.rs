use crate::sim::mask::Status::{Fusing, Gas, Liquid, Solid, Vaporizing};
use crate::sim::mask::Mask;
use crate::sim::material::Material::{Air, Water};

#[derive(Clone, Copy, Debug)]
pub struct ThermalProperties {
    pub specific_heat_solid: f64,
    pub specific_heat_liquid: Option<f64>,
    pub specific_heat_gas: Option<f64>,
    pub latent_fusion: Option<f64>,
    pub latent_vaporization: Option<f64>,
    pub melting_point: Option<f64>,
    pub boiling_point: Option<f64>,
}

#[derive(Clone, Copy)]
pub struct EnthalpyMilestones {
    pub h_melting: f64,
    pub h_fused: f64,
    pub h_boiling: f64,
    pub h_vaporized: f64,
}

impl EnthalpyMilestones {
    pub fn from_properties(props: &ThermalProperties) -> Self {
        let t_melt = props.melting_point.unwrap_or(0.0);
        let t_boil = props.boiling_point.unwrap_or(0.0);

        let h_melting = props.specific_heat_solid * t_melt;
        let h_fused = h_melting + props.latent_fusion.unwrap_or(0.0);

        let delta_t_liquid = (t_boil - t_melt).max(0.0);
        let h_boiling = h_fused + (props.specific_heat_liquid.unwrap_or(0.0) * delta_t_liquid);
        let h_vaporized = h_boiling + props.latent_vaporization.unwrap_or(0.0);

        EnthalpyMilestones {
            h_melting,
            h_fused,
            h_boiling,
            h_vaporized,
        }
    }
}

#[derive(Clone, Copy)]
pub struct Cell {
    pub mask: Mask,
    pub enthalpy: f64,
}

impl Cell {
    pub fn update_state_from_enthalpy(&mut self) -> f64 {
        let props = self.mask.material.thermal_properties();
        let milestones = EnthalpyMilestones::from_properties(&props);

        let is_sublimating_material = self.mask.material == Air || self.mask.material == Water;
        let skip_liquid = is_sublimating_material && (props.melting_point == props.boiling_point);

        if self.enthalpy < milestones.h_melting {
            self.mask.status = Solid;
            return self.enthalpy / props.specific_heat_solid;
        }

        if skip_liquid {
            let total_sublimation_latent = props.latent_fusion.unwrap_or(0.0) + props.latent_vaporization.unwrap_or(0.0);
            let h_sublimation_end = milestones.h_melting + total_sublimation_latent;

            return if self.enthalpy < h_sublimation_end {
                let current_latent = self.enthalpy - milestones.h_melting;
                self.mask.status = Vaporizing { l_energy: current_latent };
                props.melting_point.unwrap_or(0.0)
            } else {
                self.mask.status = Gas;
                let excess_energy = self.enthalpy - h_sublimation_end;
                props.boiling_point.unwrap_or(0.0) + (excess_energy / props.specific_heat_gas.unwrap_or(props.specific_heat_solid))
            }
        }

        if self.enthalpy < milestones.h_fused {
            let current_latent = self.enthalpy - milestones.h_melting;
            self.mask.status = Fusing { l_energy: current_latent };
            return props.melting_point.unwrap_or(0.0);
        }

        if self.enthalpy < milestones.h_boiling {
            self.mask.status = Liquid;
            let excess_energy = self.enthalpy - milestones.h_fused;
            return props.melting_point.unwrap_or(0.0) + (excess_energy / props.specific_heat_liquid.unwrap_or(1.0));
        }

        if self.enthalpy < milestones.h_vaporized {
            let current_latent = self.enthalpy - milestones.h_boiling;
            self.mask.status = Vaporizing { l_energy: current_latent };
            return props.boiling_point.unwrap_or(0.0);
        }

        self.mask.status = Gas;
        let excess_energy = self.enthalpy - milestones.h_vaporized;
        props.boiling_point.unwrap_or(0.0) + (excess_energy / props.specific_heat_gas.unwrap_or(1.0))
    }
}