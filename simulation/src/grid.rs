use haje::calculus::laplacian;
use haje::complex::Complex;
use haje::vec::vec2::Vec2;
use rayon::iter::IndexedParallelIterator;
use rayon::iter::ParallelIterator;
use rayon::prelude::{IntoParallelRefIterator, IntoParallelRefMutIterator, ParallelSlice, ParallelSliceMut};

pub struct Grid {
    length: usize,
    height: usize,

    pub field: Vec<f32>,
    source_mask: Vec<f64>,
    alpha_mask: Vec<f64>,
    complex_grid: Vec<Vec<Complex>>,
}

impl Grid {

    pub fn new(base_temp: f32, source_mask: Vec<f64>, alpha_mask: Vec<f64>, length: usize, height: usize) -> Self {
        let complex_grid = vec![vec![Complex::new(base_temp as f64, 0.0); height]; length];

        Self {
            length,
            height,
            field: vec![base_temp; length * height],
            source_mask,
            alpha_mask,
            complex_grid
        }
    }

    pub fn run(&mut self, iterations: usize) {
        let mut next_field = self.field.clone();

        for _ in 0..iterations {
            self.complex_grid
                .par_iter_mut()
                .zip(self.field.par_iter())
                .for_each(|(complex_row, field)| {
                    for complex_cell in complex_row.iter_mut() {
                        complex_cell.re = *field as f64;
                        complex_cell.im = 0.0;
                    }
                });

            next_field
                .par_chunks_exact_mut(self.height)
                .zip(self.field.par_chunks_exact(self.height))
                .zip(self.source_mask.par_chunks_exact(self.height))
                .zip(self.alpha_mask.par_chunks_exact(self.height))
                .enumerate()
                .for_each(|(i, (((next_row, current_row), source_row), alpha_row))| {

                    for (j, cell) in next_row.iter_mut().enumerate() {
                        if source_row[j] != 0.0 {
                            continue;
                        }

                        let pos = Vec2 { x: i, y: j };
                        let alpha = alpha_row[j];

                        let flat_idx = i * self.height + j;
                        let current = self.field[flat_idx] as f64;

                        let result = current + alpha * (laplacian(&pos, &self.complex_grid).re - current);

                        *cell = result as f32;
                    }
                });

            self.field.copy_from_slice(&next_field);
        }
    }

    #[inline]
    fn get_index(&self, x: usize, y: usize) -> usize {
        (x * self.height) + y
    }
}