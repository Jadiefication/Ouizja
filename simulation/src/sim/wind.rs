use haje::vec::vec2::Vec2;

#[derive(Clone)]
pub struct Wind {
    pub(crate) force: Vec2<f64>,
    pub(crate) temp: f64
}