package org.khdl.dsl

public sealed interface Integer : Type

public operator fun <T : Integer> Signal<T>.unaryPlus(): Signal<T> {
    return this
}
