package org.khdl.dsl

import org.khdl.collections.immutable.PersistentList

public class Register<T : Type>(type: T, clock: Signal<Clock>, clockEnable: Signal<Bit> = one()) {
    public val signal: Signal<T> = Signal(type, PersistentList(type.bitWidth) {
        RegisteredWire(SplicedWire(), clock.wires.single(), clockEnable.wires.single())
    })

    public fun connectInput(input: Signal<T>) {
        require(input.type == signal.type)
        signal.wires.forEachIndexed { i, wire ->
            ((wire as RegisteredWire).input as SplicedWire).driver = input.wires[i]
        }
    }
}
