#[cfg(test)]
mod tests {
    use crate::sim::cell::cell::Cell;
    use crate::sim::mask::{Mask, Status};
    use crate::sim::material::Material;
    use proptest::prelude::*;

    proptest! {
        #[test]
        fn test_enthalpy_roundtrip(temp in -100.0..5000.0f64) {
            let props = Material::Iron.thermal_properties();
            let enthalpy = Cell::calculate_forward_enthalpy(temp, &props);

            let mut cell = Cell {
                mask: Mask {
                    status: Status::Solid,
                    source: false,
                    alpha: props.diffusivity,
                    material: Material::Iron,
                    quantum: None
                },
                enthalpy,
            };

            let calculated_temp = cell.update_state_from_enthalpy();

            // Allow some tolerance for floating point and latent heat plateau
            if temp < props.melting_point.unwrap() || (temp > props.melting_point.unwrap() && temp < props.boiling_point.unwrap()) || temp > props.boiling_point.unwrap() {
                 prop_assert!( (temp - calculated_temp).abs() < 1e-6, "Temp mismatch: expected {}, got {}", temp, calculated_temp);
            } else {
                 // On plateau, it should be at the transition point
                 if temp == props.melting_point.unwrap() {
                     prop_assert_eq!(calculated_temp, props.melting_point.unwrap());
                 }
            }
        }

        #[test]
        fn test_phase_transition_enthalpy(material_id in 0..7u8) {
            let material = Material::find_by_id(material_id);
            let props = material.thermal_properties();
            if !props.volatile { return Ok(()); }

            let t_melt = props.melting_point.unwrap();
            let t_boil = props.boiling_point.unwrap();

            // Test enthalpy at melting point
            let h_solid_at_melt = props.specific_heat_solid * t_melt;
            let h_liquid_at_melt = h_solid_at_melt + props.latent_fusion.unwrap();

            let mut cell = Cell {
                mask: Mask {
                    status: Status::Solid,
                    source: false,
                    alpha: props.diffusivity,
                    material,
                    quantum: None
                },
                enthalpy: h_solid_at_melt - 1.0,
            };
            cell.update_state_from_enthalpy();
            prop_assert_eq!(cell.mask.status, Status::Solid);

            cell.enthalpy = h_solid_at_melt + 0.5 * props.latent_fusion.unwrap();
            cell.update_state_from_enthalpy();
            match cell.mask.status {
                Status::Fusing { .. } => {},
                _ => prop_assert!(false, "Expected Fusing state for material {:?}, got {:?}", material, cell.mask.status),
            }

            if t_melt < t_boil {
                cell.enthalpy = h_liquid_at_melt + 1.0;
                cell.update_state_from_enthalpy();
                prop_assert_eq!(cell.mask.status, Status::Liquid);
            }
        }
    }

    #[test]
    fn test_newton_cooling_logic() {
        let dt = 0.1;
        let d_temp = 10.0;
        let dq = Cell::newton_cooling(dt, d_temp);
        // HEAT_TRANSFER_C = 5.0. 5.0 * 0.1 * 10.0 = 5.0
        assert_eq!(dq, 5.0);
    }
}
