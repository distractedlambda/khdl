package org.khdl

public sealed interface WireBundle {
    public val size: Int

    public operator fun get(index: Int): Wire

    public operator fun get(indices: IntProgression): WireBundle

    public fun forEach(block: (Wire) -> Unit)

    public fun repeat(times: Int): WireBundle
}
