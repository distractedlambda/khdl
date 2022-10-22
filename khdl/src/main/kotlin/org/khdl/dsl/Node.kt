package org.khdl.dsl

import java.lang.Math.addExact
import java.lang.Math.multiplyExact

internal sealed class Node(internal val width: Int) {
    init {
        require(width >= 0)
    }

    final override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    final override fun hashCode(): Int {
        return super.hashCode()
    }
}

internal object NilNode : Node(0)

internal class ConstantNode(val value: ByteArray) : Node(value.size) {
    internal companion object {
        const val ZERO: Byte = 0
        const val ONE: Byte = 1
        const val DONT_CARE: Byte = 2
        const val HIGH_IMPEDANCE: Byte = 3
    }
}

internal class WireNode(width: Int) : Node(width) {
    var driver: Node? = null
}

internal class RegisterNode(val input: Node, val clock: Node) : Node(input.width)

internal class ModuleInputNode(val name: String, width: Int) : Node(width)

internal class ConcatNode(val parts: Array<Node>) : Node(parts.fold(0) { acc, node -> addExact(acc, node.width) })

internal class RepeatNode(val subject: Node, val times: Int) : Node(multiplyExact(subject.width, times))

internal class SliceNode(val subject: Node, val start: Int, width: Int) : Node(width)

internal class ZeroExtendNode(width: Int, val operand: Node) : Node(width)

internal class SignExtendNode(width: Int, val operand: Node) : Node(width)

internal enum class UnaryOp {
    ReductiveAnd,
    ReductiveOr,
    ReductiveXor,
    OnesComplement,
    TwosComplement,
}

internal class UnaryOpNode(width: Int, val op: UnaryOp, val operand: Node) : Node(width)

internal enum class BinaryOp {
    And,
    Or,
    Xor,
    Add,
    Sub,
    Mul,
    Eq,
    Ne,
    Lt,
    Gt,
    Le,
    Ge,
}

internal class BinaryOpNode(
    width: Int,
    val op: BinaryOp,
    val signed: Boolean,
    val lhs: Node,
    val rhs: Node,
) : Node(width)

internal class CaseNode(
    width: Int,
    val parallel: Boolean,
    val arms: Array<Arm>,
) : Node(width) {
    class Arm()
}
