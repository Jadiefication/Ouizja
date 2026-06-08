#[derive(Clone, Copy, Debug)]
pub struct ThermalProperties {
    pub specific_heat_solid: f64,
    pub specific_heat_liquid: Option<f64>,
    pub specific_heat_gas: Option<f64>,
    pub latent_fusion: Option<f64>,
    pub latent_vaporization: Option<f64>,
    pub melting_point: Option<f64>,
    pub boiling_point: Option<f64>,
    pub diffusivity: f64
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