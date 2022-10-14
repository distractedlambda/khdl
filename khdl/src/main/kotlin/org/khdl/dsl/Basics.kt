package org.khdl.dsl

import org.khdl.dsl.Register
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

public fun Signal.msb(): Signal {
    return this[width - 1]
}

public fun Signal.lsb(): Signal {
    return this[0]
}

public fun Signal.truncate(newWidth: Int): Signal {
    return slice(newWidth - 1, 0)
}

public fun Signal.zeroExtend(newWidth: Int): Signal {
    return if (newWidth == width) {
        this
    } else {
        concat(zero().repeat(newWidth - width), this)
    }
}

public fun Signal.signExtend(newWidth: Int): Signal {
    return if (newWidth == width) {
        this
    } else {
        concat(msb().repeat(newWidth - width), this)
    }
}

public fun Signal.inv(): Signal {
    return OnesComplement(this)
}

public fun Signal.all(): Signal {
    return ReductiveAnd(this)
}

public fun Signal.any(): Signal {
    return ReductiveOr(this)
}

public fun Signal.parity(): Signal {
    return ReductiveXor(this)
}

public infix fun Signal.and(rhs: Signal): Signal {
    return And(this, rhs)
}

public infix fun Signal.or(rhs: Signal): Signal {
    return Or(this, rhs)
}

public infix fun Signal.xor(rhs: Signal): Signal {
    return Xor(this, rhs)
}

public fun Signal.register(clock: Signal): Signal {
    return Register(clock, width).apply { connectInput(this) }
}

public fun register(clock: Signal, width: Int): Register {
    return Register(clock, width)
}

public fun wire(width: Int): Wire {
    return Wire(width)
}

public infix fun Signal.ne(other: Signal): Signal {
    return (this xor other).all()
}

public infix fun Signal.eq(other: Signal): Signal {
    return (this ne other).inv()
}

public operator fun Signal.plus(other: Signal): Signal {
    return Add(this, other)
}

public class SelectScope @PublishedApi internal constructor() {
    private var width: Int? = null
    private val clauses = mutableListOf<Clause>()
    private var default: Signal? = null

    public fun where(condition: Signal, result: Signal) {
        require(condition.width == 1)

        if (width == null) {
            width = result.width
        } else {
            require(result.width == width)
        }

        clauses.add(Clause(condition, result))
    }

    @OptIn(ExperimentalContracts::class)
    public inline fun where(condition: Signal, block: () -> Signal) {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }

        where(condition, block())
    }

    public fun otherwise(result: Signal) {
        if (width == null) {
            width = result.width
        } else {
            require(result.width == width)
        }

        default = result
    }

    @OptIn(ExperimentalContracts::class)
    public inline fun otherwise(block: () -> Signal) {
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

    @PublishedApi internal fun build(): Signal {
        return clauses.foldRight(checkNotNull(default)) { (condition, ifTrue), ifFalse ->
            (condition and ifTrue) or (condition.inv() and ifFalse)
        }
    }

    private data class Clause(val condition: Signal, val result: Signal)
}

@OptIn(ExperimentalContracts::class)
public inline fun select(block: SelectScope.() -> Unit): Signal {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return SelectScope().apply(block).build()
}

public fun Int.bits(width: Int): Signal {
    require(width >= 33 - (if (this < 0) inv() else this).countLeadingZeroBits())
    return Constant(ByteArray(width) {
        (this shr maxOf(it, 31) and 1).toByte()
    })
}

public fun Long.bits(width: Int): Signal {
    require(width >= 65 - (if (this < 0) inv() else this).countLeadingZeroBits())
    return Constant(ByteArray(width) {
        (this shr maxOf(it, 63) and 1).toByte()
    })
}

public fun UInt.bits(width: Int): Signal {
    require(width >= 32 - countLeadingZeroBits())
    return Constant(ByteArray(width) {
        if (it >= 32) 0 else (this shr it and 1u).toByte()
    })
}

public fun ULong.bits(width: Int): Signal {
    require(width >= 64 - countLeadingZeroBits())
    return Constant(ByteArray(width) {
        if (it >= 64) 0 else (this shr it and 1u).toByte()
    })
}
