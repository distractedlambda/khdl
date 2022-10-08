package org.khdl

public class Wire internal constructor(internal val driver: Component) : WireBundle {
    override val size: Int get() = 1

    override fun get(index: Int): Wire {
        if (index != 0) {
            throw IndexOutOfBoundsException(index)
        }

        return this
    }

    override fun get(indices: IntProgression): WireBundle {
        return when {
            indices.isEmpty() -> throw IllegalArgumentException()
            indices.first != 0 -> throw IndexOutOfBoundsException(indices.first)
            indices.last != 0 -> throw IndexOutOfBoundsException(indices.last)
            else -> this
        }
    }

    override fun forEach(block: (Wire) -> Unit) {
        block(this)
    }

    override fun repeat(times: Int): WireBundle {
        return when {
            times <= 0 -> throw IllegalArgumentException()
            times == 1 -> this
            else -> ArrayWireBundle(Array(times) { this })
        }
    }
}
