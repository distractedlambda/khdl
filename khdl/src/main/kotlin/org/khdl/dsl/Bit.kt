package org.khdl.dsl

public object Bit : Type {
    override val bitWidth: Int get() = 1
}

public fun zero(): Signal<Bit> {
    return Signal(Bit, CONSTANT_ZERO)
}

public fun one(): Signal<Bit> {
    return Signal(Bit, CONSTANT_ONE)
}

public fun dontCare(): Signal<Bit> {
    return Signal(Bit, CONSTANT_X)
}

public fun highImpedance(): Signal<Bit> {
    return Signal(Bit, CONSTANT_Z)
}

public infix fun Signal<Bit>.and(rhs: Signal<Bit>): Signal<Bit> {
    return Signal(Bit, AndNode(node, rhs.node))
}

public infix fun Signal<Bit>.or(rhs: Signal<Bit>): Signal<Bit> {
    return Signal(Bit, OrNode(node, rhs.node))
}

public infix fun Signal<Bit>.xor(rhs: Signal<Bit>): Signal<Bit> {
    return Signal(Bit, XorNode(node, rhs.node))
}

public fun Signal<Bit>.inv(): Signal<Bit> {
    return Signal(Bit, OnesComplementNode(node))
}

private val CONSTANT_ZERO = ConstantNode(byteArrayOf(ConstantNode.ZERO))
private val CONSTANT_ONE = ConstantNode(byteArrayOf(ConstantNode.ONE))
private val CONSTANT_X = ConstantNode(byteArrayOf(ConstantNode.DONT_CARE))
private val CONSTANT_Z = ConstantNode(byteArrayOf(ConstantNode.HIGH_IMPEDANCE))
