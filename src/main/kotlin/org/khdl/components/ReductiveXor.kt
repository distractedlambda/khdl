package org.khdl.components

import org.khdl.Component
import org.khdl.Wire
import org.khdl.WireBundle
import org.khdl.drivenWire

internal class ReductiveXor(val inputs: WireBundle) : Component {
    val result = drivenWire()

    override fun visitInputWires(visit: (Wire) -> Unit) {
        inputs.forEach(visit)
    }
}
