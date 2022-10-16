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

internal object NilNode : Node(0) {
    override fun toString(): String {
        return "{}"
    }
}

internal class ConstantNode(val value: ByteArray) : Node(value.size) {
    override fun toString(): String {
        return buildString {
            append("'")
            for (byte in value) append(when (byte) {
                ZERO -> '0'
                ONE -> '1'
                DONT_CARE -> 'X'
                HIGH_IMPEDANCE -> 'Z'
                else -> error("Malformed constant")
            })
            append("'")
        }
    }

    internal companion object {
        const val ZERO: Byte = 0
        const val ONE: Byte = 1
        const val DONT_CARE: Byte = 2
        const val HIGH_IMPEDANCE: Byte = 3
    }
}

internal class WireNode(width: Int) : Node(width) {
    internal lateinit var driver: Node

    fun connectDriver(driver: Node) {
        require(driver.width == width)
        check(!this::driver.isInitialized)
        this.driver = driver
    }

    override fun toString(): String {
        return "<$width>#${hashCode()}"
    }
}

internal class RegisterNode(val input: Node, val clock: Node) : Node(input.width) {
    init {
        require(clock.width == 1)
    }

    override fun toString(): String {
        return "($input @ $clock)"
    }
}

internal class ModuleInputNode(val name: String, width: Int) : Node(width) {
    override fun toString(): String {
        return "<$width>$name"
    }
}

internal class ConcatNode(val parts: Array<Node>) : Node(parts.fold(0) { acc, node -> addExact(acc, node.width) }) {
    override fun toString(): String {
        return parts.joinToString(separator = ", ", prefix = "{", postfix = "}")
    }
}

internal class RepeatNode(val subject: Node, val times: Int) : Node(multiplyExact(subject.width, times)) {
    init {
        require(times >= 0)
    }

    override fun toString(): String {
        return "{$times{$subject}}"
    }
}

internal class SliceNode(val subject: Node, val start: Int, width: Int) : Node(width) {
    init {
        require(width in 1..subject.width)
        require(start in 0..(subject.width - width))
    }

    override fun toString(): String {
        return "$subject[$start..${start + width - 1}]"
    }
}

internal class ZeroExtendNode(width: Int, val operand: Node) : Node(width) {
    init {
        require(width >= operand.width)
    }

    override fun toString(): String {
        return "($operand zext $width)"
    }
}

internal class SignExtendNode(width: Int, val operand: Node) : Node(width) {
    init {
        require(width >= operand.width)
    }

    override fun toString(): String {
        return "($operand sext $width)"
    }
}

internal class AndNode(val lhs: Node, val rhs: Node) : Node(lhs.width) {
    init {
        require(lhs.width == rhs.width)
    }

    override fun toString(): String {
        return "($lhs & $rhs)"
    }
}

internal class OrNode(val lhs: Node, val rhs: Node) : Node(lhs.width) {
    init {
        require(lhs.width == rhs.width)
    }

    override fun toString(): String {
        return "($lhs | $rhs)"
    }
}

internal class XorNode(val lhs: Node, val rhs: Node) : Node(lhs.width) {
    init {
        require(lhs.width == rhs.width)
    }

    override fun toString(): String {
        return "($lhs ^ $rhs)"
    }
}

internal class ReductiveAndNode(val operand: Node) : Node(1) {
    override fun toString(): String {
        return "&$operand"
    }
}

internal class ReductiveOrNode(val operand: Node) : Node(1) {
    override fun toString(): String {
        return "|$operand"
    }
}

internal class ReductiveXorNode(val operand: Node) : Node(1) {
    override fun toString(): String {
        return "^$operand"
    }
}

internal class OnesComplementNode(val operand: Node) : Node(operand.width) {
    override fun toString(): String {
       return "~$operand"
    }
}

internal class TwosComplementNode(val operand: Node) : Node(operand.width) {
    override fun toString(): String {
        return "-$operand"
    }
}

internal class AddNode(val lhs: Node, val rhs: Node) : Node(lhs.width) {
    init {
        require(lhs.width == rhs.width)
    }

    override fun toString(): String {
        return "($lhs + $rhs)"
    }
}

internal class UnsignedMultiplyNode(val lhs: Node, val rhs: Node) : Node(lhs.width) {
    init {
        require(lhs.width == rhs.width)
    }

    override fun toString(): String {
        return "($lhs u* $rhs)"
    }
}

internal class SignedMultiplyNode(val lhs: Node, val rhs: Node) : Node(lhs.width) {
    init {
        require(lhs.width == rhs.width)
    }

    override fun toString(): String {
        return "($lhs s* $rhs)"
    }
}

internal class EqNode(val lhs: Node, val rhs: Node) : Node(1) {
    init {
        require(lhs.width == rhs.width)
    }

    override fun toString(): String {
        return "($lhs == $rhs)"
    }
}

internal class UnsignedLtNode(val lhs: Node, val rhs: Node) : Node(1) {
    init {
        require(lhs.width == rhs.width)
    }

    override fun toString(): String {
        return "($lhs u< $rhs)"
    }
}

internal class SignedLtNode(val lhs: Node, val rhs: Node) : Node(1) {
    init {
        require(lhs.width == rhs.width)
    }

    override fun toString(): String {
        return "($lhs s< $rhs)"
    }
}

internal class ConditionalNode(val condition: Node, val ifTrue: Node, val ifFalse: Node) : Node(ifTrue.width) {
    init {
        require(condition.width == 1)
        require(ifTrue.width == ifFalse.width)
    }

    override fun toString(): String {
        return "($condition ? $ifTrue : $ifFalse)"
    }
}
