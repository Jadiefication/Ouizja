use haje::calculus::laplacian;
use haje::complex::Complex;
use haje::vec::vec2::Vec2;
use crate::material::Material;

pub struct Grid<const LENGTH: usize, const HEIGHT: usize> {
    field: [[f64 ;HEIGHT]; LENGTH],
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
        let rows = self.field.len();
        let cols = self.field[0].len();

        for _ in 0..iterations {
            let mut complex_grid = Vec::with_capacity(rows);

            for row in self.field.iter() {
                let mut complex_row = Vec::with_capacity(cols);
                for &val in row.iter() {
                    complex_row.push(Complex::new(val, 0.0));
                }
                complex_grid.push(complex_row);
            }

            for i in 0..rows {
                for j in 0..cols {
                    let result = laplacian(&Vec2 { x: i, y: j }, &complex_grid);
                    self.field[i][j] = result.real;
                }
            }
        }
    }
}