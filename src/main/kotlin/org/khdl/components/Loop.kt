package org.khdl.components

import org.khdl.Component
import org.khdl.Wire
import org.khdl.WireBundle
import org.khdl.drivenWires

internal class Loop(width: Int) : Component {
    lateinit var drive: WireBundle
    val backEdge = drivenWires(width)

    fun connect(bundle: WireBundle) {
        require(bundle.size == backEdge.size)
        check(!this::drive.isInitialized)
        this.drive = bundle
    }

    override fun visitInputWires(visit: (Wire) -> Unit) {
        drive.forEach(visit)
    }
}
