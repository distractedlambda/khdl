package org.khdl.examples

import org.khdl.codegen.toSystemVerilog
import org.khdl.dsl.buildModule
import org.khdl.dsl.register
import org.khdl.dsl.xor

fun main() {
    println(buildModule("top") {
        val clock = addInput("clock")
        val lhs = addInput("lhs", 32)
        val rhs = addInput("rhs", 32)
        val result = lhs xor rhs
        addOutput("result", result.register(clock))
    }.toSystemVerilog())
}
