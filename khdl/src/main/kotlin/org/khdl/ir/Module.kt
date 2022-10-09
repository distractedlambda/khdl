package org.khdl.ir

public class Module private constructor(
    public val name: String,
    internal val inputs: List<ModuleInput>,
    internal val outputs: List<OutputPort>,
) {
    public class Builder(public val name: String) {
        private val usedPortNames = hashSetOf<String>()
        private val inputs = mutableListOf<ModuleInput>()
        private val outputs = mutableListOf<OutputPort>()

        public fun addInput(name: String, width: Int = 1): BitVector {
            check(usedPortNames.add(name))
            return ModuleInput(name, width).also { inputs.add(it) }
        }

        public fun addOutput(name: String, value: BitVector) {
            check(usedPortNames.add(name))
            outputs.add(OutputPort(name, value))
        }

        public fun build(): Module {
            return Module(name, inputs.toList(), outputs.toList())
        }
    }

    internal data class OutputPort(val name: String, val driver: BitVector)
}
