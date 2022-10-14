package org.khdl.dsl

public interface Type {
    public val bitWidth: Int
}

public object Clock : Type {
    override val bitWidth: Int get() = 1
}

public sealed interface Integer : Type

public data class Unsigned(override val bitWidth: Int) : Integer

public data class Signed(override val bitWidth: Int) : Integer
