package org.khdl.components

import org.khdl.Component
import org.khdl.SystemVerilogOutput
import org.khdl.Wire
import org.khdl.WireBundle
import org.khdl.drivenWire

internal class ReductiveXor(val operand: WireBundle) : Component {
    val result = drivenWire()

    override fun visitInputWires(visit: (Wire) -> Unit) {
        operand.forEach(visit)
    }

    override fun emitSystemVerilog(output: SystemVerilogOutput) {
        output.appendLine("always_comb ${output.nameWire(result)} = ^${output.nameWireBundle(operand)}")
    }
}
