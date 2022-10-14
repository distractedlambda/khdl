package org.khdl.dsl

internal sealed class Node(internal val width: Int)

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
    internal lateinit var driver: Node

    fun connectDriver(driver: Node) {
        require(driver.width == width)
        check(!this::driver.isInitialized)
        this.driver = driver
    }
}

internal class RegisterNode(val input: Node, val clock: Node) : Node(input.width)

internal class ModuleInputNode(val name: String, width: Int) : Node(width)

internal class ConcatNode(val parts: Array<Node>) : Node(parts.sumOf { it.width })

internal class SliceNode(val subject: Node, val msb: Int, val lsb: Int) : Node(msb - lsb + 1)

internal class AndNode(val lhs: Node, val rhs: Node) : Node(lhs.width)

internal class OrNode(val lhs: Node, val rhs: Node) : Node(lhs.width)

internal class XorNode(val lhs: Node, val rhs: Node) : Node(lhs.width)

internal class ReductiveAndNode(val operand: Node) : Node(1)

internal class ReductiveOrNode(val operand: Node) : Node(1)

internal class ReductiveXorNode(val operand: Node) : Node(1)

internal class RepeatNode(val subject: Node, val times: Int) : Node(subject.width * times)

internal class OnesComplementNode(val operand: Node) : Node(operand.width)

internal class AddNode(val lhs: Node, val rhs: Node) : Node(maxOf(lhs.width, rhs.width) + 1)
