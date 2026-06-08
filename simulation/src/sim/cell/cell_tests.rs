#[cfg(test)]
mod tests {
    use crate::sim::mask::{Mask, Status};
    use crate::sim::material::Material;
    use proptest::prelude::*;
    use crate::sim::cell::cell::Cell;

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
    }
}
