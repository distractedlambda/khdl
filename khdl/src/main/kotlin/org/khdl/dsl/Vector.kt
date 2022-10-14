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
    return Signal(type.element, SliceNode(node, multiplyExact(index, type.element.bitWidth), type.element.bitWidth))
}

public fun <T : Type> Signal<Vector<T>>.first(): Signal<T> {
    return if (type.size == 0) {
        throw NoSuchElementException()
    } else {
        get(0)
    }
}

public fun <T : Type> Signal<Vector<T>>.last(): Signal<T> {
    return if (type.size == 0) {
        throw NoSuchElementException()
    } else {
        get(type.size - 1)
    }
}

public fun <T : Type> Signal<T>.repeat(times: Int): Signal<Vector<T>> {
    return Signal(Vector(type, times), RepeatNode(node, times))
}
