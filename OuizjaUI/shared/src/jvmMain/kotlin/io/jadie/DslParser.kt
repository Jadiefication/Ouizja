package io.jadie

import io.jadie.sim.BuiltSim
import io.jadie.sim.Material
import io.jadie.sim.Simulation
import javax.script.ScriptEngineManager

/**
 * Utility for parsing and executing Kotlin DSL simulation configurations at runtime.
 * 
 * This parser uses the Kotlin Scripting engine to evaluate DSL strings and convert them
 * into [BuiltSim] instances.
 */
class DslParser {
    /**
     * Parses a Kotlin DSL configuration string into a [BuiltSim].
     * 
     * The input string can optionally be wrapped in a `simulation { ... }` block.
     * 
     * @param dslConfig The DSL configuration as a string.
     * @return A [BuiltSim] instance ready to be run.
     * @throws javax.script.ScriptException if the script contains syntax errors or invalid DSL calls.
     */
    fun parse(dslConfig: String): BuiltSim {
        val engine = ScriptEngineManager().getEngineByExtension("kts")

        var cleanedConfig = dslConfig.trim()
        if (cleanedConfig.startsWith("simulate")) {
            cleanedConfig = cleanedConfig.substringAfter("{").substringBeforeLast("}")
        }

        val wrappedScript =
            """
            import io.jadie.*
            import io.jadie.sim.*
            
            val simulation = simulate {
                $cleanedConfig
            }
            simulation
            """.trimIndent()

        return engine.eval(wrappedScript) as BuiltSim
    }
}
