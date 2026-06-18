use crate::float::SafeDiv;
use crate::sim::cell::therms::{EnthalpyMilestones, ThermalProperties};
use crate::sim::mask::Mask;
use crate::sim::mask::Status::{Fusing, Gas, Liquid, Solid, Vaporizing};
use crate::sim::material::Material::{Air, Water};

pub const HEAT_TRANSFER_C: f64 = 5.0;
pub const STEFANS_C: f64 = 5.670e-8;

/// Represents a single cell in the simulation grid.
#[derive(Clone, Copy)]
pub struct Cell {
    /// Physical and material properties of the cell.
    pub mask: Mask,
    /// Current enthalpy (heat energy) of the cell.
    pub enthalpy: f64,
}

impl Cell {
    /// Updates the physical state of the cell based on its current enthalpy.
    /// Returns the new temperature.
    pub fn update_state_from_enthalpy(&mut self) -> f64 {
        let props = self.mask.material.thermal_properties();
        let milestones = if !props.volatile {
            EnthalpyMilestones {
                h_melting: f64::MAX,
                h_fused: f64::MAX,
                h_boiling: f64::MAX,
                h_vaporized: f64::MAX,
            }
        } else {
            EnthalpyMilestones::from_properties(&props)
        };

        let is_sublimating_material = self.mask.material == Air || self.mask.material == Water;
        let skip_liquid = is_sublimating_material && (props.melting_point == props.boiling_point);

        if self.enthalpy < milestones.h_melting {
            self.mask.status = Solid;
            return self.enthalpy / props.specific_heat_solid.safe();
        }

        if skip_liquid {
            let total_sublimation_latent =
                props.latent_fusion.unwrap_or(0.0) + props.latent_vaporization.unwrap_or(0.0);
            let h_sublimation_end = milestones.h_melting + total_sublimation_latent;

            return if self.enthalpy < h_sublimation_end {
                let current_latent = self.enthalpy - milestones.h_melting;
                self.mask.status = Vaporizing {
                    l_energy: current_latent,
                };
                props.melting_point.unwrap_or(0.0)
            } else {
                self.mask.status = Gas;
                let excess_energy = self.enthalpy - h_sublimation_end;
                props.boiling_point.unwrap_or(0.0)
                    + (excess_energy
                        / props
                            .specific_heat_gas
                            .unwrap_or(props.specific_heat_solid)
                            .safe())
            };
        }

        if self.enthalpy < milestones.h_fused {
            let current_latent = self.enthalpy - milestones.h_melting;
            self.mask.status = Fusing {
                l_energy: current_latent,
            };
            return props.melting_point.unwrap_or(0.0);
        }

        if self.enthalpy < milestones.h_boiling {
            self.mask.status = Liquid;
            let excess_energy = self.enthalpy - milestones.h_fused;
            return props.melting_point.unwrap_or(0.0)
                + (excess_energy / props.specific_heat_liquid.unwrap_or(1.0).safe());
        }

        if self.enthalpy < milestones.h_vaporized {
            let current_latent = self.enthalpy - milestones.h_boiling;
            self.mask.status = Vaporizing {
                l_energy: current_latent,
            };
            return props.boiling_point.unwrap_or(0.0);
        }

        self.mask.status = Gas;
        let excess_energy = self.enthalpy - milestones.h_vaporized;
        props.boiling_point.unwrap_or(0.0)
            + (excess_energy / props.specific_heat_gas.unwrap_or(1.0).safe())
    }

    /// Calculates the temperature of the cell based on its enthalpy and material properties.
    pub fn get_temperature(&self) -> f64 {
        let props = self.mask.material.thermal_properties();
        if !props.volatile {
            return self.enthalpy / props.specific_heat_solid;
        };
        let milestones = EnthalpyMilestones::from_properties(&props);
        let is_sublimating_material = self.mask.material == Air || self.mask.material == Water;
        let skip_liquid = is_sublimating_material && (props.melting_point == props.boiling_point);

        if skip_liquid {
            let total_sublimation_latent =
                props.latent_fusion.unwrap_or(0.0) + props.latent_vaporization.unwrap_or(0.0);
            let h_sublimation_end = milestones.h_melting + total_sublimation_latent;

            if self.enthalpy < milestones.h_melting {
                return self.enthalpy / props.specific_heat_solid.safe();
            }
            if milestones.h_melting <= self.enthalpy && self.enthalpy < h_sublimation_end {
                return props.melting_point.unwrap_or(0.0);
            }
            if self.enthalpy >= h_sublimation_end {
                return props.boiling_point.unwrap_or(0.0)
                    + ((self.enthalpy - h_sublimation_end)
                        / props.specific_heat_gas.unwrap_or(0.0).safe());
            }
        }

        match self.mask.status {
            Solid => self.enthalpy / props.specific_heat_solid.safe(),
            Fusing { .. } => props.melting_point.unwrap_or(0.0),
            Liquid => {
                let c_liquid = props.specific_heat_liquid.unwrap_or(0.0);
                props.melting_point.unwrap_or(0.0)
                    + ((self.enthalpy - milestones.h_fused) / c_liquid.safe())
            }
            Vaporizing { .. } => props.boiling_point.unwrap_or(0.0),
            Gas => {
                let c_gas = props.specific_heat_gas.unwrap_or(0.0);
                props.boiling_point.unwrap_or(0.0)
                    + ((self.enthalpy - milestones.h_vaporized) / c_gas.safe())
            }
        }
    }

    /// Returns the specific heat capacity of the cell in its current state.
    pub fn get_capacity(&self) -> f64 {
        let props = self.mask.material.thermal_properties();
        match self.mask.status {
            Solid => props.specific_heat_solid,
            Liquid => props
                .specific_heat_liquid
                .unwrap_or(props.specific_heat_solid),
            Gas => props.specific_heat_gas.unwrap_or(props.specific_heat_solid),

            Fusing { .. } => props.specific_heat_solid,
            Vaporizing { .. } => props
                .specific_heat_liquid
                .unwrap_or(props.specific_heat_solid),
        }
    }

    /// Calculates the enthalpy for a given temperature and material properties.
    pub fn calculate_forward_enthalpy(t: f64, props: &ThermalProperties) -> f64 {
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

    /// Calculates the heat loss due to convective cooling (Newton's law of cooling).
    pub fn newton_cooling(d_t: f64, d_temp: f64) -> f64 {
        HEAT_TRANSFER_C * d_t * d_temp
    }

    /// Calculates the heat loss due to radiation in a vacuum (Stefan-Boltzmann law).
    pub fn vacuum_radiation(&self, temp: f64, d_t: f64) -> f64 {
        self.mask.material.thermal_properties().emissivity * STEFANS_C * temp.powi(4) * d_t
    }
}
