package org.khdl.codegen

import org.khdl.dsl.AddNode
import org.khdl.dsl.AndNode
import org.khdl.dsl.ConcatNode
import org.khdl.dsl.ConstantNode
import org.khdl.dsl.Module
import org.khdl.dsl.ModuleInputNode
import org.khdl.dsl.NilNode
import org.khdl.dsl.Node
import org.khdl.dsl.OnesComplementNode
import org.khdl.dsl.OrNode
import org.khdl.dsl.ReductiveAndNode
import org.khdl.dsl.ReductiveOrNode
import org.khdl.dsl.ReductiveXorNode
import org.khdl.dsl.RegisterNode
import org.khdl.dsl.RepeatNode
import org.khdl.dsl.SliceNode
import org.khdl.dsl.WireNode
import org.khdl.dsl.XorNode
import java.util.IdentityHashMap

public fun Module.toSystemVerilog(): String {
    return buildString { toSystemVerilog(this) }
}

public fun Module.toSystemVerilog(output: Appendable) {
    var nextAutoId = 0
    val names = IdentityHashMap<Node, String>()
    val toVisit = ArrayDeque<Node>()

    fun getOrAssignName(node: Node): String {
        return names.getOrPut(node) {
            when (node) {
                is ConstantNode -> buildString {
                    append("${node.width}'b")
                    node.value.forEach { byte ->
                        append(when (byte) {
                            ConstantNode.ZERO -> '0'
                            ConstantNode.ONE -> '1'
                            ConstantNode.DONT_CARE -> 'X'
                            ConstantNode.HIGH_IMPEDANCE -> 'Z'
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

    val visited = IdentityHashMap<Node, Unit>()

    while (toVisit.isNotEmpty()) {
        val node = toVisit.removeFirst()

        if (visited.put(node, Unit) != null) {
            continue
        }

        val name = names[node]

        when (node) {
            is ConstantNode, is ModuleInputNode, is NilNode -> {}

            is WireNode -> {
                output.appendLine("assign $name = ${getOrAssignName(node.driver)};")
            }

            is RegisterNode -> {
                output.appendLine("always_ff @ (posedge ${getOrAssignName(node.clock)}) $name <= ${getOrAssignName(node.input)};")
            }

            is ConcatNode -> {
                output.appendLine("assign $name = ${node.parts.joinToString(separator = ", ", prefix = "{", postfix = "}") { getOrAssignName(it) }};")
            }

            is SliceNode -> {
                output.appendLine("assign $name = ${getOrAssignName(node.subject)}[${node.msb}:${node.lsb}];")
            }

            is AndNode -> {
                output.appendLine("assign $name = ${getOrAssignName(node.lhs)} & ${getOrAssignName(node.rhs)};")
            }

            is OrNode -> {
                output.appendLine("assign $name = ${getOrAssignName(node.lhs)} | ${getOrAssignName(node.rhs)};")
            }

            is XorNode -> {
                output.appendLine("assign $name = ${getOrAssignName(node.lhs)} ^ ${getOrAssignName(node.rhs)};")
            }

            is ReductiveAndNode -> {
                output.appendLine("assign $name = &${getOrAssignName(node.operand)};")
            }

            is ReductiveOrNode -> {
                output.appendLine("assign $name = |${getOrAssignName(node.operand)};")
            }

            is ReductiveXorNode -> {
                output.appendLine("assign $name = ^${getOrAssignName(node.operand)};")
            }

            is RepeatNode -> {
                output.appendLine("assign $name = {${node.times}{${getOrAssignName(node.subject)}}};")
            }

            is OnesComplementNode -> {
                output.appendLine("assign $name = ~${getOrAssignName(node.operand)};")
            }

            is AddNode -> {
                output.appendLine("assign $name = ${getOrAssignName(node.lhs)} + ${getOrAssignName(node.rhs)};")
            }
        }

        output.appendLine()
    }

    output.appendLine("endmodule;")
}
