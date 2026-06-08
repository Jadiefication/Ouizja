use haje::vec::vec2::Vec2;
use rayon::iter::IndexedParallelIterator;
use rayon::iter::ParallelIterator;
use rayon::prelude::{ParallelSlice, ParallelSliceMut};
use crate::sim::cell::cell::Cell;
use crate::sim::mask::Status::Solid;
use crate::sim::wind::Wind;

pub const T_AMBIENT: f64 = 293.15;

pub struct Grid {
    length: usize,
    height: usize,

    pub cells: Vec<Cell>,
    winds: Vec<Wind>
}

impl Grid {
    pub fn new(cells: Vec<Cell>, length: usize, height: usize, winds: Vec<Wind>) -> Self {
        Self {
            length,
            height,
            cells,
            winds
        }
    }

    pub fn run(&mut self, iterations: usize) {
        let max_alpha = self.cells
            .iter()
            .map(|it| it.mask.alpha)
            .collect::<Vec<f64>>()
            .iter()
            .copied()
            .fold(0.0f64, f64::max);
        let delta_t = if max_alpha > 0.0 { 0.25 / max_alpha } else { 1.0 };
        let mut next_field = self.cells.clone();

        let mut global_wind = Vec2 { x: 0.0, y: 0.0 };
        self.winds.iter().for_each(|it| global_wind = global_wind + it.force);
        let external_wind_temp = if !self.winds.is_empty() {
            self.winds.iter().map(|it| it.temp).sum::<f64>() / (self.winds.len() as f64)
        } else {
            0.0
        };

        for n in 0..iterations {
            next_field
                .par_chunks_exact_mut(self.height)
                .zip(self.cells.par_chunks_exact(self.height))
                .enumerate()
                .for_each(|(i, (next_row, source_row))| {
                    for (j, cell) in next_row.iter_mut().enumerate() {
                        if source_row[j].mask.source {
                            continue;
                        }
                        let alpha = source_row[j].mask.alpha;
                        let center_val = source_row[j].get_temperature();

                        let left_i  = if i == 0 { 0 } else { i - 1 };
                        let right_i = if i + 1 >= self.length { i } else { i + 1 };
                        let down_j  = if j == 0 { 0 } else { j - 1 };
                        let up_j    = if j + 1 >= self.height { j } else { j + 1 };

                        let left_val  = self.cells[left_i * self.height + j].get_temperature();
                        let right_val = self.cells[right_i * self.height + j].get_temperature();
                        let down_val  = source_row[down_j].get_temperature();
                        let up_val    = source_row[up_j].get_temperature();

                        let laplacian = left_val + right_val + up_val + down_val - (center_val * 4.0);

                        // --- PHASE 2: CONVECTION & ADVECTION ---
                        let mut advection_x = 0.0;
                        let mut advection_y = 0.0;

                        if source_row[j].mask.status != Solid {
                            let source_x_temp = if global_wind.x > 0.0 {
                                if i == 0 { external_wind_temp } else { left_val }
                            } else {
                                if i + 1 >= self.length { external_wind_temp } else { right_val }
                            };
                            advection_x = global_wind.x.abs() * (source_x_temp - center_val) * delta_t;

                            let buoyancy_wind = if down_val > center_val {
                                (down_val - center_val) * 0.1
                            } else {
                                0.0
                            };

                            let total_wind_y: f64 = global_wind.y + buoyancy_wind;

                            let source_y_temp = if total_wind_y > 0.0 {
                                if j == 0 { external_wind_temp } else { down_val }
                            } else {
                                if j + 1 >= self.height { external_wind_temp } else { up_val }
                            };
                            advection_y = total_wind_y.abs() * (source_y_temp - center_val) * delta_t;
                        }

                        let delta_t_conduction = alpha * delta_t * laplacian;
                        let delta_t_advection  = advection_x + advection_y;

                        let total_delta_t = delta_t_conduction + delta_t_advection;
                        let d_t_cooled = total_delta_t - Cell::newton_cooling(delta_t, (center_val - T_AMBIENT));

                        let cp = cell.get_capacity();
                        let dq = d_t_cooled * cp;

                        cell.enthalpy = source_row[j].enthalpy + dq  - source_row[j].vacuum_radiation(center_val, delta_t);
                        cell.update_state_from_enthalpy();
                    }
                });

            self.cells.copy_from_slice(&next_field);
        }
    }
}