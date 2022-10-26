package org.khdl.dsl

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap

internal sealed class Wire {
    final override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    final override fun hashCode(): Int {
        return super.hashCode()
    }
}

internal sealed class Op {
    final override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    final override fun hashCode(): Int {
        return super.hashCode()
    }
}

internal object ZeroWire : Wire()

internal object OneWire : Wire()

internal object DontCareWire : Wire()

internal object HighImpedanceWire : Wire()

internal data class InputWire(val name: String, val bit: Int) : Wire()

internal data class NotWire(val input: Wire) : Wire()

internal data class AndWire(val inputs: PersistentList<Wire>) : Wire()

internal data class OrWire(val inputs: PersistentList<Wire>) : Wire()

internal data class XorWire(val inputs: PersistentList<Wire>) : Wire()

internal data class UnsignedEqWire(val lhs: PersistentList<Wire>, val rhs: PersistentList<Wire>) : Wire()

internal data class UnsignedNeWire(val lhs: PersistentList<Wire>, val rhs: PersistentList<Wire>) : Wire()

internal data class UnsignedLtWire(val lhs: PersistentList<Wire>, val rhs: PersistentList<Wire>) : Wire()

internal data class UnsignedLeWire(val lhs: PersistentList<Wire>, val rhs: PersistentList<Wire>) : Wire()

internal data class UnsignedGtWire(val lhs: PersistentList<Wire>, val rhs: PersistentList<Wire>) : Wire()

internal data class UnsignedGeWire(val lhs: PersistentList<Wire>, val rhs: PersistentList<Wire>) : Wire()

internal data class SignedEqWire(val lhs: PersistentList<Wire>, val rhs: PersistentList<Wire>) : Wire()

internal data class SignedNeWire(val lhs: PersistentList<Wire>, val rhs: PersistentList<Wire>) : Wire()

internal data class SignedLtWire(val lhs: PersistentList<Wire>, val rhs: PersistentList<Wire>) : Wire()

internal data class SignedLeWire(val lhs: PersistentList<Wire>, val rhs: PersistentList<Wire>) : Wire()

internal data class SignedGtWire(val lhs: PersistentList<Wire>, val rhs: PersistentList<Wire>) : Wire()

internal data class SignedGeWire(val lhs: PersistentList<Wire>, val rhs: PersistentList<Wire>) : Wire()

internal data class RegisteredWire(val input: Wire, val clock: Wire, val clockEnable: Wire) : Wire()

internal data class OpWire(val op: Op, val bit: Int) : Wire()

internal class SplicedWire : Wire() {
    var driver: Wire = HighImpedanceWire
}

internal data class NegateOp(val operand: PersistentList<Wire>) : Op()

internal data class UnsignedAddOp(val lhs: PersistentList<Wire>, val rhs: PersistentList<Wire>) : Op()

internal data class SignedAddOp(val lhs: PersistentList<Wire>, val rhs: PersistentList<Wire>) : Op()

internal data class UnsignedSubtractOp(val lhs: PersistentList<Wire>, val rhs: PersistentList<Wire>) : Op()

internal data class SignedSubtractOp(val lhs: PersistentList<Wire>, val rhs: PersistentList<Wire>) : Op()

internal data class UnsignedMultiplyOp(val lhs: PersistentList<Wire>, val rhs: PersistentList<Wire>) : Op()

internal data class SignedMultiplyOp(val lhs: PersistentList<Wire>, val rhs: PersistentList<Wire>) : Op()

internal data class SelectOp(
    val selection: PersistentList<Wire>,
    val cases: PersistentMap<PersistentList<Wire>, PersistentList<Wire>>,
    val default: PersistentList<Wire>,
) : Op()

internal data class ConditionalOp(val clauses: PersistentList<Clause>, val default: PersistentList<Wire>) : Op() {
    data class Clause(val condition: Wire, val value: PersistentList<Wire>)
}

internal class Ram(val width: Int, val depth: Int, val writePorts: PersistentList<WritePort>) {
    data class WritePort(
        val address: PersistentList<Wire>,
        val data: PersistentList<Wire>,
        val clock: Wire,
        val writeEnable: Wire,
    )
}

internal data class RamReadOp(val ram: Ram, val address: PersistentList<Wire>) : Op()
