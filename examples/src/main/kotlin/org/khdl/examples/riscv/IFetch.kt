package org.khdl.examples.riscv

import org.khdl.dsl.concat
import org.khdl.dsl.ones
import org.khdl.dsl.zero
import org.khdl.dsl.Signal

data class FetchedInstruction(
    val compressedInstruction: Signal,
    val compressedAddress: Signal,
    val illegal: Signal,
) {
    init {
        require(compressedInstruction.width == 30)
        require(compressedAddress.width == 31)
        require(illegal.width == 1)
    }

    val instruction: Signal
        get() {
        return concat(compressedInstruction, ones(2))
    }

    val address: Signal
        get() {
        return concat(compressedAddress, zero())
    }
}
