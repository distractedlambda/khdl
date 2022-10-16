package org.khdl.dsl

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

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

        public fun <T : Type> Wire<T>.exportInput(name: String) {
            connectDriver(addInput(name, signal.type))
        }

        public fun addOutput(name: String, value: Signal<*>) {
            check(usedPortNames.add(name))
            outputs.add(OutputPort(name, value.node))
        }

        public fun Wire<*>.exportOutput(name: String) {
            addOutput(name, signal)
        }

        public fun build(): Module {
            return Module(name, inputs.toList(), outputs.toList())
        }
    }

    internal data class OutputPort(val name: String, val driver: Node)
}

@OptIn(ExperimentalContracts::class)
public inline fun buildModule(name: String, block: Module.Builder.() -> Unit): Module {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return Module.Builder(name).apply(block).build()
}
