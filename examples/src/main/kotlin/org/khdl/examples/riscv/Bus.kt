package org.khdl.examples.riscv

import org.khdl.ir.BitVector

data class ReadRequest<A>(
    val address: A,
    val valid: BitVector,
    val ready: BitVector,
) {
    init {
        require(valid.width == 1)
        require(ready.width == 1)
    }
}

data class ReadResponse<T>(
    val valid: BitVector,
    val data: T,
) {
    init {
        require(valid.width == 1)
    }
}
