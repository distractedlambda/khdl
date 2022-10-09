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

public fun concat(first: BitVector, vararg rest: BitVector): BitVector {
    TODO()
}

public fun one(): BitVector {
    TODO()
}

public fun zero(): BitVector {
    TODO()
}

public fun dontCare(): BitVector {
    TODO()
}

public fun highImpedance(): BitVector {
    TODO()
}

public fun BitVector.msb(): BitVector {
    TODO()
}

public fun BitVector.lsb(): BitVector {
    TODO()
}

public fun BitVector.truncate(newSize: Int): BitVector {
    TODO()
}

public fun BitVector.zeroExtend(newSize: Int): BitVector {
    TODO()
}

public fun BitVector.signExtend(newSize: Int): BitVector {
    TODO()
}

public fun BitVector.inv(): BitVector {
    TODO()
}

public fun BitVector.all(): BitVector {
    TODO()
}

public fun BitVector.any(): BitVector {
    TODO()
}

public fun BitVector.parity(): BitVector {
    TODO()
}

public infix fun BitVector.and(rhs: BitVector): BitVector {
    TODO()
}

public infix fun BitVector.or(rhs: BitVector): BitVector {
    TODO()
}

public infix fun BitVector.xor(rhs: BitVector): BitVector {
    TODO()
}

public fun BitVector.register(clock: BitVector): BitVector {
    TODO()
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
