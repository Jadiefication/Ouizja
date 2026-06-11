package io.jadie

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.jadie.sim.Material

@Composable
@Preview
fun App() {
    var isRunning by remember { mutableStateOf(false) }
    var gridData by remember { mutableStateOf(Array(50) { DoubleArray(50) { 20.0 } }) }
    var materialData by remember { mutableStateOf(Array(50) { Array(50) { Material.IRON } }) }
    var iteration by remember { mutableStateOf(0) }
    var showMaterial by remember { mutableStateOf(false) }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .width(250.dp)
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Ouizja",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Iteration: $iteration")
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { isRunning = !isRunning }
                    ) {
                        Text(if (isRunning) "Stop" else "Start")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { iteration = 0 }
                    ) {
                        Text("Reset")
                    }

                    Spacer(modifier = Modifier.dividerSpacerHeight())
                    HorizontalDivider()
                    Spacer(modifier = Modifier.dividerSpacerHeight())

                    Text(text = "View Mode", style = MaterialTheme.typography.titleSmall)
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(
                            selected = !showMaterial,
                            onClick = { showMaterial = false }
                        )
                        Text("Temperature")
                    }
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(
                            selected = showMaterial,
                            onClick = { showMaterial = true }
                        )
                        Text("Material")
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    if (showMaterial) {
                        MaterialGrid(materialData)
                    } else {
                        SimulationGrid(gridData)
                    }
                }
            }
        }
    }
}

private fun Modifier.dividerSpacerHeight() = this.height(16.dp)

@Composable
fun MaterialGrid(data: Array<Array<Material>>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val rows = data.size
        val cols = if (rows > 0) data[0].size else 0
        if (rows == 0 || cols == 0) return@Canvas

        val cellWidth = size.width / cols
        val cellHeight = size.height / rows

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val material = data[r][c]
                val color = materialToColor(material)
                drawRect(
                    color = color,
                    topLeft = Offset(c * cellWidth, r * cellHeight),
                    size = Size(cellWidth, cellHeight)
                )
            }
        }
    }
}

fun materialToColor(material: Material): Color {
    return when (material) {
        Material.COPPER -> Color(0xFFB87333)
        Material.WATER -> Color(0xFF0000FF)
        Material.WOOD -> Color(0xFF8B4513)
        Material.ALUMINUM -> Color(0xFFD1D1D1)
        Material.IRON -> Color(0xFF434343)
        Material.GLASS -> Color(0xFFADD8E6)
        Material.STONE -> Color(0xFF808080)
        Material.AIR -> Color(0xFFF0F8FF)
        Material.BARRIER -> Color.Black
    }
}

@Composable
fun SimulationGrid(data: Array<DoubleArray>) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val rows = data.size
        val cols = if (rows > 0) data[0].size else 0
        if (rows == 0 || cols == 0) return@Canvas

        val cellWidth = size.width / cols
        val cellHeight = size.height / rows

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val temp = data[r][c]
                val color = temperatureToColor(temp)
                drawRect(
                    color = color,
                    topLeft = Offset(c * cellWidth, r * cellHeight),
                    size = Size(cellWidth, cellHeight)
                )
            }
        }
    }
}

fun temperatureToColor(temp: Double): Color {
    val normalized = ((temp - 20.0) / 80.0).coerceIn(0.0, 1.0).toFloat()
    return Color(
        red = normalized,
        green = 1f - normalized,
        blue = 1f - (normalized * 2).coerceIn(0f, 1f),
        alpha = 1f
    )
}

expect fun runSimulation()