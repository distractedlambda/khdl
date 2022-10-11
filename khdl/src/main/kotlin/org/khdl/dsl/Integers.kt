package org.khdl.dsl

import org.khdl.ir.Add
import org.khdl.ir.BitVector
import org.khdl.ir.Constant

public data class UnsignedInteger(val bits: BitVector)

public val BitVector.unsigned: UnsignedInteger get() {
    return UnsignedInteger(this)
}

public val Int.unsigned: UnsignedInteger get() = when {
    this < 0 -> throw IllegalArgumentException()

    this == 0 -> {
        zero().unsigned
    }

    else -> {
        Constant(
            ByteArray(Int.SIZE_BITS - countLeadingZeroBits()) {
                ((this ushr it) and 1).toByte()
            }
        ).unsigned
    }
}

public val Long.unsigned: UnsignedInteger get() = when {
    this < 0 -> throw IllegalArgumentException()

    this == 0L -> {
        zero().unsigned
    }

    else -> {
        Constant(
            ByteArray(Long.SIZE_BITS - countLeadingZeroBits()) {
                ((this ushr it) and 1).toByte()
            }
        ).unsigned
    }
}

public val UInt.unsigned: UnsignedInteger get() = when {
    this == 0U -> {
        zero().unsigned
    }

    else -> {
        Constant(
            ByteArray(Int.SIZE_BITS - countLeadingZeroBits()) {
                ((this shr it) and 1u).toByte()
            }
        ).unsigned
    }
}

public val ULong.unsigned: UnsignedInteger get() = when {
    this == 0UL -> {
        zero().unsigned
    }

    else -> {
        Constant(
            ByteArray(Long.SIZE_BITS - countLeadingZeroBits()) {
                ((this shr it) and 1u).toByte()
            }
        ).unsigned
    }
}

public data class SignedInteger(val bits: BitVector)

public val BitVector.signed: SignedInteger get() {
    return SignedInteger(this)
}

public val Int.signed: SignedInteger get() = when {
    this < 0 -> {
        Constant(
            ByteArray(Int.SIZE_BITS - inv().countLeadingZeroBits() + 1) {
                ((this ushr it) and 1).toByte()
            }
        ).signed
    }

    this == 0 -> {
        zero().signed
    }

    else -> {
        Constant(
            ByteArray(Int.SIZE_BITS - countLeadingZeroBits() + 1) {
                ((this ushr it) and 1).toByte()
            }
        ).signed
    }
}

public val Long.signed: SignedInteger get() = when {
    this < 0 -> {
        Constant(
            ByteArray(Long.SIZE_BITS - inv().countLeadingZeroBits() + 1) {
                ((this ushr it) and 1).toByte()
            }
        ).signed
    }

    this == 0L -> {
        zero().signed
    }

    else -> {
        Constant(
            ByteArray(Long.SIZE_BITS - countLeadingZeroBits() + 1) {
                ((this ushr it) and 1).toByte()
            }
        ).signed
    }
}

public val UInt.signed: SignedInteger get() = when {
    this == 0u -> {
        zero().signed
    }

    else -> {
        Constant(
            ByteArray(Int.SIZE_BITS - countLeadingZeroBits() + 1) {
                ((this shr it) and 1u).toByte()
            }
        ).signed
    }
}

public val ULong.signed: SignedInteger get() = when {
    this == 0UL -> {
        zero().signed
    }

    else -> {
        Constant(
            ByteArray(Long.SIZE_BITS - countLeadingZeroBits() + 1) {
                ((this shr it) and 1u).toByte()
            }
        ).signed
    }
}

public fun UnsignedInteger.truncate(newWidth: Int): UnsignedInteger {
    return bits.truncate(newWidth).unsigned
}

public fun SignedInteger.truncate(newWidth: Int): SignedInteger {
    return bits.truncate(newWidth).signed
}

public fun UnsignedInteger.extend(newWidth: Int): UnsignedInteger {
    return when {
        newWidth < bits.width -> throw IllegalArgumentException()
        newWidth == bits.width -> this
        else -> UnsignedInteger(bits.zeroExtend(newWidth))
    }
}

public fun SignedInteger.extend(newWidth: Int): SignedInteger {
    return when {
        newWidth < bits.width -> throw IllegalArgumentException()
        newWidth == bits.width -> this
        else -> SignedInteger(bits.signExtend(newWidth))
    }
}

private fun promote(lhs: UnsignedInteger, rhs: UnsignedInteger): Pair<UnsignedInteger, UnsignedInteger> {
    return when {
        lhs.bits.width == rhs.bits.width -> lhs to rhs
        lhs.bits.width < rhs.bits.width -> lhs.extend(rhs.bits.width) to rhs
        else -> lhs to rhs.extend(lhs.bits.width)
    }
}

private fun promote(lhs: SignedInteger, rhs: SignedInteger): Pair<SignedInteger, SignedInteger> {
    return when {
        lhs.bits.width == rhs.bits.width -> lhs to rhs
        lhs.bits.width < rhs.bits.width -> lhs.extend(rhs.bits.width) to rhs
        else -> lhs to rhs.extend(lhs.bits.width)
    }
}

public operator fun UnsignedInteger.plus(other: UnsignedInteger): UnsignedInteger {
    val (lhs, rhs) = promote(this, other)
    return Add(lhs.bits, rhs.bits).truncate(lhs.bits.width).unsigned
}

public operator fun SignedInteger.plus(other: SignedInteger): SignedInteger {
    val (lhs, rhs) = promote(this, other)
    return Add(lhs.bits, rhs.bits).truncate(lhs.bits.width).signed
}

public fun UnsignedInteger.inv(): UnsignedInteger {
    return bits.inv().unsigned
}

public fun SignedInteger.inv(): SignedInteger {
    return bits.inv().signed
}

public operator fun UnsignedInteger.minus(other: UnsignedInteger): UnsignedInteger {
    return this + other.inv() + 1.unsigned
}

public operator fun SignedInteger.minus(other: SignedInteger): SignedInteger {
    val (lhs, rhs) = promote(this, other)
    return lhs + -rhs
}

public fun SignedInteger.reinterpretUnsigned(): UnsignedInteger {
    return bits.unsigned
}

public fun UnsignedInteger.reinterpretSigned(): SignedInteger {
    return bits.signed
}

public operator fun SignedInteger.unaryMinus(): SignedInteger {
    return (reinterpretUnsigned().inv() + 1.unsigned).reinterpretSigned()
}

public operator fun UnsignedInteger.unaryPlus(): UnsignedInteger {
    return this
}

public operator fun SignedInteger.unaryPlus(): SignedInteger {
    return this
}

public infix fun UnsignedInteger.and(other: UnsignedInteger): UnsignedInteger {
    val (lhs, rhs) = promote(this, other)
    return (lhs.bits and rhs.bits).unsigned
}

public infix fun UnsignedInteger.or(other: UnsignedInteger): UnsignedInteger {
    val (lhs, rhs) = promote(this, other)
    return (lhs.bits or rhs.bits).unsigned
}

public infix fun UnsignedInteger.xor(other: UnsignedInteger): UnsignedInteger {
    val (lhs, rhs) = promote(this, other)
    return (lhs.bits xor rhs.bits).unsigned
}

public infix fun SignedInteger.and(other: SignedInteger): SignedInteger {
    val (lhs, rhs) = promote(this, other)
    return (lhs.bits and rhs.bits).signed
}

public infix fun SignedInteger.or(other: SignedInteger): UnsignedInteger {
    val (lhs, rhs) = promote(this, other)
    return (lhs.bits or rhs.bits).unsigned
}

public infix fun SignedInteger.xor(other: SignedInteger): UnsignedInteger {
    val (lhs, rhs) = promote(this, other)
    return (lhs.bits xor rhs.bits).unsigned
}

public infix fun UnsignedInteger.eq(other: UnsignedInteger): BitVector {
    val (lhs, rhs) = promote(this, other)
    return lhs.bits eq rhs.bits
}

public infix fun UnsignedInteger.ne(other: UnsignedInteger): BitVector {
    val (lhs, rhs) = promote(this, other)
    return lhs.bits ne rhs.bits
}

public infix fun SignedInteger.eq(other: SignedInteger): BitVector {
    val (lhs, rhs) = promote(this, other)
    return lhs.bits eq rhs.bits
}

public infix fun SignedInteger.ne(other: SignedInteger): BitVector {
    val (lhs, rhs) = promote(this, other)
    return lhs.bits ne rhs.bits
}
