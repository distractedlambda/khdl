package org.khdl.ir

public sealed interface BitVector {
    public val width: Int
}

internal data class Constant(val value: ByteArray) : BitVector {
    override val width: Int get() = value.size

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
        const val HIGH_IMPEDENCE: Byte = 3
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

internal data class ModuleInput(val name: String, override val width: Int) : BitVector {
    init {
        require(width > 0)
    }
}

internal data class FlipFlop(val driver: BitVector, val clock: BitVector) : BitVector {
    override val width: Int get() = driver.width

    init {
        require(clock.width == 1)
    }
}
