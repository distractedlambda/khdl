package org.khdl.examples

import org.khdl.codegen.toSystemVerilog
import org.khdl.dsl.Clock
import org.khdl.dsl.buildModule
import org.khdl.examples.riscv.GprReadPort
import org.khdl.examples.riscv.GprWritePort
import org.khdl.examples.riscv.registerFile

fun main() {
    val module = buildModule("regfile") {
        val readPorts = List(2) { GprReadPort() }
        val writePorts = List(2) { GprWritePort() }

        val clock = addInput("clock", Clock)

        readPorts.forEachIndexed { i, port ->
            port.registerIndex.exportInput("read_address_$i")
            port.data.exportOutput("read_data_$i")
        }

        writePorts.forEachIndexed { i, port ->
            port.registerIndex.exportInput("write_address_$i")
            port.writeEnable.exportInput("write_enable_$i")
            port.data.exportInput("write_data_$i")
        }

        registerFile(clock, readPorts, writePorts)
    }

    println(module.toSystemVerilog())
}
