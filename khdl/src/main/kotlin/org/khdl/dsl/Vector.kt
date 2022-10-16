package org.khdl.dsl

import java.lang.Math.addExact
import java.lang.Math.multiplyExact
import java.util.Objects.checkFromIndexSize
import java.util.Objects.checkIndex
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public data class Vector<T : Type>(val element: T, val size: Int) : Type {
    init {
        require(size >= 0)
    }

    override val bitWidth: Int get() = multiplyExact(element.bitWidth, size)
}

public val Signal<*>.bits: Signal<Vector<Bit>> get() {
    return bitCastTo(Vector(Bit, type.bitWidth))
}

@OptIn(ExperimentalContracts::class)
public fun <T : Type> vector(element: T, size: Int, block: (Int) -> Signal<T>): Signal<Vector<T>> {
    contract {
        callsInPlace(block, InvocationKind.UNKNOWN)
    }

    return Signal(
        Vector(element, size),
        ConcatNode(
            Array(size) { index ->
                block(index).let {
                    require(it.type == element)
                    it.node
                }
            }
        ),
    )
}

public fun zeros(width: Int): Signal<Vector<Bit>> {
    return zero().repeat(width)
}

public fun ones(width: Int): Signal<Vector<Bit>> {
    return one().repeat(width)
}

public fun dontCare(width: Int): Signal<Vector<Bit>> {
    return dontCare().repeat(width)
}

public fun highImpedance(width: Int): Signal<Vector<Bit>> {
    return highImpedance().repeat(width)
}

public val Signal<Vector<*>>.indices: IntRange get() {
    return 0 until type.size
}

public operator fun <T : Type> Signal<Vector<T>>.iterator(): Iterator<Signal<T>> {
    return iterator {
        for (i in indices) {
            yield(get(i))
        }
    }
}

@OptIn(ExperimentalContracts::class)
public inline fun <T : Type> Signal<Vector<T>>.forEach(block: (Signal<T>) -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.UNKNOWN)
    }

    for (i in indices) {
        block(get(i))
    }
}

public operator fun <T : Type> Signal<Vector<T>>.get(index: Int): Signal<T> {
    checkIndex(index, type.size)
    return Signal(type.element, SliceNode(node, multiplyExact(index, type.element.bitWidth), type.element.bitWidth))
}

public operator fun <T : Type> Signal<Vector<T>>.get(indices: IntProgression): Signal<Vector<T>> {
    return if (indices.step == 1) {
        val last = indices.last
        val size = last - indices.first + 1
        checkFromIndexSize(indices.first, size, type.size)
        Signal(
            Vector(type.element, size),
            SliceNode(
                node,
                multiplyExact(indices.first, type.element.bitWidth),
                multiplyExact(size, type.element.bitWidth),
            ),
        )
    } else {
        val indexList = indices.toList()
        Signal(
            Vector(type.element, indexList.size),
            ConcatNode(
                indexList.map {
                    checkIndex(it, type.size)
                    SliceNode(node, multiplyExact(it, type.element.bitWidth), type.element.bitWidth)
                }.toTypedArray()
            ),
        )
    }
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

public operator fun <T : Type> Signal<Vector<T>>.component0(): Signal<T> {
    return get(0)
}

public operator fun <T : Type> Signal<Vector<T>>.component1(): Signal<T> {
    return get(1)
}

public operator fun <T : Type> Signal<Vector<T>>.component2(): Signal<T> {
    return get(2)
}

public operator fun <T : Type> Signal<Vector<T>>.component3(): Signal<T> {
    return get(3)
}

public operator fun <T : Type> Signal<Vector<T>>.component4(): Signal<T> {
    return get(4)
}

public operator fun <T : Type> Signal<Vector<T>>.component5(): Signal<T> {
    return get(5)
}

public operator fun <T : Type> Signal<Vector<T>>.component6(): Signal<T> {
    return get(6)
}

public operator fun <T : Type> Signal<Vector<T>>.component7(): Signal<T> {
    return get(7)
}

public operator fun <T : Type> Signal<Vector<T>>.component8(): Signal<T> {
    return get(8)
}

public operator fun <T : Type> Signal<Vector<T>>.component9(): Signal<T> {
    return get(9)
}

public fun <T : Type> Signal<T>.repeat(times: Int): Signal<Vector<T>> {
    return Signal(Vector(type, times), RepeatNode(node, times))
}

public fun <T : Type> Signal<Vector<Vector<T>>>.flatten(): Signal<Vector<T>> {
    return Signal(Vector(type.element.element, multiplyExact(type.size, type.element.size)), node)
}

public fun <T : Type> Signal<Vector<T>>.repeatConcat(times: Int): Signal<Vector<T>> {
    return repeat(times).flatten()
}

public operator fun <T : Type> Signal<Vector<T>>.plus(rhs: Signal<Vector<T>>): Signal<Vector<T>> {
    require(type.element == rhs.type.element)
    return Signal(Vector(type.element, addExact(type.size, rhs.type.size)), ConcatNode(arrayOf(node, rhs.node)))
}

public fun Signal<Vector<Bit>>.all(): Signal<Bit> {
    return Signal(Bit, ReductiveAndNode(node))
}

public fun Signal<Vector<Bit>>.any(): Signal<Bit> {
    return Signal(Bit, ReductiveOrNode(node))
}

public fun Signal<Vector<Bit>>.parity(): Signal<Bit> {
    return Signal(Bit, ReductiveXorNode(node))
}

public fun Signal<Vector<Bit>>.inv(): Signal<Vector<Bit>> {
    return Signal(type, OnesComplementNode(node))
}

public infix fun Signal<Vector<Bit>>.and(rhs: Signal<Vector<Bit>>): Signal<Vector<Bit>> {
    require(type == rhs.type)
    return Signal(type, AndNode(node, rhs.node))
}

public infix fun Signal<Vector<Bit>>.or(rhs: Signal<Vector<Bit>>): Signal<Vector<Bit>> {
    require(type == rhs.type)
    return Signal(type, OrNode(node, rhs.node))
}

public infix fun Signal<Vector<Bit>>.xor(rhs: Signal<Vector<Bit>>): Signal<Vector<Bit>> {
    require(type == rhs.type)
    return Signal(type, XorNode(node, rhs.node))
}
