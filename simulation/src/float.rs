pub trait SafeDiv {
    fn safe(self) -> Self;
}

impl SafeDiv for f64 {
    fn safe(self) -> Self {
        if self == 0.0 { 1.0 } else { self }
    }
}
