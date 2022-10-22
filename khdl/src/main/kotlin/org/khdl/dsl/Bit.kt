package org.khdl.dsl

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

public object Bit : Type {
    override val bitWidth: Int get() = 1
}

public val Boolean.bit: Signal<Bit> get() {
    return if (this) one() else zero()
}

public fun zero(): Signal<Bit> {
    return Signal(Bit, persistentListOf(ZeroWire))
}

public fun one(): Signal<Bit> {
    return Signal(Bit, persistentListOf(OneWire))
}

public fun dontCare(): Signal<Bit> {
    return Signal(Bit, persistentListOf(DontCareWire))
}

public fun highImpedance(): Signal<Bit> {
    return Signal(Bit, persistentListOf(HighImpedanceWire))
}

public infix fun Signal<Bit>.and(rhs: Signal<Bit>): Signal<Bit> {
    return Signal(Bit, persistentListOf(AndWire(persistentSetOf(wires.single(), rhs.wires.single()))))
}

public infix fun Signal<Bit>.or(rhs: Signal<Bit>): Signal<Bit> {
    return Signal(Bit, persistentListOf(OrWire(persistentSetOf(wires.single(), rhs.wires.single()))))
}

public infix fun Signal<Bit>.xor(rhs: Signal<Bit>): Signal<Bit> {
    return Signal(Bit, persistentListOf(XorWire(persistentSetOf(wires.single(), rhs.wires.single()))))
}

public fun Signal<Bit>.inv(): Signal<Bit> {
    return Signal(Bit, persistentListOf(NotWire(wires.single())))
}
