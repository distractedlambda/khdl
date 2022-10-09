package org.khdl.components

import org.khdl.Component
import org.khdl.SystemVerilogOutput
import org.khdl.Wire
import org.khdl.WireBundle
import org.khdl.drivenWires

internal class ParallelXor(val lhs: WireBundle, val rhs: WireBundle) : Component {
    val result = drivenWires(lhs.size)

    init {
        require(lhs.size == rhs.size)
    }

    override fun visitInputWires(visit: (Wire) -> Unit) {
        lhs.forEach(visit)
        rhs.forEach(visit)
    }

    override fun emitSystemVerilog(output: SystemVerilogOutput) {
        output.appendLine("always_comb ${output.nameWireBundle(rhs)} = ${output.nameWireBundle(lhs)} ^ ${output.nameWireBundle(rhs)};")
    }
}
