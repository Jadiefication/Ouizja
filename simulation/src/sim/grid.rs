use crate::float::ThermUtils;
use crate::sim::cells::quantum::Quantum;
use crate::sim::cells::therms::EnthalpyMilestones;
use crate::sim::mask::Status;
use crate::sim::mask::Status::Solid;
use crate::sim::material::Material;
use crate::sim::material::Material::{Air, Barrier, Water};
use crate::sim::wind::Wind;
use haje::vec::vec2::Vec2;
use rayon::iter::IndexedParallelIterator;
use rayon::iter::ParallelIterator;
use rayon::prelude::{IntoParallelRefIterator, ParallelSlice, ParallelSliceMut};
use std::cmp::Ordering::Equal;

const T_SAFE: f64 = 0.001;

/// The main simulation grid containing all cells and global properties.
pub struct Grid {
    /// Width of the grid.
    length: usize,
    /// Height of the grid.
    height: usize,

    /// Flat collection of all cells in the grid.
    //pub cells: Vec<Cell>,
    pub enthalpies: Vec<f64>,
    pub alpha_mask: Vec<f64>,
    pub quantum_indices: Vec<isize>,
    pub quantum: Vec<Quantum>,
    /// pub material_mask: Vec<Material>,
    /// pub source_mask: Vec<bool>,
    /// pub status_mask: Vec<Status>,
    /// Collapses into this
    /// 1st Bit - Source
    /// 2nd - 4th Bits - Status
    /// 5th - 9th Bits - Material
    pub metadata: Vec<u16>,
    /// Collection of wind vectors affecting the simulation.
    winds: Vec<Wind>,
    /// Ambient temperature of the environment.
    t_ambient: f64,
    t_ambient_fourth: f64
}

impl Grid {

    /// Creates a new simulation grid.
    pub fn new(
        enthalpies: Vec<f64>,
        alpha_mask: Vec<f64>,
        quantum_indices: Vec<isize>,
        quantum: Vec<Quantum>,
        metadata: Vec<u16>,
        winds: Vec<Wind>,
        grid_info: (usize, usize, f64),
    ) -> Self {
        let length = grid_info.0;
        let height = grid_info.1;
        let t_ambient = grid_info.2;
        let t_ambient_fourth = t_ambient.powi(4);

        Self {
            length,
            height,
            enthalpies,
            alpha_mask,
            quantum_indices,
            quantum,
            metadata,
            winds,
            t_ambient,
            t_ambient_fourth
        }
    }

    /// Executes the simulation for a given number of iterations.
    /// Uses parallel processing for cell updates.
    pub fn run(&mut self, iterations: usize) {
        let max_alpha = self.alpha_mask.iter().copied().fold(0.0f64, f64::max);

        let delta_t = T_SAFE.min(1.0/4.0*max_alpha);
        let mut next_field = self.enthalpies.clone();
        let mut next_metadata = self.metadata.clone();

        let mut global_wind = Vec2 { x: 0.0, y: 0.0 };
        self.winds
            .iter()
            .for_each(|it| global_wind = global_wind + it.force);
        let external_wind_temp = if !self.winds.is_empty() {
            self.winds.iter().map(|it| it.temp).sum::<f64>() / (self.winds.len() as f64)
        } else {
            0.0
        };

        for _ in 0..iterations {
            next_field
                .par_chunks_exact_mut(self.height)
                .zip(next_metadata.par_chunks_exact_mut(self.height))
                .zip(self.enthalpies.par_chunks_exact(self.height))
                .zip(self.alpha_mask.par_chunks_exact(self.height))
                .zip(self.metadata.par_chunks_exact(self.height))
                .enumerate()
                .for_each(
                    |(i, ((((next_row, next_m_row), _source_row), alpha_row), metadata_row))| {
                        for (j, enthalpy) in next_row.iter_mut().enumerate() {
                            let material =
                                Material::find_by_id(((metadata_row[j] >> 4) & 0x1F) as u8);
                            let status = Status::find_by_id(((metadata_row[j] >> 1) & 0x07) as u8);
                            if metadata_row[j] & 0x1 == 1 || material == Barrier {
                                continue;
                            }
                            let alpha = alpha_row[j];
                            let center_val =
                                self.enthalpies[i * self.height + j].get_temp(material, &status);

                            let left_i = if i == 0 { 0 } else { i - 1 };
                            let right_i = if i + 1 >= self.length { i } else { i + 1 };
                            let down_j = if j == 0 { 0 } else { j - 1 };
                            let up_j = if j + 1 >= self.height { j } else { j + 1 };

                            let left_m = self.metadata[left_i * self.height + j];
                            let right_m = self.metadata[right_i * self.height + j];
                            let down_m = self.metadata[i * self.height + down_j];
                            let up_m = self.metadata[i * self.height + up_j];

                            let r_material = Material::find_by_id(((right_m >> 4) & 0x1F) as u8);
                            let r_status = Status::find_by_id(((right_m >> 1) & 0x07) as u8);

                            let u_material = Material::find_by_id(((up_m >> 4) & 0x1F) as u8);
                            let u_status = Status::find_by_id(((up_m >> 1) & 0x07) as u8);

                            let d_material = Material::find_by_id(((down_m >> 4) & 0x1F) as u8);
                            let d_status = Status::find_by_id(((down_m >> 1) & 0x07) as u8);

                            let l_material = Material::find_by_id(((left_m >> 4) & 0x1F) as u8);
                            let l_status = Status::find_by_id(((left_m >> 1) & 0x07) as u8);

                            let left_val = if l_material == Barrier {
                                center_val
                            } else {
                                self.enthalpies[left_i * self.height + j]
                                    .get_temp(l_material, &l_status)
                            };
                            let right_val = if r_material == Barrier {
                                center_val
                            } else {
                                self.enthalpies[right_i * self.height + j]
                                    .get_temp(r_material, &r_status)
                            };
                            let down_val = if d_material == Barrier {
                                center_val
                            } else {
                                self.enthalpies[i * self.height + down_j]
                                    .get_temp(d_material, &d_status)
                            };
                            let up_val = if u_material == Barrier {
                                center_val
                            } else {
                                self.enthalpies[i * self.height + up_j]
                                    .get_temp(u_material, &u_status)
                            };

                            let laplacian =
                                left_val + right_val + up_val + down_val - (center_val * 4.0);

                            // --- PHASE 2: CONVECTION & ADVECTION ---
                            let mut advection_x = 0.0;
                            let mut advection_y = 0.0;

                            if status != Solid {
                                let source_x_temp = if global_wind.x > 0.0 {
                                    if i == 0 { external_wind_temp } else { left_val }
                                } else {
                                    if i + 1 >= self.length {
                                        external_wind_temp
                                    } else {
                                        right_val
                                    }
                                };
                                advection_x = global_wind.x.abs() * (source_x_temp - center_val);

                                let buoyancy_wind = if down_val > center_val {
                                    (down_val - center_val) * 1.0
                                } else {
                                    0.0
                                };

                                let total_wind_y: f64 = global_wind.y + buoyancy_wind;

                                let source_y_temp = if total_wind_y > 0.0 {
                                    if j == 0 { external_wind_temp } else { down_val }
                                } else {
                                    if j + 1 >= self.height {
                                        external_wind_temp
                                    } else {
                                        up_val
                                    }
                                };
                                advection_y = total_wind_y.abs() * (source_y_temp - center_val);
                            }

                            let cp = enthalpy.get_capacity(material, &status);
                            let delta_t_conduction = alpha * delta_t * laplacian;
                            let delta_t_advection = (advection_x + advection_y) * delta_t;

                            let dq = (delta_t_conduction * cp) + (delta_t_advection * cp);

                            let safe_temp = center_val.max(0.0);

                            let dq_rad = f64::vacuum_radiation(material, safe_temp, delta_t, self.t_ambient_fourth);
                            let dq_newton =
                                f64::newton_cooling(delta_t, safe_temp - self.t_ambient);

                            let mut new_enthalpy =
                                self.enthalpies[i * self.height + j] + dq - dq_rad - dq_newton;
                            if new_enthalpy < 0.0 || new_enthalpy.is_nan() {
                                new_enthalpy = 0.0;
                            }

                            *enthalpy = new_enthalpy;

                            let props = material.thermal_properties();
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

                            let is_sublimating_material = material == Air || material == Water;
                            let skip_liquid = is_sublimating_material
                                && (props.melting_point == props.boiling_point);
                            let inverse_mask = !0x000E;

                            if *enthalpy < milestones.h_melting {
                                next_m_row[j] = (metadata_row[j] & inverse_mask) | (0 << 1); // Solid
                            } else if skip_liquid {
                                let total_sublimation_latent = props.latent_fusion.unwrap_or(0.0)
                                    + props.latent_vaporization.unwrap_or(0.0);
                                let h_sublimation_end =
                                    milestones.h_melting + total_sublimation_latent;

                                if *enthalpy < h_sublimation_end {
                                    next_m_row[j] = (metadata_row[j] & inverse_mask) | (4 << 1);
                                } else {
                                    next_m_row[j] = (metadata_row[j] & inverse_mask) | (2 << 1);
                                };
                            } else {
                                if *enthalpy < milestones.h_fused {
                                    next_m_row[j] = (metadata_row[j] & inverse_mask) | (3 << 1); // Fusing
                                } else if *enthalpy < milestones.h_boiling {
                                    next_m_row[j] = (metadata_row[j] & inverse_mask) | (1 << 1); // Liquid
                                } else if *enthalpy < milestones.h_vaporized {
                                    next_m_row[j] = (metadata_row[j] & inverse_mask) | (4 << 1); // Vaporizing
                                } else {
                                    next_m_row[j] = (metadata_row[j] & inverse_mask) | (2 << 1); // Gas
                                }
                            }
                        }
                    },
                );

            self.metadata.copy_from_slice(&next_metadata);
            self.enthalpies.copy_from_slice(&next_field);

            self.quantum_indices
                .iter_mut()
                .zip(&self.enthalpies)
                .zip(&self.metadata)
                .for_each(|((i, enthalpy), metadata)| {
                    if *i != -1 {
                        let status = Status::find_by_id(((metadata >> 1) & 0x07) as u8);
                        let material = Material::find_by_id(((metadata >> 4) & 0x1F) as u8);
                        let quantum = &mut self.quantum[*i as usize];
                        let center_val = enthalpy.get_temp(material, &status);
                        let new_gamma = quantum.get_next(center_val, delta_t);
                        if new_gamma <= 0.0 {
                            *i = -1
                        } else {
                            quantum.gamma = new_gamma;
                        }
                    }
                });
        }
    }
}
