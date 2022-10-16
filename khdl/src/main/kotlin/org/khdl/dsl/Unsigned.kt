package org.khdl.dsl

public data class Unsigned(override val bitWidth: Int) : Integer

public val Signal<Vector<Bit>>.unsigned: Signal<Unsigned> get() {
    return bitCastTo(Unsigned(type.bitWidth))
}

public val Int.unsigned: Signal<Unsigned> get() = when (this) {
    0 -> Signal(Unsigned(0), NilNode)

    in 1..Int.MAX_VALUE -> {
        val bitWidth = Int.SIZE_BITS - countLeadingZeroBits()
        Signal(
            Unsigned(bitWidth),
            ConstantNode(ByteArray(bitWidth) { (this shr it and 1).toByte() }),
        )
    }

    else -> throw IllegalArgumentException()
}

public val Long.unsigned: Signal<Unsigned> get() = when (this) {
    0L -> Signal(Unsigned(0), NilNode)

    in 1..Long.MAX_VALUE -> {
        val bitWidth = Long.SIZE_BITS - countLeadingZeroBits()
        Signal(
            Unsigned(bitWidth),
            ConstantNode(ByteArray(bitWidth) { (this shr it and 1).toByte() }),
        )
    }

    else -> throw IllegalArgumentException()
}

public val UInt.unsigned: Signal<Unsigned> get() = when (this) {
    0u -> Signal(Unsigned(0), NilNode)

    else -> {
        val bitWidth = UInt.SIZE_BITS - countLeadingZeroBits()
        Signal(
            Unsigned(bitWidth),
            ConstantNode(ByteArray(bitWidth) { (this shr it and 1u).toByte() }),
        )
    }
}

public val ULong.unsigned: Signal<Unsigned> get() = when (this) {
    0uL -> Signal(Unsigned(0), NilNode)

    else -> {
        val bitWidth = ULong.SIZE_BITS - countLeadingZeroBits()
        Signal(
            Unsigned(bitWidth),
            ConstantNode(ByteArray(bitWidth) { (this shr it and 1u).toByte() }),
        )
    }
}

private fun binaryOp(lhs: Signal<Unsigned>, rhs: Signal<Unsigned>, factory: (Node, Node) -> Node): Signal<Unsigned> {
    val promotedWidth = maxOf(lhs.type.bitWidth, rhs.type.bitWidth)
    val promotedLhs = if (lhs.type.bitWidth == promotedWidth) lhs.node else ZeroExtendNode(promotedWidth, lhs.node)
    val promotedRhs = if (rhs.type.bitWidth == promotedWidth) rhs.node else ZeroExtendNode(promotedWidth, rhs.node)
    return Signal(Unsigned(promotedWidth), factory(promotedLhs, promotedRhs))
}

public operator fun Signal<Unsigned>.plus(rhs: Signal<Unsigned>): Signal<Unsigned> {
    return binaryOp(this, rhs, ::AddNode)
}

public operator fun Signal<Unsigned>.minus(rhs: Signal<Unsigned>): Signal<Unsigned> {
    return binaryOp(this, rhs) { l, r -> AddNode(l, TwosComplementNode(r)) }
}

public operator fun Signal<Unsigned>.times(rhs: Signal<Unsigned>): Signal<Unsigned> {
    return binaryOp(this, rhs, ::UnsignedMultiplyNode)
}

public infix fun Signal<Unsigned>.and(rhs: Signal<Unsigned>): Signal<Unsigned> {
    return binaryOp(this, rhs, ::AndNode)
}

public infix fun Signal<Unsigned>.or(rhs: Signal<Unsigned>): Signal<Unsigned> {
    return binaryOp(this, rhs, ::OrNode)
}

public infix fun Signal<Unsigned>.xor(rhs: Signal<Unsigned>): Signal<Unsigned> {
    return binaryOp(this, rhs, ::XorNode)
}

private fun binaryCompare(lhs: Signal<Unsigned>, rhs: Signal<Unsigned>, factory: (Node, Node) -> Node): Signal<Bit> {
    val promotedWidth = maxOf(lhs.type.bitWidth, rhs.type.bitWidth)
    val promotedLhs = if (lhs.type.bitWidth == promotedWidth) lhs.node else ZeroExtendNode(promotedWidth, lhs.node)
    val promotedRhs = if (rhs.type.bitWidth == promotedWidth) rhs.node else ZeroExtendNode(promotedWidth, rhs.node)
    return Signal(Bit, factory(promotedLhs, promotedRhs))
}

public infix fun Signal<Unsigned>.eq(rhs: Signal<Unsigned>): Signal<Bit> {
    return binaryCompare(this, rhs, ::EqNode)
}

public infix fun Signal<Unsigned>.ne(rhs: Signal<Unsigned>): Signal<Bit> {
    return (this eq rhs).inv()
}

public infix fun Signal<Unsigned>.lt(rhs: Signal<Unsigned>): Signal<Bit> {
    return binaryCompare(this, rhs, ::UnsignedLtNode)
}

public infix fun Signal<Unsigned>.le(rhs: Signal<Unsigned>): Signal<Bit> {
    return (this lt rhs) or (this eq rhs)
}

public infix fun Signal<Unsigned>.gt(rhs: Signal<Unsigned>): Signal<Bit> {
    return (this le rhs).inv()
}

public infix fun Signal<Unsigned>.ge(rhs: Signal<Unsigned>): Signal<Bit> {
    return (this lt rhs).inv()
}
