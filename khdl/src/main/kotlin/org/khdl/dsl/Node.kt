package org.khdl.dsl

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet

internal sealed class Wire

internal sealed class Op

internal object ZeroWire : Wire()

internal object OneWire : Wire()

internal object DontCareWire : Wire()

internal object HighImpedanceWire : Wire()

internal data class NotWire(val input: Wire) : Wire()

internal data class AndWire(val inputs: PersistentSet<Wire>) : Wire()

internal data class OrWire(val inputs: PersistentSet<Wire>) : Wire()

internal data class XorWire(val inputs: PersistentSet<Wire>) : Wire()

internal data class RegisteredWire(val input: Wire, val clock: Wire, val clockEnable: Wire) : Wire()

internal data class OpWire(val op: Op, val bit: Int) : Wire()

internal class SplicedWire : Wire() {
    var driver: Wire? = null
}

internal data class UnsignedAddOp(val lhs: PersistentList<Wire>, val rhs: PersistentList<Wire>) : Op()

internal data class SignedAddOp(val lhs: PersistentList<Wire>, val rhs: PersistentList<Wire>) : Op()

internal data class UnsignedSubtractOp(val lhs: PersistentList<Wire>, val rhs: PersistentList<Wire>) : Op()

internal data class SignedSubtractOp(val lhs: PersistentList<Wire>, val rhs: PersistentList<Wire>) : Op()

internal data class UnsignedMultiplyOp(val lhs: PersistentList<Wire>, val rhs: PersistentList<Wire>) : Op()

internal data class SignedMultiplyOp(val lhs: PersistentList<Wire>, val rhs: PersistentList<Wire>) : Op()

internal data class MuxOp(val address: PersistentList<Wire>, val choices: PersistentList<PersistentList<Wire>>) : Op()

internal class Ram(val width: Int, val depth: Int, val writePorts: PersistentList<WritePort>) {
    data class WritePort(
        val address: PersistentList<Wire>,
        val data: PersistentList<Wire>,
        val clock: Wire,
        val writeEnable: Wire,
    )
}

internal data class RamReadOp(val ram: Ram, val address: PersistentList<Wire>) : Op()
