package io.jadie

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

/**
 * Configuration settings for the UI simulation view.
 * 
 * @property width Grid width (number of columns).
 * @property height Grid height (number of rows).
 * @property globalTemp Initial temperature for all cells (°C).
 * @property globalMaterial Default material for all cells.
 * @property iterations Number of simulation steps to run per execution.
 * @property dslConfig The raw DSL string defining the simulation.
 */
data class SimSettings(
    val width: Int = 50,
    val height: Int = 50,
    val globalTemp: Double = 20.0,
    val globalMaterial: Material = Material.IRON,
    val iterations: Int = 100,
    val dslConfig: String =
        """
        simulate {
            grid(50, 50)
            globalMaterial(Material.IRON)
            globalTemperature(20.0)
            temp(100.0, 25, 25)
        }
        """.trimIndent(),
)

/**
 * Runs the thermal simulation based on the provided [settings].
 * 
 * This function is expected to be implemented in platform-specific code (e.g., `jvmMain`).
 * It handles the creation and execution of the simulation and reports updates back to the UI.
 * 
 * @param settings The configuration for the simulation.
 * @param onUpdate Callback function invoked with the updated temperature field, material field, 
 *                 and current iteration count.
 */
expect fun runSimulation(
    settings: SimSettings,
    onUpdate: (Array<DoubleArray>, Array<Array<Material>>, Int, Double) -> Unit,
)

/**
 * The main Compose Multiplatform entry point for the Ouizja UI.
 * 
 * This component provides a side panel for DSL configuration and simulation control,
 * and a main area for visualizing the thermal or material grid.
 */
@Composable
@Preview
fun App() {
    var isRunning by remember { mutableStateOf(false) }
    var settings by remember { mutableStateOf(SimSettings()) }
    var gridData by remember(settings.width, settings.height) {
        mutableStateOf(Array(settings.height) { DoubleArray(settings.width) { settings.globalTemp } })
    }
    var materialData by remember(settings.width, settings.height) {
        mutableStateOf(Array(settings.height) { Array(settings.width) { settings.globalMaterial } })
    }
    var iteration by remember { mutableStateOf(0) }
    var showMaterial by remember { mutableStateOf(false) }

    var hoverInfo by remember { mutableStateOf<Triple<Int, Int, Double>?>(null) }

    var ambientTemp by remember { mutableStateOf(293.15) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            withContext(Dispatchers.Default) {
                runSimulation(settings) { grid, materials, iter, ambient ->
                    gridData = grid
                    materialData = materials
                    iteration = iter
                    ambientTemp = ambient
                }
            }
            isRunning = false
        }
    }

    val darkColorScheme =
        darkColorScheme(
            primary = Color(0xFFD0BCFF),
            onPrimary = Color(0xFF381E72),
            primaryContainer = Color(0xFF4F378B),
            onPrimaryContainer = Color(0xFFEADDFF),
            secondary = Color(0xFFCCC2DC),
            onSecondary = Color(0xFF332D41),
            background = Color(0xFF1C1B1F),
            onBackground = Color(0xFFE6E1E5),
            surface = Color(0xFF1C1B1F),
            onSurface = Color(0xFFE6E1E5),
        )

    MaterialTheme(colorScheme = darkColorScheme) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier =
                        Modifier
                            .width(350.dp)
                            .fillMaxHeight()
                            .padding(16.dp),
                ) {
                    Text(
                        text = "Ouizja",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Iteration: $iteration", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { isRunning = !isRunning },
                        ) {
                            Text(if (isRunning) "Stop" else "Start")
                        }

                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { iteration = 0 },
                        ) {
                            Text("Reset")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "DSL Configuration", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = settings.dslConfig,
                        onValueChange = { settings = settings.copy(dslConfig = it) },
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        textStyle =
                            MaterialTheme.typography.bodySmall.copy(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            ),
                        label = { Text("Simulation DSL") },
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Control", style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = settings.iterations.toString(),
                        onValueChange = { settings = settings.copy(iterations = it.toIntOrNull() ?: settings.iterations) },
                        label = { Text("Max Iterations") },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "View Mode", style = MaterialTheme.typography.titleSmall)
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(
                            selected = !showMaterial,
                            onClick = { showMaterial = false },
                        )
                        Text("Temperature")
                        Spacer(modifier = Modifier.width(16.dp))
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
                                    hoverInfo = Triple(c, r, (gridData.getOrNull(r)?.getOrNull(c) ?: 0.0))
                                }
                            } else {
                                SimulationGrid(gridData, ambientTemp) { r, c ->
                                    hoverInfo = Triple(c, r, (gridData.getOrNull(r)?.getOrNull(c) ?: 0.0))
                                }
                            }
                        }

                        hoverInfo?.let { (x, y, temp) ->
                            val mat = materialData.getOrNull(y)?.getOrNull(x) ?: Material.BARRIER
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                            ) {
                                Text(
                                    text = "X: $x, Y: $y | Temp: ${temp.format(2)}K | Material: $mat",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(8.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.dividerSpacerHeight() = this.height(16.dp)

fun Double.format(digits: Int) = this.toString()

/**
 * A Compose component that renders a grid based on the cell [Material].
 * 
 * @param data 2D array of [Material] for each cell.
 * @param onHover Callback invoked with (row, column) when the mouse hovers over a cell.
 */
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

/**
 * A Compose component that renders a grid based on cell temperatures.
 * 
 * @param data 2D array of temperature values for each cell.
 * @param onHover Callback invoked with (row, column) when the mouse hovers over a cell.
 */
@Composable
fun SimulationGrid(
    data: Array<DoubleArray>,
    tAmbient: Double,
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
        val max = data.maxOf { row -> row.max() }

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val temp = data[r][c]
                val color = temperatureToColor(temp, tAmbient, max)
                drawRect(
                    color = color,
                    topLeft = Offset(c * cellWidth, r * cellHeight),
                    size = Size(cellWidth, cellHeight),
                )
            }
        }
    }
}

fun temperatureToColor(temp: Double, tAmbient: Double, maxTemp: Double): Color {
    val normalized = ((temp - tAmbient) / (maxTemp - tAmbient)).coerceIn(0.0, 1.0).toFloat()
    return Color(
        red = normalized,
        green = 1f - normalized,
        blue = 1f - (normalized * 2).coerceIn(0f, 1f),
        alpha = 1f,
    )
}
