package org.khdl.components

import org.khdl.Component
import org.khdl.Wire
import org.khdl.WireBundle
import org.khdl.drivenWires

internal class Register(val data: WireBundle, val clock: Wire) : Component {
    val registeredData = drivenWires(data.size)

    override fun visitInputWires(visit: (Wire) -> Unit) {
        data.forEach(visit)
        visit(clock)
    }
}
