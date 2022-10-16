package org.khdl.examples.riscv

import org.khdl.dsl.Bit
import org.khdl.dsl.Clock
import org.khdl.dsl.Register
import org.khdl.dsl.Signal
import org.khdl.dsl.Unsigned
import org.khdl.dsl.Vector
import org.khdl.dsl.Wire

data class GprReadPort(
    val registerIndex: Wire<Unsigned>,
    val data: Wire<Vector<Bit>>,
    val dataValid: Wire<Bit>,
)

data class GprLockPort(
    val registerIndex: Wire<Unsigned>,
    val lockEnable: Wire<Bit>,
)

data class GprWritePort(
    val registerIndex: Wire<Unsigned>,
    val data: Wire<Vector<Bit>>,
    val writeEnable: Wire<Bit>,
    val writeAccepted: Wire<Bit>,
)

fun registerFile(
    clock: Signal<Clock>,
    readPorts: List<GprReadPort>,
    lockPorts: List<GprLockPort>,
    writePorts: List<GprWritePort>,
) {
    val gprs = Register(Vector(Vector(Bit, 32), 31), clock)
}
