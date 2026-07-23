import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.shared)

    implementation(compose.desktop.currentOs)
    implementation(libs.kotlinx.coroutinesSwing)

    implementation(libs.compose.uiToolingPreview)

    implementation("io.jadie.sim:lib:1.0-SNAPSHOT")
}

compose.desktop {
    application {
        buildTypes.release.proguard {
            isEnabled.set(false)
        }
        mainClass = "io.jadie.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "io.jadie"
            packageName = "Ouizja"
            packageVersion = "1.0.0"
            description = "High-Performance Thermal Simulation Engine"
            vendor = "Jadie"
            copyright = "© 2026 Jadie. All rights reserved."
            includeAllModules = true

            macOS {
                iconFile.set(project.file("src/resources/app_icon.icns"))
                bundleID = "io.jadie.ouizja"
            }
            windows {
                iconFile.set(project.file("src/resources/app_icon.ico"))
                shortcut = true
                menuGroup = "Ouizja"
            }
            linux {
                iconFile.set(project.file("src/resources/icon.png"))
                shortcut = true
            }
        }
    }
}
