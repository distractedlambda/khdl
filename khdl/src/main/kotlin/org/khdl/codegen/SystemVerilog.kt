package org.khdl.codegen

import org.khdl.dsl.AddNode
import org.khdl.dsl.AndNode
import org.khdl.dsl.ConcatNode
import org.khdl.dsl.ConditionalNode
import org.khdl.dsl.ConstantNode
import org.khdl.dsl.EqNode
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
import org.khdl.dsl.SignExtendNode
import org.khdl.dsl.SignedLtNode
import org.khdl.dsl.SignedMultiplyNode
import org.khdl.dsl.SliceNode
import org.khdl.dsl.TwosComplementNode
import org.khdl.dsl.UnsignedLtNode
import org.khdl.dsl.UnsignedMultiplyNode
import org.khdl.dsl.WireNode
import org.khdl.dsl.XorNode
import org.khdl.dsl.ZeroExtendNode
import java.util.IdentityHashMap

public fun Module.toSystemVerilog(): String {
    return buildString { toSystemVerilog(this) }
}

public fun Module.toSystemVerilog(output: Appendable) {
    var nextAutoId = 0
    val names = IdentityHashMap<Node, String>()
    val toVisit = ArrayDeque<Node>()

    fun getOrAssignName(node: Node): String {
        require(node.width != 0)
        return names.getOrPut(node) {
            when (node) {
                is ConstantNode -> buildString {
                    append("${node.width}'b")
                    for (i in node.value.indices.reversed()) {
                        append(when (node.value[i]) {
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

    inputs.mapNotNullTo(portDecls) {
        if (it.width != 0) {
            names[it] = it.name
            if (it.width == 1) {
                "input logic ${it.name}"
            } else {
                "input logic [${it.width - 1}:0] ${it.name}"
            }
        } else {
            null
        }
    }

    outputs.mapNotNullTo(portDecls) { (name, driver) ->
        if (driver.width != 0) {
            names[driver] = name
            toVisit.add(driver)
            if (driver.width == 1) {
                "output logic $name"
            } else {
                "output logic [${driver.width - 1}:0] $name"
            }
        } else {
            null
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
                val partNames = mutableListOf<String>()

                for (i in node.parts.indices.reversed()) {
                    if (node.parts[i].width != 0) {
                        partNames.add(getOrAssignName(node.parts[i]))
                    }
                }

                output.appendLine("assign $name = ${partNames.joinToString(separator = ", ", prefix = "{", postfix = "}")};")
            }

            is SliceNode -> {
                val range = if (node.width > 1) {
                    "${node.start + node.width - 1}:${node.start}"
                } else {
                    node.start.toString()
                }

                output.appendLine("assign $name = ${getOrAssignName(node.subject)}[$range];")
            }

            is ReductiveAndNode -> {
                val rhs = if (node.operand.width != 0) {
                    "&${getOrAssignName(node.operand)}"
                } else {
                    "'1"
                }

                output.appendLine("assign $name = $rhs;")
            }

            is ReductiveOrNode -> {
                val rhs = if (node.operand.width != 0) {
                    "|${getOrAssignName(node.operand)}"
                } else {
                    "'0"
                }

                output.appendLine("assign $name = $rhs;")
            }

            is ReductiveXorNode -> {
                val rhs = if (node.operand.width != 0) {
                    "^${getOrAssignName(node.operand)}"
                } else {
                    "'0"
                }

                output.appendLine("assign $name = $rhs;")
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

            is AddNode -> {
                output.appendLine("assign $name = ${getOrAssignName(node.lhs)} + ${getOrAssignName(node.rhs)};")
            }

            is ZeroExtendNode -> {
                val rhs = if (node.operand.width != 0) {
                    getOrAssignName(node.operand)
                } else {
                    "'0"
                }

                output.appendLine("assign $name = $rhs;")
            }

            is SignExtendNode -> {
                val rhs = if (node.operand.width != 0) {
                    "\$signed(${getOrAssignName(node.operand)})"
                } else {
                    "'0"
                }

                output.appendLine("assign $name = $rhs;")
            }

            is EqNode -> {
                val rhs = if (node.lhs.width != 0) {
                    "${getOrAssignName(node.lhs)} == ${getOrAssignName(node.rhs)}"
                } else {
                    "'1"
                }

                output.appendLine("assign $name = $rhs;")
            }

            is UnsignedLtNode -> {
                val rhs = if (node.lhs.width != 0) {
                    "${getOrAssignName(node.lhs)} < ${getOrAssignName(node.rhs)}"
                } else {
                    "'0"
                }

                output.appendLine("assign $name = $rhs;")
            }

            is SignedLtNode -> {
                val rhs = if (node.lhs.width != 0) {
                    "\$signed(${getOrAssignName(node.lhs)}) < \$signed(${getOrAssignName(node.rhs)})"
                } else {
                    "'0"
                }

                output.appendLine("assign $name = $rhs;")
            }

            is UnsignedMultiplyNode -> {
                output.appendLine("assign $name = ${getOrAssignName(node.lhs)} * ${getOrAssignName(node.rhs)};")
            }

            is SignedMultiplyNode -> {
                output.appendLine("assign $name = \$signed(${getOrAssignName(node.lhs)}) * \$signed(${getOrAssignName(node.rhs)});")
            }

            is RepeatNode -> {
                output.appendLine("assign $name = {${node.times}{${getOrAssignName(node.subject)}}};")
            }

            is OnesComplementNode -> {
                output.appendLine("assign $name = ~${getOrAssignName(node.operand)};")
            }

            is TwosComplementNode -> {
                output.appendLine("assign $name = -${getOrAssignName(node.operand)};")
            }

            is ConditionalNode -> {
                output.appendLine("assign $name = ${getOrAssignName(node.condition)} ? ${getOrAssignName(node.ifTrue)} : ${getOrAssignName(node.ifFalse)};")
            }
        }

        output.appendLine()
    }

    output.appendLine("endmodule;")
}
