package org.khdl.dsl

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import org.khdl.collections.immutable.PersistentList
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public class Module private constructor(
    public val name: String,
    internal val inputs: PersistentMap<String, Type>,
    internal val outputs: PersistentMap<String, Signal<*>>,
) {
    public class Builder(public val name: String) {
        private val usedPortNames = hashSetOf<String>()
        private val inputs = persistentMapOf<String, Type>().builder()
        private val outputs = persistentMapOf<String, Signal<*>>().builder()

        public fun <T : Type> addInput(name: String, type: T): Signal<T> {
            check(usedPortNames.add(name))
            inputs[name] = type
            return Signal(type, PersistentList(type.bitWidth) { InputWire(name, it) })
        }

        public fun <T : Type> Port<T>.exportInput(name: String) {
            connectDriver(addInput(name, signal.type))
        }

        public fun addOutput(name: String, value: Signal<*>) {
            check(usedPortNames.add(name))
            outputs[name] = value
        }

        public fun Port<*>.exportOutput(name: String) {
            addOutput(name, signal)
        }

        public fun build(): Module {
            return Module(name, inputs.build(), outputs.build())
        }
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun buildModule(name: String, block: Module.Builder.() -> Unit): Module {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return Module.Builder(name).apply(block).build()
}
