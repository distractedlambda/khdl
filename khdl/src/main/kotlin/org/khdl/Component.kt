package org.khdl

internal interface Component {
    fun visitInputWires(visit: (Wire) -> Unit)

    fun emitSystemVerilog(output: SystemVerilogOutput)
}

internal fun Component.drivenWire(): Wire {
    return Wire(this)
}

internal fun Component.drivenWires(count: Int): WireBundle {
    return when {
        count <= 0 -> throw IllegalArgumentException()
        count == 1 -> Wire(this)
        else -> ArrayWireBundle(Array(count) { Wire(this) })
    }
}
