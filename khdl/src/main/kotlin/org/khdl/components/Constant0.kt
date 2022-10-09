package org.khdl.components

import org.khdl.Component
import org.khdl.SystemVerilogOutput
import org.khdl.Wire
import org.khdl.drivenWire

internal object Constant0 : Component {
    val wire = drivenWire()

    override fun visitInputWires(visit: (Wire) -> Unit) = Unit

    override fun emitSystemVerilog(output: SystemVerilogOutput) {
        output.appendLine("assign ${output.nameWire(wire)} = '0;")
    }
}
