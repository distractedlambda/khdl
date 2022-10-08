package org.khdl.components

import org.khdl.Component
import org.khdl.Wire
import org.khdl.WireBundle
import org.khdl.drivenWires

internal class ParallelNot(val input: WireBundle) : Component {
    val output = drivenWires(input.size)

    override fun visitInputWires(visit: (Wire) -> Unit) {
        input.forEach(visit)
    }
}
