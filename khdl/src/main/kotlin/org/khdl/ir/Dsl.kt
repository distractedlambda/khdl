package org.khdl.ir

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
public inline fun buildModule(name: String, block: Module.Builder.() -> Unit): Module {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return Module.Builder(name).apply(block).build()
}

public fun concat(first: BitVector, second: BitVector, vararg rest: BitVector): BitVector {
    return Concat(listOf(first, second, *rest))
}

private val CONSTANT_ZERO = Constant(byteArrayOf(Constant.ZERO))
private val CONSTANT_ONE = Constant(byteArrayOf(Constant.ONE))
private val CONSTANT_X = Constant(byteArrayOf(Constant.DONT_CARE))
private val CONSTANT_Z = Constant(byteArrayOf(Constant.HIGH_IMPEDANCE))

public fun zero(): BitVector {
    return CONSTANT_ZERO
}

public fun one(): BitVector {
    return CONSTANT_ONE
}

public fun dontCare(): BitVector {
    return CONSTANT_X
}

public fun highImpedance(): BitVector {
    return CONSTANT_Z
}

public fun BitVector.slice(msb: Int, lsb: Int): BitVector {
    return Slice(this, msb, lsb)
}

public operator fun BitVector.get(index: Int): BitVector {
    return slice(index, index)
}

public fun BitVector.repeat(times: Int): BitVector {
    return Repeat(this, times)
}

public fun BitVector.msb(): BitVector {
    return this[width - 1]
}

public fun BitVector.lsb(): BitVector {
    return this[0]
}

public fun BitVector.truncate(newWidth: Int): BitVector {
    return slice(newWidth - 1, 0)
}

public fun BitVector.zeroExtend(newWidth: Int): BitVector {
    return concat(zero().repeat(newWidth - width), this)
}

public fun BitVector.signExtend(newWidth: Int): BitVector {
    return concat(msb().repeat(newWidth - width), this)
}

public fun BitVector.inv(): BitVector {
    return OnesComplement(this)
}

public fun BitVector.all(): BitVector {
    return ReductiveAnd(this)
}

public fun BitVector.any(): BitVector {
    return ReductiveOr(this)
}

public fun BitVector.parity(): BitVector {
    return ReductiveXor(this)
}

public infix fun BitVector.and(rhs: BitVector): BitVector {
    return And(this, rhs)
}

public infix fun BitVector.or(rhs: BitVector): BitVector {
    return Or(this, rhs)
}

public infix fun BitVector.xor(rhs: BitVector): BitVector {
    return Xor(this, rhs)
}

public fun BitVector.register(clock: BitVector): BitVector {
    return FlipFlop(this, clock)
}

@OptIn(ExperimentalContracts::class)
public fun loop(width: Int, block: (BitVector) -> BitVector): BitVector {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return Loop(width).let { loop ->
        block(loop).also {
            loop.drive(it)
        }
    }
}
