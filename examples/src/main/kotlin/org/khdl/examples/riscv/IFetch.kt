package org.khdl.examples.riscv

import org.khdl.dsl.concat
import org.khdl.dsl.ones
import org.khdl.dsl.register
import org.khdl.dsl.zero
import org.khdl.dsl.zeros
import org.khdl.ir.BitVector

data class FetchedInstruction(
    val compressedInstruction: BitVector,
    val compressedAddress: BitVector,
    val illegal: BitVector,
) {
    init {
        require(compressedInstruction.width == 30)
        require(compressedAddress.width == 31)
        require(illegal.width == 1)
    }

    val instruction: BitVector get() {
        return concat(compressedInstruction, ones(2))
    }

    val address: BitVector get() {
        return concat(compressedAddress, zero())
    }
}

data class InstructionFetch(
    val instruction: StreamRequest<FetchedInstruction>,
    val bus: ReadRequest<BitVector>,
)

fun InstructionFetch(
    clock: BitVector,
    instructionResponse: StreamResponse,
    busResponse: ReadResponse<BitVector>,
): InstructionFetch {
    val fetchAddress = register(clock, 30)
    TODO()
}
