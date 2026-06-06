use haje::calculus::laplacian;
use haje::complex::Complex;
use haje::vec::vec2::Vec2;
use rayon::iter::IndexedParallelIterator;
use rayon::iter::ParallelIterator;
use rayon::prelude::{IntoParallelRefIterator, IntoParallelRefMutIterator, ParallelSlice, ParallelSliceMut};

pub struct Grid {
    length: usize,
    height: usize,

    pub temperature: Vec<f64>,
    alpha_mask: Vec<f64>,
    complex_grid: Vec<Vec<Complex>>,
}

impl Grid {

    pub fn new(temperature: Vec<f64>, alpha_mask: Vec<f64>, length: usize, height: usize) -> Self {
        let mut complex_grid = vec![vec![Complex::zero(); height]; length];
        temperature.iter().enumerate().for_each(|(i, &temp)| {
            let x = i / height;
            let y = i % height;
            complex_grid[x][y] = Complex::new(temp, 0.0)
        });

        Self {
            length,
            height,
            temperature,
            alpha_mask,
            complex_grid
        }
    }

    pub fn run(&mut self, iterations: usize) {
        let mut next_field = self.temperature.clone();

        for _ in 0..iterations {
            self.complex_grid
                .par_iter_mut()
                .zip(self.temperature.par_iter())
                .for_each(|(complex_row, field)| {
                    for complex_cell in complex_row.iter_mut() {
                        complex_cell.re = *field as f64;
                        complex_cell.im = 0.0;
                    }
                });

            next_field
                .par_chunks_exact_mut(self.height)
                .zip(self.temperature.par_chunks_exact(self.height))
                .zip(self.alpha_mask.par_chunks_exact(self.height))
                .enumerate()
                .for_each(|(i, ((next_row, source_row), alpha_row))| {

                    for (j, cell) in next_row.iter_mut().enumerate() {
                        if source_row[j] != 0.0 {
                            continue;
                        }

                        let pos = Vec2 { x: i, y: j };
                        let alpha = alpha_row[j];

                        let flat_idx = i * self.height + j;
                        let current = self.temperature[flat_idx];

                        let result = current + alpha * (laplacian(&pos, &self.complex_grid).re - current);

                        *cell = result;
                    }
                });

            self.temperature.copy_from_slice(&next_field);
        }
    }

    #[inline]
    fn get_index(&self, x: usize, y: usize) -> usize {
        (x * self.height) + y
    }
}