package org.khdl.dsl

import org.khdl.collections.immutable.PersistentList

public sealed interface Input<out T : Type> {
    public val signal: Signal<T>
}

public sealed interface Output<in T : Type> {
    public fun connectDriver(driver: Signal<T>)
}

public class Port<T : Type>(type: T) : Input<T>, Output<T> {
    override val signal: Signal<T> = Signal(type, PersistentList(type.bitWidth) { SplicedWire() })

    override fun connectDriver(driver: Signal<T>) {
        require(driver.type == signal.type)
        signal.wires.forEachIndexed { i, wire ->
            (wire as SplicedWire).driver = driver.wires[i]
        }
    }
}
