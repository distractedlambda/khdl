package org.khdl.components

import org.khdl.Component
import org.khdl.SystemVerilogOutput
import org.khdl.Wire
import org.khdl.WireBundle
import org.khdl.drivenWires

internal class ParallelNot(val operand: WireBundle) : Component {
    val result = drivenWires(operand.size)

    override fun visitInputWires(visit: (Wire) -> Unit) {
        operand.forEach(visit)
    }

    override fun emitSystemVerilog(output: SystemVerilogOutput) {
        output.appendLine("always_comb ${output.nameWireBundle(result)} = ~${output.nameWireBundle(operand)};")
    }
}
