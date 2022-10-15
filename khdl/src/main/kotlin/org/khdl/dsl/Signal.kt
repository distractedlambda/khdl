package org.khdl.dsl

public class Signal<out T : Type> internal constructor(public val type: T, internal val node: Node) {
    init {
        require(type.bitWidth == node.width)
    }

    override fun equals(other: Any?): Boolean {
        return other is Signal<*> && type == other.type && node == other.node
    }

    override fun hashCode(): Int {
        return 31 * type.hashCode() + node.hashCode()
    }

    override fun toString(): String {
        return "Signal($node : $type)"
    }
}

public fun <T : Type> Signal<*>.bitCastTo(type: T): Signal<T> {
    require(this.type.bitWidth == type.bitWidth)
    return Signal(type, node)
}
