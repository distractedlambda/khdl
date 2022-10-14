package org.khdl.dsl

import java.lang.Math.multiplyExact
import java.util.Objects.checkIndex

public data class Vector<T : Type>(val element: T, val size: Int) : Type {
    init {
        require(size >= 0)
    }

    override val bitWidth: Int get() = multiplyExact(element.bitWidth, size)
}

public fun zeros(width: Int): Signal<Vector<Bit>> {
    return zero().repeat(width)
}

public fun ones(width: Int): Signal<Vector<Bit>> {
    return one().repeat(width)
}

public operator fun <T : Type> Signal<Vector<T>>.get(index: Int): Signal<T> {
    checkIndex(index, type.size)
    TODO()
}

public fun <T : Type> Signal<T>.repeat(times: Int): Signal<Vector<T>> {
    return when {
        times < 0 -> throw IllegalArgumentException()
        times == 0 -> Signal(Vector(type, 0), NilNode)
        else -> Signal(Vector(type, times), RepeatNode(node, times))
    }
}
