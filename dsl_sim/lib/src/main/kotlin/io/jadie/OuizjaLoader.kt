package io.jadie

import java.nio.file.Files

class OuizjaLoader {
    companion object {
        init {
            loadNativeLibrary()
        }

        /**
         * Loads the `Uzyi` native library for the current OS.
         *
         * Search order:
         * 1) Bundled in `resources/native` of this module (preferred for published artifacts)
         * 2) Fallback to `System.loadLibrary("Uzyi")` for developer environments
         *
         * Throws a [RuntimeException] if the library cannot be found or loaded.
         */
        private fun loadNativeLibrary() {
            val os = System.getProperty("os.name").lowercase()
            val arch = System.getProperty("os.arch").lowercase()

            val suffix =
                when {
                    os.contains("win") -> ".dll"
                    os.contains("mac") -> ".dylib"
                    else -> ".so"
                }

            val prefix = if (os.contains("win")) "" else "lib"
            val libName = "${prefix}Ouizja$suffix"
            val resourcePath = "/native/$libName"

            val inputStream =
                OuizjaLoader::class.java.getResourceAsStream(resourcePath) ?: try {
                    System.loadLibrary("Ouizja")
                    return
                } catch (e: UnsatisfiedLinkError) {
                    throw RuntimeException("Could not find native library $libName in resources or library path")
                }

            val tempFile = Files.createTempFile("libouizja", suffix).toFile()
            tempFile.deleteOnExit()

            inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            System.load(tempFile.absolutePath)
        }

        @JvmStatic
        external fun createSim(
            temps: DoubleArray,
            sourceMask: BooleanArray,
            materialMask: IntArray,
            quantum: DoubleArray,
            winds: DoubleArray,
            length: Int,
            height: Int,
        ): Long

        @JvmStatic
        external fun runSim(
            iterations: Long,
            pointer: Long,
            length: Int,
            height: Int
        ): SimState

        @JvmStatic
        external fun freeSim(
            pointer: Long
        )
    }
}