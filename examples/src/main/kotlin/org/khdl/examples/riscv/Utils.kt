package org.khdl.examples.riscv

import org.khdl.dsl.concat
import org.khdl.dsl.get
import org.khdl.dsl.signExtend
import org.khdl.dsl.slice
import org.khdl.dsl.zero
import org.khdl.dsl.zeros
import org.khdl.dsl.Signal

data class Instruction32(val bits: Signal) {
    init {
        require(bits.width == 32)
    }

    val opcode: Signal
        get() {
        return bits.slice(6, 0)
    }

    val funct3: Signal
        get() {
        return bits.slice(14, 12)
    }

    val funct7: Signal
        get() {
        return bits.slice(31, 25)
    }

    val rd: Signal
        get() {
        return bits.slice(11, 7)
    }

    val rs1: Signal
        get() {
        return bits.slice(19, 15)
    }

    val rs2: Signal
        get() {
        return bits.slice(24, 20)
    }

    val iImmediate: Signal
        get() {
        return bits.slice(31, 20).signExtend(32)
    }

    val sImmediate: Signal
        get() {
        return concat(bits.slice(31, 25), bits.slice(11, 7)).signExtend(32)
    }

    val uImmediate: Signal
        get() {
        return concat(bits.slice(31, 12), zeros(12))
    }

    val bImmediate: Signal
        get() {
        return concat(bits[31], bits[7], bits.slice(30, 25), bits.slice(11, 8), zero()).signExtend(32)
    }

    val jImmediate: Signal
        get() {
        return concat(bits[20], bits.slice(19, 12), bits[20], bits.slice(30, 21), zero()).signExtend(32)
    }
}
