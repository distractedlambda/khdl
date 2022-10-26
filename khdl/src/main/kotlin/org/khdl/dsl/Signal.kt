package org.khdl.dsl

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

public class Signal<out T : Type> internal constructor(
    public val type: T,
    internal val wires: PersistentList<Wire>,
) {
    init {
        require(type.bitWidth == wires.size)
    }
}

public fun <T : Type> Signal<*>.bitCastTo(type: T): Signal<T> {
    require(this.type.bitWidth == type.bitWidth)
    return Signal(type, wires)
}

public infix fun <T : Type> Signal<T>.bitwiseEq(rhs: Signal<T>): Signal<Bit> {
    require(type == rhs.type)
    return Signal(Bit, persistentListOf(UnsignedEqWire(wires, rhs.wires)))
}

public infix fun <T : Type> Signal<T>.bitwiseNe(rhs: Signal<T>): Signal<Bit> {
    require(type == rhs.type)
    return Signal(Bit, persistentListOf(UnsignedNeWire(wires, rhs.wires)))
}
