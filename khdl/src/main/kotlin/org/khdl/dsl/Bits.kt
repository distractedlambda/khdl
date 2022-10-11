package org.khdl.dsl

import org.khdl.ir.Add
import org.khdl.ir.And
import org.khdl.ir.BitVector
import org.khdl.ir.Concat
import org.khdl.ir.Constant
import org.khdl.ir.FlipFlop
import org.khdl.ir.Loop
import org.khdl.ir.Module
import org.khdl.ir.OnesComplement
import org.khdl.ir.Or
import org.khdl.ir.ReductiveAnd
import org.khdl.ir.ReductiveOr
import org.khdl.ir.ReductiveXor
import org.khdl.ir.Repeat
import org.khdl.ir.Slice
import org.khdl.ir.Xor
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

public fun zeros(width: Int): BitVector {
    return zero().repeat(width)
}

public fun one(): BitVector {
    return CONSTANT_ONE
}

public fun ones(width: Int): BitVector {
    return one().repeat(width)
}

public fun dontCare(width: Int = 1): BitVector {
    return CONSTANT_X.repeat(width)
}

public fun highImpedance(width: Int = 1): BitVector {
    return CONSTANT_Z.repeat(width)
}

public fun BitVector.slice(msb: Int, lsb: Int): BitVector {
    return if (msb == width - 1 && lsb == 0) {
        this
    } else {
        Slice(this, msb, lsb)
    }
}

public operator fun BitVector.get(index: Int): BitVector {
    return slice(index, index)
}

public fun BitVector.repeat(times: Int): BitVector {
    return if (times == 1) {
        this
    } else {
        return Repeat(this, times)
    }
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
    return if (newWidth == width) {
        this
    } else {
        concat(zero().repeat(newWidth - width), this)
    }
}

public fun BitVector.signExtend(newWidth: Int): BitVector {
    return if (newWidth == width) {
        this
    } else {
        concat(msb().repeat(newWidth - width), this)
    }
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

public infix fun BitVector.ne(other: BitVector): BitVector {
    return (this xor other).all()
}

public infix fun BitVector.eq(other: BitVector): BitVector {
    return (this ne other).inv()
}

public operator fun BitVector.plus(other: BitVector): BitVector {
    return Add(this, other)
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

public class SelectScope @PublishedApi internal constructor() {
    private var width: Int? = null
    private val clauses = mutableListOf<Clause>()
    private var default: BitVector? = null

    public fun where(condition: BitVector, result: BitVector) {
        require(condition.width == 1)

        if (width == null) {
            width = result.width
        } else {
            require(result.width == width)
        }

        clauses.add(Clause(condition, result))
    }

    @OptIn(ExperimentalContracts::class)
    public inline fun where(condition: BitVector, block: () -> BitVector) {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }

        where(condition, block())
    }

    public fun otherwise(result: BitVector) {
        if (width == null) {
            width = result.width
        } else {
            require(result.width == width)
        }

        default = result
    }

    @OptIn(ExperimentalContracts::class)
    public inline fun otherwise(block: () -> BitVector) {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }

        otherwise(block())
    }

    public fun otherwiseDontCare() {
        default = dontCare(checkNotNull(width))
    }

    public fun otherwiseHighImpedance() {
        default = highImpedance(checkNotNull(width))
    }

    public fun otherwiseZeros() {
        default = zeros(checkNotNull(width))
    }

    public fun otherwiseOnes() {
        default = ones(checkNotNull(width))
    }

    @PublishedApi internal fun build(): BitVector {
        return clauses.foldRight(checkNotNull(default)) { (condition, ifTrue), ifFalse ->
            (condition and ifTrue) or (condition.inv() and ifFalse)
        }
    }

    private data class Clause(val condition: BitVector, val result: BitVector)
}

@OptIn(ExperimentalContracts::class)
public inline fun select(block: SelectScope.() -> Unit): BitVector {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return SelectScope().apply(block).build()
}

public fun Int.bits(width: Int): BitVector {
    require(width >= 33 - (if (this < 0) inv() else this).countLeadingZeroBits())
    return Constant(ByteArray(width) {
        (this shr maxOf(it, 31) and 1).toByte()
    })
}

public fun Long.bits(width: Int): BitVector {
    require(width >= 65 - (if (this < 0) inv() else this).countLeadingZeroBits())
    return Constant(ByteArray(width) {
        (this shr maxOf(it, 63) and 1).toByte()
    })
}

public fun UInt.bits(width: Int): BitVector {
    require(width >= 32 - countLeadingZeroBits())
    return Constant(ByteArray(width) {
        if (it >= 32) 0 else (this shr it and 1u).toByte()
    })
}

public fun ULong.bits(width: Int): BitVector {
    require(width >= 64 - countLeadingZeroBits())
    return Constant(ByteArray(width) {
        if (it >= 64) 0 else (this shr it and 1u).toByte()
    })
}
