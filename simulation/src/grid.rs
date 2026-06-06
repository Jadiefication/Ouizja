use rayon::iter::IndexedParallelIterator;
use rayon::iter::ParallelIterator;
use rayon::prelude::{ParallelSlice, ParallelSliceMut};
use crate::mask::Mask;

pub struct Grid {
    length: usize,
    height: usize,

    pub temperature: Vec<f64>,
    pub masks: Vec<Mask>
}

impl Grid {
    pub fn new(temperature: Vec<f64>, masks: Vec<Mask>, length: usize, height: usize) -> Self {
        Self {
            length,
            height,
            temperature,
            masks
        }
    }

    pub fn run(&mut self, iterations: usize) {
        let max_alpha = self.masks
            .iter()
            .map(|it| it.alpha)
            .collect::<Vec<f64>>()
            .iter()
            .copied()
            .fold(0.0f64, f64::max);
        let delta_t = if max_alpha > 0.0 { 0.25 / max_alpha } else { 1.0 };
        let mut next_field = self.temperature.clone();

        for _ in 0..iterations {
            next_field
                .par_chunks_exact_mut(self.height)
                .zip(self.temperature.par_chunks_exact(self.height))
                .zip(self.masks.par_chunks_exact(self.height))
                .enumerate()
                .for_each(|(i, ((next_row, source_row), alpha_row))| {
                    for (j, cell) in next_row.iter_mut().enumerate() {
                        let flat_idx = i * self.height + j;
                        if self.masks[flat_idx].source {
                            continue;
                        }
                        let alpha = alpha_row[j].alpha;
                        let center_val = source_row[j];

                        let left_i  = if i == 0 { 0 } else { i - 1 };
                        let right_i = if i + 1 >= self.length { i } else { i + 1 };

                        let down_j = if j == 0 { 0 } else { j - 1 };
                        let up_j   = if j + 1 >= self.height { j } else { j + 1 };

                        let left_val  = self.temperature[left_i * self.height + j];
                        let right_val = self.temperature[right_i * self.height + j];

                        let down_val  = source_row[down_j];
                        let up_val    = source_row[up_j];

                        let buoyancy_force = if self.masks[flat_idx].not_solid {
                            if down_val > center_val {
                                (down_val - center_val) * 0.1
                            } else {
                                0.0
                            }
                        } else {
                            0.0
                        };

                        let laplacian = left_val + right_val + up_val + down_val - (center_val * 4.0) + buoyancy_force;
                        *cell = center_val + (alpha * delta_t) * laplacian;
                    }
                });

            self.temperature.copy_from_slice(&next_field);
        }
    }
}