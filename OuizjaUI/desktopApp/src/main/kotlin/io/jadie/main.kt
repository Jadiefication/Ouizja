package io.jadie

import androidx.compose.material.Button
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "OuizjaUI",
    ) {
        App()
    }
}