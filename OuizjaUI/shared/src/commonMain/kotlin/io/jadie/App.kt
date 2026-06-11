package io.jadie

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Run Simulation") }
    MaterialTheme {
        Button(
            onClick = {
                text = "Running..."
                runSimulation()
                text = "Done!"
            }
        ) {
            Text(text)
        }
    }
}

expect fun runSimulation()