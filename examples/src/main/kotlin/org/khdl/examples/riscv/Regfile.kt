package org.khdl.examples.riscv

import org.khdl.dsl.Bit
import org.khdl.dsl.Clock
import org.khdl.dsl.Input
import org.khdl.dsl.Output
import org.khdl.dsl.Register
import org.khdl.dsl.Signal
import org.khdl.dsl.Unsigned
import org.khdl.dsl.Vector
import org.khdl.dsl.Wire
import org.khdl.dsl.and
import org.khdl.dsl.dontCare
import org.khdl.dsl.eq
import org.khdl.dsl.select
import org.khdl.dsl.unsigned
import org.khdl.dsl.zeros

interface GprReadRequester {
    val registerIndex: Output<Unsigned>
    val data: Input<Vector<Bit>>
}

interface GprReadResponder {
    val registerIndex: Input<Unsigned>
    val data: Output<Vector<Bit>>
}

data class GprReadPort(
    override val registerIndex: Wire<Unsigned> = Wire(Unsigned(5)),
    override val data: Wire<Vector<Bit>> = Wire(Vector(Bit, 32)),
) : GprReadRequester, GprReadResponder

interface GprWriteRequester {
    val registerIndex: Output<Unsigned>
    val data: Output<Vector<Bit>>
    val writeEnable: Output<Bit>
}

interface GprWriteResponder {
    val registerIndex: Input<Unsigned>
    val data: Input<Vector<Bit>>
    val writeEnable: Input<Bit>
}

data class GprWritePort(
    override val registerIndex: Wire<Unsigned> = Wire(Unsigned(5)),
    override val data: Wire<Vector<Bit>> = Wire(Vector(Bit, 32)),
    override val writeEnable: Wire<Bit> = Wire(Bit),
) : GprWriteRequester, GprWriteResponder

fun registerFile(
    clock: Signal<Clock>,
    readPorts: List<GprReadResponder>,
    writePorts: List<GprWriteResponder>,
) {
    val gprs = List(31) { Register(Vector(Bit, 32), clock) }

    readPorts.forEach { port ->
        val registerIndex = port.registerIndex.signal

        port.data.connectDriver(select {
            where(registerIndex eq 0.unsigned, zeros(32))

            writePorts.forEach {
                where(registerIndex eq it.registerIndex.signal and it.writeEnable.signal, it.data.signal)
            }

            for (i in 1..31) {
                where(registerIndex eq i.unsigned, gprs[i - 1].signal)
            }

            otherwise(dontCare(32))
        })
    }

    for (i in 1..31) {
        gprs[i - 1].connectInput(select {
            writePorts.forEach { writePort ->
                where(
                    writePort.registerIndex.signal eq i.unsigned and writePort.writeEnable.signal,
                    writePort.data.signal,
                )
            }

            otherwise(gprs[i - 1].signal)
        })
    }
}
