package org.khdl

import java.util.IdentityHashMap

internal class SystemVerilogOutput(destination: Appendable) : Appendable by destination {
    private val wireNames = IdentityHashMap<Wire, String>()

    fun appendModule(module: Module) {
        wireNames.clear()

        val portDecls = mutableListOf<String>()

        module.inputs.mapTo(portDecls) { (name, wires) ->
            if (wires.size == 1) {
                "input logic \\$name"
            } else {
                "input logic [${wires.size - 1}:0] \\$name"
            }
        }

        module.outputs.mapTo(portDecls) { (name, wires) ->
            if (wires.size == 1) {
                "output logic \\$name"
            } else {
                "output logic [${wires.size - 1}:0] \\$name"
            }
        }

        append("module \\${module.name}(")

        if (portDecls.isNotEmpty()) {
            appendLine()

            for (i in 0 until portDecls.size - 1) {
                append("    ")
                append(portDecls[i])
                append(",")
                appendLine()
            }

            append("    ")
            appendLine(portDecls.last())
        }

        appendLine(");")
        appendLine()

        module.inputs.forEach { (name, wires) ->
            appendLine("assign ${nameWireBundle(wires)} = \\$name;")
            appendLine()
        }

        module.outputs.forEach { (name, wires) ->
            appendLine("assign \\$name = ${nameWireBundle(wires)};")
            appendLine()
        }

        module.components.forEach { component ->
            component.emitSystemVerilog(this)
            appendLine()
        }

        appendLine("endmodule;")
    }

    fun nameWire(wire: Wire): String {
        wireNames[wire]?.let { return it }
        val name = "w\$${wireNames.size}"
        appendLine("logic $name;")
        return name
    }

    fun nameWireBundle(bundle: WireBundle): String {
        return when (bundle) {
            is Wire -> nameWire(bundle)
            is ArrayWireBundle -> bundle.wires.joinToString(prefix = "{", postfix = "}", transform = ::nameWire)
        }
    }
}
