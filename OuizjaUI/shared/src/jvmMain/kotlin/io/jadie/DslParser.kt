package io.jadie

import io.jadie.sim.Material
import io.jadie.sim.Simulation
import javax.script.ScriptEngineManager

class DslParser {
    fun parse(dslConfig: String): Simulation {
        val engine = ScriptEngineManager().getEngineByExtension("kts")

        // Remove the "simulation {" and "}" wrapper if it exists to allow the apply block to work correctly
        // The user is expected to paste "simulation { ... }"
        var cleanedConfig = dslConfig.trim()
        if (cleanedConfig.startsWith("simulation")) {
            cleanedConfig = cleanedConfig.substringAfter("{").substringBeforeLast("}")
        }

        val wrappedScript =
            """
            import io.jadie.*
            import io.jadie.sim.*
            
            val simulation = Simulation()
            simulation.apply {
                $cleanedConfig
            }
            simulation
            """.trimIndent()

        return engine.eval(wrappedScript) as Simulation
    }
}
