package org.khdl

import java.lang.Appendable

public class Module private constructor(
    public val name: String,
    internal val inputs: List<Pair<String, WireBundle>>,
    internal val outputs: List<Pair<String, WireBundle>>,
    internal val components: List<Component>,
) {
    public fun toSystemVerilog(destination: Appendable) {
        SystemVerilogOutput(destination).appendModule(this)
    }

    public fun toSystemVerilog(): String {
        return buildString { toSystemVerilog(this) }
    }

    public class Builder(public val name: String) {
        private val usedPortNames = hashSetOf<String>()
        private val inputs = mutableListOf<Pair<String, WireBundle>>()
        private val outputs = mutableListOf<Pair<String, WireBundle>>()

        public fun addInput(name: String, width: Int): WireBundle {
            check(!usedPortNames.add(name))
            require(width > 0)
            val wires = if (width == 1) Wire(null) else ArrayWireBundle(Array(width) { Wire(null) })
            inputs.add(name to wires)
            return wires
        }

        public fun addOutput(name: String, drivers: WireBundle) {
            check(!usedPortNames.add(name))
            outputs.add(name to drivers)
        }

        public fun build(): Module {
            val foundComponents = linkedSetOf<Component>()
            val componentsToVisit = ArrayDeque<Component>()

            for ((_, wires) in outputs) {
                wires.forEach { wire ->
                    wire.driver?.let {
                        componentsToVisit.add(it)
                    }
                }
            }

            while (componentsToVisit.isNotEmpty()) {
                val component = componentsToVisit.removeFirst()

                if (component in foundComponents) {
                    continue
                }

                component.visitInputWires { wire ->
                    wire.driver?.let {
                        componentsToVisit.add(it)
                    }
                }

                foundComponents.add(component)
            }

            return Module(name, inputs.toList(), outputs.toList(), foundComponents.toList())
        }
    }
}
