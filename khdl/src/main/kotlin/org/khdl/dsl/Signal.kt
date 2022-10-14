package org.khdl.dsl

public data class Signal<out T : Type> internal constructor(public val type: T, internal val node: Node)
