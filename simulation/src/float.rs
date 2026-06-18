/// Utility trait for safe division, avoiding division by zero.
pub trait SafeDiv {
    /// Returns 1.0 if the value is 0.0, otherwise returns the value itself.
    fn safe(self) -> Self;
}

impl SafeDiv for f64 {
    fn safe(self) -> Self {
        if self == 0.0 { 1.0 } else { self }
    }
}
