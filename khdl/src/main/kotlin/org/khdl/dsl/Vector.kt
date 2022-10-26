package org.khdl.dsl

import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import org.khdl.collections.immutable.PersistentList
import org.khdl.collections.immutable.buildPersistentList
import java.lang.Math.addExact
import java.lang.Math.multiplyExact
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
        buildPersistentList {
            repeat(size) { index ->
                block(index).let {
                    require(it.type == element)
                    addAll(it.wires)
                }
            }
        },
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
    return Signal(type.element, PersistentList(type.element.bitWidth) { wires[index * type.element.bitWidth + it] })
}

public operator fun <T : Type> Signal<Vector<T>>.get(indices: IntProgression): Signal<Vector<T>> {
    var size = 0

    val wires = buildPersistentList {
        for (i in indices) {
            addAll(this@get[i].wires)
            size++
        }
    }

    return Signal(Vector(type.element, size), wires)
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
    return Signal(Vector(type, times), buildPersistentList {
        repeat(times) {
            addAll(wires)
        }
    })
}

public fun <T : Type> Signal<Vector<Vector<T>>>.flatten(): Signal<Vector<T>> {
    return Signal(Vector(type.element.element, multiplyExact(type.size, type.element.size)), wires)
}

public fun <T : Type> Signal<Vector<T>>.repeatConcat(times: Int): Signal<Vector<T>> {
    return repeat(times).flatten()
}

public operator fun <T : Type> Signal<Vector<T>>.plus(rhs: Signal<Vector<T>>): Signal<Vector<T>> {
    require(type.element == rhs.type.element)
    return Signal(Vector(type.element, addExact(type.size, rhs.type.size)), wires + rhs.wires)
}

public fun Signal<Vector<Bit>>.all(): Signal<Bit> {
    return Signal(Bit, persistentListOf(AndWire(wires)))
}

public fun Signal<Vector<Bit>>.any(): Signal<Bit> {
    return Signal(Bit, persistentListOf(OrWire(wires)))
}

public fun Signal<Vector<Bit>>.parity(): Signal<Bit> {
    return Signal(Bit, persistentListOf(XorWire(wires)))
}

public fun Signal<Vector<Bit>>.inv(): Signal<Vector<Bit>> {
    return Signal(type, PersistentList(wires.size) { NotWire(wires[it]) })
}

public infix fun Signal<Vector<Bit>>.and(rhs: Signal<Vector<Bit>>): Signal<Vector<Bit>> {
    require(type == rhs.type)
    return Signal(type, PersistentList(wires.size) { AndWire(persistentListOf(wires[it], rhs.wires[it])) })
}

public infix fun Signal<Vector<Bit>>.or(rhs: Signal<Vector<Bit>>): Signal<Vector<Bit>> {
    require(type == rhs.type)
    return Signal(type, PersistentList(wires.size) { OrWire(persistentListOf(wires[it], rhs.wires[it])) })
}

public infix fun Signal<Vector<Bit>>.xor(rhs: Signal<Vector<Bit>>): Signal<Vector<Bit>> {
    require(type == rhs.type)
    return Signal(type, PersistentList(wires.size) { XorWire(persistentListOf(wires[it], rhs.wires[it])) })
}
