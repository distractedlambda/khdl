package org.khdl.examples.riscv

import org.khdl.ir.BitVector

data class StreamRequest<T>(val valid: BitVector, val data: T) {
    init {
        require(valid.width == 1)
    }
}

data class StreamResponse(val ready: BitVector) {
    init {
        require(ready.width == 1)
    }
}
