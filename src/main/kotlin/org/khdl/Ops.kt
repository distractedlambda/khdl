package org.khdl

import org.khdl.components.Add
import org.khdl.components.Constant0
import org.khdl.components.Constant1
import org.khdl.components.ConstantX
import org.khdl.components.ConstantZ
import org.khdl.components.Loop
import org.khdl.components.ReductiveAnd
import org.khdl.components.ParallelAnd
import org.khdl.components.ParallelNot
import org.khdl.components.ParallelOr
import org.khdl.components.ParallelXor
import org.khdl.components.ReductiveOr
import org.khdl.components.ReductiveXor
import org.khdl.components.Register
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public fun concat(first: WireBundle, vararg rest: WireBundle): WireBundle {
    val wires = mutableListOf<Wire>()

    for (i in rest.indices.reversed()) {
        when (val part = rest[i]) {
            is Wire -> wires.add(part)
            is ArrayWireBundle -> wires.addAll(part.wires)
        }
    }

    when (first) {
        is Wire -> wires.add(first)
        is ArrayWireBundle -> wires.addAll(first.wires)
    }

    return wires.singleOrNull() ?: ArrayWireBundle(wires.toTypedArray())
}

public fun one(): Wire {
    return Constant1.output
}

public fun zero(): Wire {
    return Constant0.output
}

public fun dontCare(): Wire {
    return ConstantX.output
}

public fun highImpedance(): Wire {
    return ConstantZ.output
}

public fun WireBundle.msb(): Wire {
    return this[size - 1]
}

public fun WireBundle.lsb(): Wire {
    return this[0]
}

public fun WireBundle.truncate(newSize: Int): WireBundle {
    require(newSize in 1..size)
    return this[newSize - 1 downTo 0]
}

public fun WireBundle.zeroExtend(newSize: Int): WireBundle {
    require(newSize >= size)
    return if (newSize == size) {
        this
    } else {
        concat(zero().repeat(newSize - size), this)
    }
}

public fun WireBundle.signExtend(newSize: Int): WireBundle {
    require(newSize >= size)
    return if (newSize == size) {
        this
    } else {
        concat(msb().repeat(newSize - size), this)
    }
}

public fun WireBundle.inv(): WireBundle {
    return ParallelNot(this).output
}

public fun WireBundle.all(): Wire {
    return ReductiveAnd(this).result
}

public fun WireBundle.any(): Wire {
    return ReductiveOr(this).result
}

public fun WireBundle.parity(): Wire {
    return ReductiveXor(this).result
}

public infix fun WireBundle.and(rhs: WireBundle): WireBundle {
    return ParallelAnd(this, rhs).result
}

public infix fun WireBundle.or(rhs: WireBundle): WireBundle {
    return ParallelOr(this, rhs).result
}

public infix fun WireBundle.xor(rhs: WireBundle): WireBundle {
    return ParallelXor(this, rhs).result
}

public operator fun WireBundle.unaryMinus(): WireBundle {
    return Add(inv(), one()).result
}

public fun WireBundle.register(clock: Wire): WireBundle {
    return Register(this, clock).registeredData
}

@OptIn(ExperimentalContracts::class)
public fun loop(width: Int, block: (WireBundle) -> WireBundle): WireBundle {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return Loop(width).run {
        block(backEdge).also {
            connect(it)
        }
    }
}
