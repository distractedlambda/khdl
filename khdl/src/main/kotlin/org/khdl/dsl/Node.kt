package org.khdl.dsl

import java.lang.Math.addExact
import java.lang.Math.multiplyExact
import java.util.Objects.checkFromIndexSize

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

internal fun Node.repeat(times: Int): Node {
    return when (times) {
        0 -> NilNode
        1 -> if (width > 0) this else NilNode
        in 2..Int.MAX_VALUE -> if (width > 0) RepeatNode(this, times) else NilNode
        else -> throw IllegalArgumentException()
    }
}

internal fun Node.slice(start: Int, width: Int): Node {
    checkFromIndexSize(start, width, this.width)
    return when (width) {
        0 -> NilNode
        this.width -> this
        else -> SliceNode(this, start, width)
    }
}

internal fun Node.reductiveAnd(): Node {
    return when (width) {
        0 -> ConstantNode(byteArrayOf(ConstantNode.ONE))
        1 -> this
        else -> ReductiveAndNode(this)
    }
}

internal fun Node.reductiveOr(): Node {
    return when (width) {
        0 -> ConstantNode(byteArrayOf(ConstantNode.ZERO))
        1 -> this
        else -> ReductiveOrNode(this)
    }
}

internal fun Node.reductiveXor(): Node {
    return when (width) {
        0 -> ConstantNode(byteArrayOf(ConstantNode.ZERO))
        1 -> this
        else -> ReductiveXorNode(this)
    }
}

internal fun concat(nodes: Iterable<Node>): Node {
    val parts = nodes.mapNotNull { it.takeIf { it.width > 0 } }
    return when (parts.size) {
        0 -> NilNode
        1 -> parts.single()
        else -> ConcatNode(parts.toTypedArray())
    }
}

internal fun concat(vararg nodes: Node): Node {
    return concat(nodes.asIterable())
}

internal object NilNode : Node(0) {
    override fun toString(): String {
        return "{}"
    }
}

internal class ConstantNode(val value: ByteArray) : Node(value.size) {
    init {
        require(value.isNotEmpty())
    }

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
    init {
        require(width > 0)
    }

    override fun toString(): String {
        return "<$width>$name"
    }
}

internal class ConcatNode(val parts: Array<Node>) : Node(parts.fold(0) { acc, node -> addExact(acc, node.width) }) {
    init {
        require(parts.isNotEmpty())
        require(parts.all { it.width > 0 })
    }

    override fun toString(): String {
        return parts.joinToString(separator = ", ", prefix = "{", postfix = "}")
    }
}

internal class RepeatNode(val subject: Node, val times: Int) : Node(multiplyExact(subject.width, times)) {
    init {
        require(subject.width > 0)
        require(times > 0)
    }

    override fun toString(): String {
        return "{$times{$subject}}"
    }
}

internal class SliceNode(val subject: Node, val start: Int, width: Int) : Node(width) {
    init {
        require(subject.width > 0)
        require(width in 1..subject.width)
        require(start in 0..(subject.width - width))
    }

    override fun toString(): String {
        return "$subject[$start..${start + width - 1}]"
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
    init {
        require(operand.width > 1)
    }

    override fun toString(): String {
        return "&$operand"
    }
}

internal class ReductiveOrNode(val operand: Node) : Node(1) {
    init {
        require(operand.width > 1)
    }

    override fun toString(): String {
        return "|$operand"
    }
}

internal class ReductiveXorNode(val operand: Node) : Node(1) {
    init {
        require(operand.width > 1)
    }

    override fun toString(): String {
        return "^$operand"
    }
}

internal class OnesComplementNode(val operand: Node) : Node(operand.width) {
    init {
        require(operand.width > 0)
    }

    override fun toString(): String {
       return "~$operand"
    }
}

internal class AddNode(val lhs: Node, val rhs: Node) : Node(addExact(lhs.width, 1)) {
    init {
        require(lhs.width == rhs.width)
        require(lhs.width > 0)
    }

    override fun toString(): String {
        return "($lhs + $rhs)"
    }
}
