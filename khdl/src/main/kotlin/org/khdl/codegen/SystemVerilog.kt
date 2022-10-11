package org.khdl.codegen

import org.khdl.ir.Add
import org.khdl.ir.And
import org.khdl.ir.BitVector
import org.khdl.ir.Concat
import org.khdl.ir.Constant
import org.khdl.ir.FlipFlop
import org.khdl.ir.Loop
import org.khdl.ir.Module
import org.khdl.ir.ModuleInput
import org.khdl.ir.OnesComplement
import org.khdl.ir.Or
import org.khdl.ir.ReductiveAnd
import org.khdl.ir.ReductiveOr
import org.khdl.ir.ReductiveXor
import org.khdl.ir.Repeat
import org.khdl.ir.Slice
import org.khdl.ir.Xor
import java.util.IdentityHashMap

public fun Module.toSystemVerilog(): String {
    return buildString { toSystemVerilog(this) }
}

public fun Module.toSystemVerilog(output: Appendable) {
    var nextAutoId = 0
    val names = IdentityHashMap<BitVector, String>()
    val toVisit = ArrayDeque<BitVector>()

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
                            Constant.HIGH_IMPEDANCE -> 'Z'
                            else -> error("Invalid constant")
                        })
                    }
                }

                else -> {
                    toVisit.add(node)

                    val name = "${node.javaClass.simpleName.lowercase()}$${nextAutoId++}"

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

    output.append("module $name(")

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

    val visited = IdentityHashMap<BitVector, Unit>()

    while (toVisit.isNotEmpty()) {
        val node = toVisit.removeFirst()

        if (visited.put(node, Unit) != null) {
            continue
        }

        val name = names[node]

        when (node) {
            is Constant, is ModuleInput -> {}

            is Loop -> {
                output.appendLine("always_comb $name = ${getOrAssignName(node.driver)};")
            }

            is FlipFlop -> {
                output.appendLine("always_ff @ (posedge ${getOrAssignName(node.clock)}) $name <= ${getOrAssignName(node.driver)};")
            }

            is Concat -> {
                output.appendLine("always_comb $name = ${node.parts.joinToString(separator = ", ", prefix = "{", postfix = "}") { getOrAssignName(it) }};")
            }

            is Slice -> {
                output.appendLine("always_comb $name = ${getOrAssignName(node.subject)}[${node.msb}:${node.lsb}];")
            }

            is And -> {
                output.appendLine("always_comb $name = ${getOrAssignName(node.lhs)} & ${getOrAssignName(node.rhs)};")
            }

            is Or -> {
                output.appendLine("always_comb $name = ${getOrAssignName(node.lhs)} | ${getOrAssignName(node.rhs)};")
            }

            is Xor -> {
                output.appendLine("always_comb $name = ${getOrAssignName(node.lhs)} ^ ${getOrAssignName(node.rhs)};")
            }

            is ReductiveAnd -> {
                output.appendLine("always_comb $name = &${getOrAssignName(node.operand)};")
            }

            is ReductiveOr -> {
                output.appendLine("always_comb $name = |${getOrAssignName(node.operand)};")
            }

            is ReductiveXor -> {
                output.appendLine("always_comb $name = ^${getOrAssignName(node.operand)};")
            }

            is Repeat -> {
                output.appendLine("always_comb $name = {${node.times}{${getOrAssignName(node.subject)}}};")
            }

            is OnesComplement -> {
                output.appendLine("always_comb $name = ~${getOrAssignName(node.operand)};")
            }

            is Add -> {
                output.appendLine("always_comb $name = ${getOrAssignName(node.lhs)} + ${getOrAssignName(node.rhs)};")
            }
        }

        output.appendLine()
    }

    output.appendLine("endmodule;")
}
