package org.khdl.dsl

import kotlinx.collections.immutable.PersistentList

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
