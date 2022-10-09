package org.khdl.components

import org.khdl.Component
import org.khdl.SystemVerilogOutput
import org.khdl.Wire
import org.khdl.WireBundle
import org.khdl.drivenWires

internal class Register(val dataIn: WireBundle, val clock: Wire) : Component {
    val dataOut = drivenWires(dataIn.size)

    override fun visitInputWires(visit: (Wire) -> Unit) {
        dataIn.forEach(visit)
        visit(clock)
    }

    override fun emitSystemVerilog(output: SystemVerilogOutput) {
        output.appendLine("always_ff @ (posedge ${output.nameWire(clock)}) ${output.nameWireBundle(dataOut)} <= ${output.nameWireBundle(dataIn)};")
    }
}
