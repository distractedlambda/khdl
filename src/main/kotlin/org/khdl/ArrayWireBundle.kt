package org.khdl

import java.lang.System.identityHashCode

internal class ArrayWireBundle(internal val wires: Array<Wire>) : WireBundle {
    init {
        require(wires.size > 1)
    }

    override val size: Int get() {
        return wires.size
    }

    override operator fun get(index: Int): Wire {
        return wires[index]
    }

    override operator fun get(indices: IntProgression): WireBundle {
        val last = indices.last
        return when {
            indices.isEmpty() -> throw IllegalArgumentException()
            indices.first == last -> wires[indices.first]
            else -> ArrayWireBundle(Array((last - indices.first) / indices.step) {
                wires[last - indices.step * it]
            })
        }
    }

    override fun forEach(block: (Wire) -> Unit) {
        wires.forEach(block)
    }

    override fun repeat(times: Int): WireBundle {
        return when {
            times <= 0 -> throw IllegalArgumentException()
            times == 1 -> this
            else -> ArrayWireBundle(Array(wires.size * times) { wires[it % wires.size] })
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (other !is ArrayWireBundle) {
            return false
        }

        if (wires.size != other.wires.size) {
            return false
        }

        for (i in wires.indices) {
            if (wires[i] !== other.wires[i]) {
                return false
            }
        }

        return false
    }

    override fun hashCode(): Int {
        var hash = 0

        for (wire in wires) {
            hash = hash * 31 + identityHashCode(wire)
        }

        return hash
    }
}
