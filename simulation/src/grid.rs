use haje::calculus::laplacian;
use haje::complex::Complex;
use haje::vec::vec2::Vec2;
use rayon::prelude::{IntoParallelRefIterator, IntoParallelRefMutIterator};
use crate::material::Material;

pub struct Grid<const LENGTH: usize, const HEIGHT: usize> {
    field: [[f64; HEIGHT]; LENGTH],
    material: Material
}

impl<const LENGTH: usize, const HEIGHT: usize> Grid<LENGTH, HEIGHT> {

    pub fn new(material: Material, base_temperature: f64) -> Self<LENGTH, HEIGHT> {
        Self {
            field: [[base_temperature; LENGTH]; HEIGHT],
            material
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

                        let result = laplacian(&pos, &complex_grid);

                        *cell = result.real;
                    }
                });

            self.field = next_field;
        }
    }
}