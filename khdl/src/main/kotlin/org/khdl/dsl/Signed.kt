package org.khdl.dsl

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.khdl.collections.immutable.PersistentList

public data class Signed(override val bitWidth: Int) : Integer

public val Signal<Vector<Bit>>.signed: Signal<Signed> get() {
    return bitCastTo(Signed(type.bitWidth))
}

public val Int.signed: Signal<Signed> get() = when (this) {
    0 -> Signal(Signed(0), persistentListOf())
    else -> {
        val bitWidth = Int.SIZE_BITS - (if (this < 0) inv() else this).countLeadingZeroBits() + 1
        Signal(Signed(bitWidth), PersistentList(bitWidth) {
            if (this shr it and 1 != 0) {
                OneWire
            } else {
                ZeroWire
            }
        })
    }
}

public val Long.signed: Signal<Signed> get() = when (this) {
    0L -> Signal(Signed(0), persistentListOf())
    else -> {
        val bitWidth = Long.SIZE_BITS - (if (this < 0) inv() else this).countLeadingZeroBits() + 1
        Signal(Signed(bitWidth), PersistentList(bitWidth) {
            if (this shr it and 1 != 0L) {
                OneWire
            } else {
                ZeroWire
            }
        })
    }
}

public val UInt.signed: Signal<Signed> get() = when (this) {
    0u -> Signal(Signed(0), persistentListOf())
    else -> {
        val bitWidth = UInt.SIZE_BITS - countLeadingZeroBits() + 1
        Signal(Signed(bitWidth), PersistentList(bitWidth) {
            if (it < UInt.SIZE_BITS) {
                if (this shr it and 1u != 0u) {
                    OneWire
                } else {
                    ZeroWire
                }
            } else {
                ZeroWire
            }
        })
    }
}

public val ULong.signed: Signal<Signed> get() = when (this) {
    0uL -> Signal(Signed(0), persistentListOf())
    else -> {
        val bitWidth = ULong.SIZE_BITS - countLeadingZeroBits() + 1
        Signal(Signed(bitWidth), PersistentList(bitWidth) {
            if (it < ULong.SIZE_BITS) {
                if (this shr it and 1uL != 0uL) {
                    OneWire
                } else {
                    ZeroWire
                }
            } else {
                ZeroWire
            }
        })
    }
}

public operator fun Signal<Signed>.unaryMinus(): Signal<Signed> {
    val op = NegateOp(wires)
    return Signal(type, PersistentList(wires.size) { OpWire(op, it) })
}

private fun binaryOp(
    lhs: Signal<Signed>,
    rhs: Signal<Signed>,
    factory: (PersistentList<Wire>, PersistentList<Wire>) -> Op,
): Signal<Signed> {
    val promotedWidth = maxOf(lhs.type.bitWidth, rhs.type.bitWidth)
    val op = factory(lhs.wires, rhs.wires)
    return Signal(Signed(promotedWidth), PersistentList(promotedWidth) { OpWire(op, it) })
}

public operator fun Signal<Signed>.plus(rhs: Signal<Signed>): Signal<Signed> {
    return binaryOp(this, rhs, ::SignedAddOp)
}

public operator fun Signal<Signed>.minus(rhs: Signal<Signed>): Signal<Signed> {
    return binaryOp(this, rhs, ::SignedSubtractOp)
}

public operator fun Signal<Signed>.times(rhs: Signal<Signed>): Signal<Signed> {
    return binaryOp(this, rhs, ::SignedMultiplyOp)
}

public infix fun Signal<Signed>.eq(rhs: Signal<Signed>): Signal<Bit> {
    return Signal(Bit, persistentListOf(SignedEqWire(wires, rhs.wires)))
}

public infix fun Signal<Signed>.ne(rhs: Signal<Signed>): Signal<Bit> {
    return Signal(Bit, persistentListOf(SignedNeWire(wires, rhs.wires)))
}

public infix fun Signal<Signed>.lt(rhs: Signal<Signed>): Signal<Bit> {
    return Signal(Bit, persistentListOf(SignedLtWire(wires, rhs.wires)))
}

public infix fun Signal<Signed>.le(rhs: Signal<Signed>): Signal<Bit> {
    return Signal(Bit, persistentListOf(SignedLeWire(wires, rhs.wires)))
}

public infix fun Signal<Signed>.gt(rhs: Signal<Signed>): Signal<Bit> {
    return Signal(Bit, persistentListOf(SignedGtWire(wires, rhs.wires)))
}

public infix fun Signal<Signed>.ge(rhs: Signal<Signed>): Signal<Bit> {
    return Signal(Bit, persistentListOf(SignedGeWire(wires, rhs.wires)))
}
