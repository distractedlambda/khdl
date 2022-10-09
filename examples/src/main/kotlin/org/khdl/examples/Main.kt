package org.khdl.examples

import org.khdl.ir.buildModule
import org.khdl.ir.register
import org.khdl.ir.xor

fun main() {
    println(buildModule("top") {
        val clock = addInput("clock")
        val lhs = addInput("lhs", 32)
        val rhs = addInput("rhs", 32)
        val result = lhs xor rhs
        addOutput("result", result.register(clock))
    }.toSystemVerilog())
}
