/// Represents the thermal characteristics of a material.
///
/// These properties define how a material stores heat, how it transitions between phases,
/// and how it interacts with its environment via radiation and conduction.
#[derive(Clone, Copy, Debug)]
pub struct ThermalProperties {
    /// Whether the material can change states (true for water/metals, false for wood/stone).
    pub volatile: bool,
    /// Specific heat capacity in the solid state (J/(kg·K)).
    pub specific_heat_solid: f64,
    /// Specific heat capacity in the liquid state (J/(kg·K)). Optional if not volatile.
    pub specific_heat_liquid: Option<f64>,
    /// Specific heat capacity in the gaseous state (J/(kg·K)). Optional if not volatile.
    pub specific_heat_gas: Option<f64>,
    /// Latent heat of fusion (J/kg) required to transition from solid to liquid.
    pub latent_fusion: Option<f64>,
    /// Latent heat of vaporization (J/kg) required to transition from liquid to gas.
    pub latent_vaporization: Option<f64>,
    /// Temperature at which the material melts (K).
    pub melting_point: Option<f64>,
    /// Temperature at which the material boils (K).
    pub boiling_point: Option<f64>,
    /// Thermal diffusivity (m²/s), representing the rate of heat transfer through the material.
    pub diffusivity: f64,
    /// Surface emissivity (0.0 to 1.0), defining radiation efficiency.
    pub emissivity: f64,
}

/// Enthalpy levels at which state transitions occur.
///
/// Enthalpy is used as the internal state for heat because it remains constant during
/// phase transitions while latent heat is being absorbed or released.
#[derive(Clone, Copy)]
pub struct EnthalpyMilestones {
    /// Enthalpy at the start of melting (solid at melting point).
    pub h_melting: f64,
    /// Enthalpy when fully fused (liquid at melting point).
    pub h_fused: f64,
    /// Enthalpy at the start of boiling (liquid at boiling point).
    pub h_boiling: f64,
    /// Enthalpy when fully vaporized (gas at boiling point).
    pub h_vaporized: f64,
}

impl EnthalpyMilestones {
    /// Calculates enthalpy milestones from [`ThermalProperties`].
    ///
    /// The milestones are computed based on specific heat capacities and latent heats.
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
