use haje::calculus::laplacian;
use haje::complex::Complex;
use haje::vec::vec2::Vec2;
use rayon::prelude::{IntoParallelRefIterator, IntoParallelRefMutIterator};

pub struct Grid<const LENGTH: usize, const HEIGHT: usize> {
    field: [[f64; HEIGHT]; LENGTH],
    source_mask: [[bool; HEIGHT]; LENGTH],
    alpha_mask: [[f64; HEIGHT]; LENGTH]
}

impl<const LENGTH: usize, const HEIGHT: usize> Grid<LENGTH, HEIGHT> {

    pub fn new(base_temperature: f64, source_cells: [[bool; HEIGHT]; LENGTH], barrier: [[f64; HEIGHT]; LENGTH]) -> Self<LENGTH, HEIGHT> {
        Self {
            field: [[base_temperature; LENGTH]; HEIGHT],
            source_mask: source_cells,
            alpha_mask: barrier
        }
    }

    pub fn run(&mut self, iterations: usize) {
        let mut next_field = self.field;

        for _ in 0..iterations {
            let complex_grid = self.field.map(|row| {
                row.map(|val| Complex::new(val, 0.0))
            });

            next_field
                .par_iter_mut()
                .zip(self.field.par_iter())
                .enumerate()
                .for_each(|(i, (next_row, current_row))| {
                    for (j, cell) in next_row.iter_mut().enumerate() {
                        let pos = Vec2 { x: i, y: j };
                        if self.source_mask[i][j] {
                            continue
                        }

                        let alpha = self.alpha_mask[i][j];
                        let current = complex_grid[i][j];

                        let result = current + alpha * (laplacian(&pos, &complex_grid) - current);

                        *cell = result.real;
                    }
                });

            self.field = next_field;
        }
    }
}