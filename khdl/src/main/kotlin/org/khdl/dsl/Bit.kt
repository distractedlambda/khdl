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

private val CONSTANT_ZERO = ConstantNode(byteArrayOf(ConstantNode.ZERO))
private val CONSTANT_ONE = ConstantNode(byteArrayOf(ConstantNode.ONE))
private val CONSTANT_X = ConstantNode(byteArrayOf(ConstantNode.DONT_CARE))
private val CONSTANT_Z = ConstantNode(byteArrayOf(ConstantNode.HIGH_IMPEDANCE))
