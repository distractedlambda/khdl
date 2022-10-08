package org.khdl.components

import org.khdl.Component
import org.khdl.Wire
import org.khdl.WireBundle
import org.khdl.drivenWires

internal class Add(val lhs: WireBundle, val rhs: WireBundle) : Component {
    val result = drivenWires(maxOf(lhs.size, rhs.size))

    override fun visitInputWires(visit: (Wire) -> Unit) {
        lhs.forEach(visit)
        rhs.forEach(visit)
    }
}
