package org.khdl.codegen

import org.khdl.ir.BitVector
import org.khdl.ir.Constant
import org.khdl.ir.FlipFlop
import org.khdl.ir.Loop
import org.khdl.ir.Module
import org.khdl.ir.ModuleInput

public fun Module.toSystemVerilog(output: Appendable) {
    var nextAutoId = 0
    val names = hashMapOf<BitVector, String>()
    val toVisit = ArrayDeque<BitVector>()
    val visited = hashSetOf<BitVector>()

    fun getOrAssignName(node: BitVector): String {
        return names.getOrPut(node) {
            when (node) {
                is Constant -> buildString {
                    append("${node.width}'b")
                    node.value.forEach { byte ->
                        append(when (byte) {
                            Constant.ZERO -> '0'
                            Constant.ONE -> '1'
                            Constant.DONT_CARE -> 'X'
                            Constant.HIGH_IMPEDENCE -> 'Z'
                            else -> error("Invalid constant")
                        })
                    }
                }

                is Loop -> {
                    getOrAssignName(node.driver)
                }

                is FlipFlop -> {
                    "${getOrAssignName(node.driver)}\$ff\$${getOrAssignName(node.clock)}"
                }

                else -> {
                    val name = "${node.javaClass.simpleName}$${nextAutoId++}"

                    if (node.width == 1) {
                        output.appendLine("logic $name;")
                    } else {
                        output.appendLine("logic [${node.width - 1}:0] $name;")
                    }

                    name
                }
            }
        }
    }

    val portDecls = mutableListOf<String>()

    inputs.mapTo(portDecls) {
        names[it] = it.name
        visited.add(it)
        if (it.width == 1) {
            "input logic ${it.name}"
        } else {
            "input logic [${it.width - 1}:0] ${it.name}"
        }
    }

    outputs.mapTo(portDecls) { (name, driver) ->
        names[driver] = name
        toVisit.add(driver)
        if (driver.width == 1) {
            "output logic $name"
        } else {
            "output logic [${driver.width - 1}:0] $name"
        }
    }

    output.append("module \\$name(")

    if (portDecls.isNotEmpty()) {
        output.appendLine()

        for (i in 0 until portDecls.size - 1) {
            output.append("    ")
            output.append(portDecls[i])
            output.append(",")
            output.appendLine()
        }

        output.append("    ")
        output.appendLine(portDecls.last())
    }

    output.appendLine(");")
    output.appendLine()

    while (toVisit.isNotEmpty()) {
        val node = toVisit.removeFirst()

        if (!visited.add(node)) {
            continue
        }

        when (node) {
            is Constant, is Loop, is ModuleInput -> {}

            is FlipFlop -> {
                toVisit.add(node.driver)
                toVisit.add(node.clock)

                val driver = getOrAssignName(node.driver)
                val clock = getOrAssignName(node.clock)
                val name = getOrAssignName(node)

                output.appendLine("always_ff @ (posedge $clock) $name <= $driver;")
            }
        }
    }

    output.appendLine("endmodule;")
}
