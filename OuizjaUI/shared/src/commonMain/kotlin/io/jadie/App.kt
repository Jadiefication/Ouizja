package io.jadie

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.jadie.sim.Material
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
@Preview
fun App() {
    var isRunning by remember { mutableStateOf(false) }
    var settings by remember { mutableStateOf(SimSettings()) }
    var gridData by remember(settings.width, settings.height) {
        mutableStateOf<Array<DoubleArray>>(Array(settings.height) { DoubleArray(settings.width) { settings.globalTemp } })
    }
    var materialData by remember(settings.width, settings.height) {
        mutableStateOf<Array<Array<Material>>>(Array(settings.height) { Array(settings.width) { settings.globalMaterial } })
    }
    var iteration by remember { mutableStateOf(0) }
    var showMaterial by remember { mutableStateOf(false) }

    var hoverInfo by remember { mutableStateOf<Triple<Int, Int, Double>?>(null) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            withContext(Dispatchers.Default) {
                runSimulation(settings) { grid, materials, iter ->
                    gridData = grid
                    materialData = materials
                    iteration = iter
                }
            }
            isRunning = false
        }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier =
                        Modifier
                            .width(250.dp)
                            .fillMaxHeight()
                            .padding(16.dp),
                ) {
                    Text(
                        text = "Ouizja",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Iteration: $iteration")
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { isRunning = !isRunning },
                    ) {
                        Text(if (isRunning) "Stop" else "Start")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { iteration = 0 },
                    ) {
                        Text("Reset")
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Settings", style = MaterialTheme.typography.titleSmall)

                    OutlinedTextField(
                        value = settings.width.toString(),
                        onValueChange = { settings = settings.copy(width = it.toIntOrNull() ?: settings.width) },
                        label = { Text("Width") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = settings.height.toString(),
                        onValueChange = { settings = settings.copy(height = it.toIntOrNull() ?: settings.height) },
                        label = { Text("Height") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = settings.globalTemp.toString(),
                        onValueChange = { settings = settings.copy(globalTemp = it.toDoubleOrNull() ?: settings.globalTemp) },
                        label = { Text("Global Temp") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = settings.iterations.toString(),
                        onValueChange = { settings = settings.copy(iterations = it.toIntOrNull() ?: settings.iterations) },
                        label = { Text("Iterations") },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Global Material", style = MaterialTheme.typography.titleSmall)

                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(settings.globalMaterial.name)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            Material.entries.forEach { material ->
                                DropdownMenuItem(
                                    text = { Text(material.name) },
                                    onClick = {
                                        settings = settings.copy(globalMaterial = material)
                                        expanded = false
                                    },
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.dividerSpacerHeight())
                    HorizontalDivider()
                    Spacer(modifier = Modifier.dividerSpacerHeight())

                    Text(text = "View Mode", style = MaterialTheme.typography.titleSmall)
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(
                            selected = !showMaterial,
                            onClick = { showMaterial = false },
                        )
                        Text("Temperature")
                    }
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(
                            selected = showMaterial,
                            onClick = { showMaterial = true },
                        )
                        Text("Material")
                    }
                }

                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(16.dp),
                ) {
                    Column {
                        Box(modifier = Modifier.weight(1f)) {
                            if (showMaterial) {
                                MaterialGrid(materialData) { r, c ->
                                    hoverInfo = Triple(c, r, gridData[r][c])
                                }
                            } else {
                                SimulationGrid(gridData) { r, c ->
                                    hoverInfo = Triple(c, r, gridData[r][c])
                                }
                            }
                        }

                        hoverInfo?.let { (x, y, temp) ->
                            val mat = materialData[y][x]
                            Text(
                                text = "X: $x, Y: $y | Temp: ${temp.format(2)}°C | Material: $mat",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.dividerSpacerHeight() = this.height(16.dp)

fun Double.format(digits: Int) = this.toString() // Placeholder for multiplatform formatting

@Composable
fun MaterialGrid(
    data: Array<Array<Material>>,
    onHover: (Int, Int) -> Unit,
) {
    Canvas(
        modifier =
            Modifier.fillMaxSize().pointerInput(data) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Move || event.type == PointerEventType.Enter) {
                            val position = event.changes.first().position
                            val rows = data.size
                            val cols = if (rows > 0) data[0].size else 0
                            if (rows > 0 && cols > 0) {
                                val cellWidth = size.width / cols
                                val cellHeight = size.height / rows
                                val c = (position.x / cellWidth).toInt().coerceIn(0, cols - 1)
                                val r = (position.y / cellHeight).toInt().coerceIn(0, rows - 1)
                                onHover(r, c)
                            }
                        }
                    }
                }
            },
    ) {
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
                    size = Size(cellWidth, cellHeight),
                )
            }
        }
    }
}

fun materialToColor(material: Material): Color =
    when (material) {
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

@Composable
fun SimulationGrid(
    data: Array<DoubleArray>,
    onHover: (Int, Int) -> Unit,
) {
    Canvas(
        modifier =
            Modifier.fillMaxSize().pointerInput(data) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.type == PointerEventType.Move || event.type == PointerEventType.Enter) {
                            val position = event.changes.first().position
                            val rows = data.size
                            val cols = if (rows > 0) data[0].size else 0
                            if (rows > 0 && cols > 0) {
                                val cellWidth = size.width / cols
                                val cellHeight = size.height / rows
                                val c = (position.x / cellWidth).toInt().coerceIn(0, cols - 1)
                                val r = (position.y / cellHeight).toInt().coerceIn(0, rows - 1)
                                onHover(r, c)
                            }
                        }
                    }
                }
            },
    ) {
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
                    size = Size(cellWidth, cellHeight),
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
        alpha = 1f,
    )
}

data class SimSettings(
    val width: Int = 50,
    val height: Int = 50,
    val globalTemp: Double = 20.0,
    val globalMaterial: Material = Material.IRON,
    val iterations: Int = 100,
)

expect fun runSimulation(
    settings: SimSettings,
    onUpdate: (Array<DoubleArray>, Array<Array<Material>>, Int) -> Unit,
)
