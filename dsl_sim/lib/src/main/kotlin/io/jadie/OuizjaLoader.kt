package io.jadie

import java.nio.file.Files

/**
 * Helper class for loading the native library and defining JNI interfaces.
 */
class OuizjaLoader {
    companion object {
        init {
            loadNativeLibrary()
        }

        /**
         * Loads the `Ouizja` native library for the current OS.
         *
         * Search order:
         * 1) Bundled in `resources/native` of this module (preferred for published artifacts)
         * 2) Fallback to `System.loadLibrary("Ouizja")` for developer environments
         *
         * @throws [RuntimeException] if the library cannot be found or loaded.
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

        /**
         * Creates a new simulation instance in the native environment.
         *
         * @param temps Initial temperatures for all cells.
         * @param sourceMask Boolean mask identifying heat source cells.
         * @param materialMask Integer mask identifying material IDs for each cell.
         * @param quantum Initial quantum values (gamma).
         * @param winds Wind force values.
         * @param length Grid width.
         * @param height Grid height.
         * @param tAmbient Ambient temperature.
         * @return A pointer (long) to the native simulation instance.
         */
        @JvmStatic
        external fun createSim(
            temps: DoubleArray,
            sourceMask: BooleanArray,
            materialMask: IntArray,
            quantum: DoubleArray,
            winds: DoubleArray,
            length: Int,
            height: Int,
            tAmbient: Double,
        ): Long

        /**
         * Runs the simulation for a specified number of iterations.
         *
         * @param iterations Number of simulation steps to execute.
         * @param pointer Pointer to the native simulation instance.
         * @param length Grid width.
         * @param height Grid height.
         * @return The resulting [SimState].
         */
        @JvmStatic
        external fun runSim(
            iterations: Long,
            pointer: Long,
            length: Int,
            height: Int,
        ): SimState

        /**
         * Frees the memory allocated for the native simulation instance.
         *
         * @param pointer Pointer to the native simulation instance.
         */
        @JvmStatic
        external fun freeSim(pointer: Long)
    }
}
