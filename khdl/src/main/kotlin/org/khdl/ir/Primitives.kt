package org.khdl.ir

public sealed interface BitVector {
    public val width: Int
}

internal class Constant(val value: ByteArray) : BitVector {
    override val width get() = value.size

    override fun hashCode(): Int {
        return value.contentHashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is Constant && value.contentEquals(other.value)
    }

    companion object {
        const val ZERO: Byte = 0
        const val ONE: Byte = 1
        const val DONT_CARE: Byte = 2
        const val HIGH_IMPEDANCE: Byte = 3
    }
}

internal class Loop(override val width: Int) : BitVector {
    lateinit var driver: BitVector

    fun drive(driver: BitVector) {
        require(driver.width == width)
        check(!this::driver.isInitialized)
        this.driver = driver
    }
}

internal class ModuleInput(val name: String, override val width: Int) : BitVector {
    init {
        require(width > 0)
    }
}

internal class FlipFlop(val driver: BitVector, val clock: BitVector) : BitVector {
    override val width = driver.width

    init {
        require(clock.width == 1)
    }
}

internal class Concat(val parts: List<BitVector>) : BitVector {
    override val width = parts.sumOf { it.width }
}

internal class Slice(val subject: BitVector, val msb: Int, val lsb: Int) : BitVector {
    override val width = msb - lsb + 1

    init {
        require(msb >= lsb)
        require(msb < subject.width)
        require(lsb >= 0)
    }
}

internal class And(val lhs: BitVector, val rhs: BitVector) : BitVector {
    override val width = lhs.width

    init {
        require(lhs.width == rhs.width)
    }
}

internal class Or(val lhs: BitVector, val rhs: BitVector) : BitVector {
    override val width = lhs.width

    init {
        require(lhs.width == rhs.width)
    }
}

internal class Xor(val lhs: BitVector, val rhs: BitVector) : BitVector {
    override val width = lhs.width

    init {
        require(lhs.width == rhs.width)
    }
}

internal class ReductiveAnd(val operand: BitVector) : BitVector {
    override val width = operand.width
}

internal class ReductiveOr(val operand: BitVector) : BitVector {
    override val width = operand.width
}

internal class ReductiveXor(val operand: BitVector) : BitVector {
    override val width = operand.width
}

internal class Repeat(val subject: BitVector, val times: Int) : BitVector {
    init {
        require(times > 0)
    }

    override val width = Math.multiplyExact(subject.width, times)
}

internal class OnesComplement(val operand: BitVector) : BitVector {
    override val width = operand.width
}

internal class Add(val lhs: BitVector, val rhs: BitVector) : BitVector {
    init {
        require(lhs.width == rhs.width)
    }

    override val width = lhs.width + 1
}
