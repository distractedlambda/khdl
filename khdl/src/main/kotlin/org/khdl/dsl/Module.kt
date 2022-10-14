package org.khdl.dsl

public class Module private constructor(
    public val name: String,
    internal val inputs: List<ModuleInputNode>,
    internal val outputs: List<OutputPort>,
) {
    public class Builder(public val name: String) {
        private val usedPortNames = hashSetOf<String>()
        private val inputs = mutableListOf<ModuleInputNode>()
        private val outputs = mutableListOf<OutputPort>()

        public fun <T : Type> addInput(name: String, type: T): Signal<T> {
            check(usedPortNames.add(name))
            val node = ModuleInputNode(name, type.bitWidth)
            inputs.add(node)
            return Signal(type, node)
        }

        public fun addOutput(name: String, value: Signal<*>) {
            check(usedPortNames.add(name))
            outputs.add(OutputPort(name, value.node))
        }

        public fun build(): Module {
            return Module(name, inputs.toList(), outputs.toList())
        }
    }

    internal data class OutputPort(val name: String, val driver: Node)
}
