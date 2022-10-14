package org.khdl.dsl

import java.lang.Math.addExact
import java.lang.Math.multiplyExact

internal sealed class Node(internal val width: Int) {
    init {
        require(width >= 0)
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
    internal lateinit var driver: Node

    fun connectDriver(driver: Node) {
        require(driver.width == width)
        check(!this::driver.isInitialized)
        this.driver = driver
    }
}

internal class RegisterNode(val input: Node, val clock: Node) : Node(input.width) {
    init {
        require(clock.width == 1)
    }
}

internal class ModuleInputNode(val name: String, width: Int) : Node(width)

internal class ConcatNode(val parts: Array<Node>) : Node(parts.fold(0) { acc, node -> addExact(acc, node.width) })

internal class SliceNode(val subject: Node, val start: Int, width: Int) : Node(width) {
    init {
        require(start >= 0)
        require(subject.width - start >= width)
    }
}

internal class AndNode(val lhs: Node, val rhs: Node) : Node(lhs.width) {
    init {
        require(lhs.width == rhs.width)
    }
}

internal class OrNode(val lhs: Node, val rhs: Node) : Node(lhs.width) {
    init {
        require(lhs.width == rhs.width)
    }
}

internal class XorNode(val lhs: Node, val rhs: Node) : Node(lhs.width) {
    init {
        require(lhs.width == rhs.width)
    }
}

internal class ReductiveAndNode(val operand: Node) : Node(1)

internal class ReductiveOrNode(val operand: Node) : Node(1)

internal class ReductiveXorNode(val operand: Node) : Node(1)

internal class RepeatNode(val subject: Node, val times: Int) : Node(multiplyExact(subject.width, times))

internal class OnesComplementNode(val operand: Node) : Node(operand.width)

internal class AddNode(val lhs: Node, val rhs: Node) : Node(if (lhs.width != 0) addExact(lhs.width, 1) else 0) {
    init {
        require(lhs.width == rhs.width)
    }
}
