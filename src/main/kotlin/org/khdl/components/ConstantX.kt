package org.khdl.components

import org.khdl.Component
import org.khdl.Wire
import org.khdl.drivenWire

internal object ConstantX : Component {
    val output = drivenWire()

    override fun visitInputWires(visit: (Wire) -> Unit) = Unit
}
