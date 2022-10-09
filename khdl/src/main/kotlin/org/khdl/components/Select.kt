package org.khdl.components

import org.khdl.Component
import org.khdl.SystemVerilogOutput
import org.khdl.Wire
import org.khdl.WireBundle
import org.khdl.drivenWires

internal class Select(val subject: WireBundle, val clauses: List<Clause>, val defaultValue: WireBundle) : Component {
    val output = drivenWires(defaultValue.size)

    override fun visitInputWires(visit: (Wire) -> Unit) {
        subject.forEach(visit)

        clauses.forEach { (key, value) ->
            key.forEach(visit)
            value.forEach(visit)
        }

        defaultValue.forEach(visit)
    }

    override fun emitSystemVerilog(output: SystemVerilogOutput) {
        TODO()
    }

    data class Clause(val key: WireBundle, val value: WireBundle)
}
