package org.khdl.dsl

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.khdl.collections.immutable.PersistentList

public data class Unsigned(override val bitWidth: Int) : Integer

public val Signal<Vector<Bit>>.unsigned: Signal<Unsigned> get() {
    return bitCastTo(Unsigned(type.bitWidth))
}

public val Int.unsigned: Signal<Unsigned> get() = when (this) {
    0 -> Signal(Unsigned(0), persistentListOf())

    in 1..Int.MAX_VALUE -> {
        val bitWidth = Int.SIZE_BITS - countLeadingZeroBits()
        Signal(Unsigned(bitWidth), PersistentList(bitWidth) {
            if (this shr it and 1 != 0) {
                OneWire
            } else {
                ZeroWire
            }
        })
    }

    else -> throw IllegalArgumentException()
}

public val Long.unsigned: Signal<Unsigned> get() = when (this) {
    0L -> Signal(Unsigned(0), persistentListOf())

    in 1..Long.MAX_VALUE -> {
        val bitWidth = Long.SIZE_BITS - countLeadingZeroBits()
        Signal(Unsigned(bitWidth), PersistentList(bitWidth) {
            if (this shr it and 1 != 0L) {
                OneWire
            } else {
                ZeroWire
            }
        })
    }

    else -> throw IllegalArgumentException()
}

public val UInt.unsigned: Signal<Unsigned> get() = when (this) {
    0u -> Signal(Unsigned(0), persistentListOf())

    else -> {
        val bitWidth = UInt.SIZE_BITS - countLeadingZeroBits()
        Signal(Unsigned(bitWidth), PersistentList(bitWidth) {
            if (this shr it and 1u != 0u) {
                OneWire
            } else {
                ZeroWire
            }
        })
    }
}

public val ULong.unsigned: Signal<Unsigned> get() = when (this) {
    0uL -> Signal(Unsigned(0), persistentListOf())

    else -> {
        val bitWidth = ULong.SIZE_BITS - countLeadingZeroBits()
        Signal(Unsigned(bitWidth), PersistentList(bitWidth) {
            if (this shr it and 1u != 0uL) {
                OneWire
            } else {
                ZeroWire
            }
        })
    }
}

private fun binaryOp(
    lhs: Signal<Unsigned>,
    rhs: Signal<Unsigned>,
    factory: (PersistentList<Wire>, PersistentList<Wire>) -> Op,
): Signal<Unsigned> {
    val promotedWidth = maxOf(lhs.type.bitWidth, rhs.type.bitWidth)
    val op = factory(lhs.wires, rhs.wires)
    return Signal(Unsigned(promotedWidth), PersistentList(promotedWidth) { OpWire(op, it) })
}

public operator fun Signal<Unsigned>.plus(rhs: Signal<Unsigned>): Signal<Unsigned> {
    return binaryOp(this, rhs, ::UnsignedAddOp)
}

public operator fun Signal<Unsigned>.minus(rhs: Signal<Unsigned>): Signal<Unsigned> {
    return binaryOp(this, rhs, ::UnsignedSubtractOp)
}

public operator fun Signal<Unsigned>.times(rhs: Signal<Unsigned>): Signal<Unsigned> {
    return binaryOp(this, rhs, ::UnsignedMultiplyOp)
}

public infix fun Signal<Unsigned>.eq(rhs: Signal<Unsigned>): Signal<Bit> {
    return Signal(Bit, persistentListOf(UnsignedEqWire(wires, rhs.wires)))
}

public infix fun Signal<Unsigned>.ne(rhs: Signal<Unsigned>): Signal<Bit> {
    return Signal(Bit, persistentListOf(UnsignedNeWire(wires, rhs.wires)))
}

public infix fun Signal<Unsigned>.lt(rhs: Signal<Unsigned>): Signal<Bit> {
    return Signal(Bit, persistentListOf(UnsignedLtWire(wires, rhs.wires)))
}

public infix fun Signal<Unsigned>.le(rhs: Signal<Unsigned>): Signal<Bit> {
    return Signal(Bit, persistentListOf(UnsignedLeWire(wires, rhs.wires)))
}

public infix fun Signal<Unsigned>.gt(rhs: Signal<Unsigned>): Signal<Bit> {
    return Signal(Bit, persistentListOf(UnsignedGtWire(wires, rhs.wires)))
}

public infix fun Signal<Unsigned>.ge(rhs: Signal<Unsigned>): Signal<Bit> {
    return Signal(Bit, persistentListOf(UnsignedGeWire(wires, rhs.wires)))
}
