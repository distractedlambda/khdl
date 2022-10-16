package org.khdl.dsl

public data class Signed(override val bitWidth: Int) : Integer

public val Signal<Vector<Bit>>.signed: Signal<Signed> get() {
    return bitCastTo(Signed(type.bitWidth))
}

public val Int.signed: Signal<Signed> get() = when (this) {
    0 -> Signal(Signed(0), NilNode)
    else -> {
        val bitWidth = Int.SIZE_BITS - (if (this < 0) inv() else this).countLeadingZeroBits() + 1
        Signal(
            Signed(bitWidth),
            ConstantNode(ByteArray(bitWidth) { (this shr it and 1).toByte() }),
        )
    }
}

public val Long.signed: Signal<Signed> get() = when (this) {
    0L -> Signal(Signed(0), NilNode)
    else -> {
        val bitWidth = Long.SIZE_BITS - (if (this < 0) inv() else this).countLeadingZeroBits() + 1
        Signal(
            Signed(bitWidth),
            ConstantNode(ByteArray(bitWidth) { (this shr it and 1).toByte() }),
        )
    }
}

public val UInt.signed: Signal<Signed> get() = when (this) {
    0u -> Signal(Signed(0), NilNode)
    else -> {
        val bitWidth = UInt.SIZE_BITS - countLeadingZeroBits() + 1
        Signal(
            Signed(bitWidth),
            ConstantNode(ByteArray(bitWidth) { if (it < UInt.SIZE_BITS) (this shr it and 1u).toByte() else 0 }),
        )
    }
}

public val ULong.signed: Signal<Signed> get() = when (this) {
    0uL -> Signal(Signed(0), NilNode)
    else -> {
        val bitWidth = ULong.SIZE_BITS - countLeadingZeroBits() + 1
        Signal(
            Signed(bitWidth),
            ConstantNode(ByteArray(bitWidth) { if (it < ULong.SIZE_BITS) (this shr it and 1u).toByte() else 0 }),
        )
    }
}

public operator fun Signal<Signed>.unaryMinus(): Signal<Signed> {
    return Signal(type, TwosComplementNode(node))
}

private fun binaryOp(lhs: Signal<Signed>, rhs: Signal<Signed>, factory: (Node, Node) -> Node): Signal<Signed> {
    val promotedWidth = maxOf(lhs.type.bitWidth, rhs.type.bitWidth)
    val promotedLhs = if (lhs.type.bitWidth == promotedWidth) lhs.node else SignExtendNode(promotedWidth, lhs.node)
    val promotedRhs = if (rhs.type.bitWidth == promotedWidth) rhs.node else SignExtendNode(promotedWidth, rhs.node)
    return Signal(Signed(promotedWidth), factory(promotedLhs, promotedRhs))
}

public operator fun Signal<Signed>.plus(rhs: Signal<Signed>): Signal<Signed> {
    return binaryOp(this, rhs, ::AddNode)
}

public operator fun Signal<Signed>.minus(rhs: Signal<Signed>): Signal<Signed> {
    return binaryOp(this, rhs) { l, r -> AddNode(l, TwosComplementNode(r)) }
}

public operator fun Signal<Signed>.times(rhs: Signal<Signed>): Signal<Signed> {
    return binaryOp(this, rhs, ::SignedMultiplyNode)
}

public infix fun Signal<Signed>.and(rhs: Signal<Signed>): Signal<Signed> {
    return binaryOp(this, rhs, ::AndNode)
}

public infix fun Signal<Signed>.or(rhs: Signal<Signed>): Signal<Signed> {
    return binaryOp(this, rhs, ::OrNode)
}

public infix fun Signal<Signed>.xor(rhs: Signal<Signed>): Signal<Signed> {
    return binaryOp(this, rhs, ::XorNode)
}

private fun binaryCompare(lhs: Signal<Signed>, rhs: Signal<Signed>, factory: (Node, Node) -> Node): Signal<Bit> {
    val promotedWidth = maxOf(lhs.type.bitWidth, rhs.type.bitWidth)
    val promotedLhs = if (lhs.type.bitWidth == promotedWidth) lhs.node else SignExtendNode(promotedWidth, lhs.node)
    val promotedRhs = if (rhs.type.bitWidth == promotedWidth) rhs.node else SignExtendNode(promotedWidth, rhs.node)
    return Signal(Bit, factory(promotedLhs, promotedRhs))
}

public infix fun Signal<Signed>.eq(rhs: Signal<Signed>): Signal<Bit> {
    return binaryCompare(this, rhs, ::EqNode)
}

public infix fun Signal<Signed>.ne(rhs: Signal<Signed>): Signal<Bit> {
    return (this eq rhs).inv()
}

public infix fun Signal<Signed>.lt(rhs: Signal<Signed>): Signal<Bit> {
    return binaryCompare(this, rhs, ::SignedLtNode)
}

public infix fun Signal<Signed>.le(rhs: Signal<Signed>): Signal<Bit> {
    return (this lt rhs) or (this eq rhs)
}

public infix fun Signal<Signed>.gt(rhs: Signal<Signed>): Signal<Bit> {
    return (this le rhs).inv()
}

public infix fun Signal<Signed>.ge(rhs: Signal<Signed>): Signal<Bit> {
    return (this lt rhs).inv()
}
