package org.khdl.dsl

public sealed interface Input<out T : Type> {
    public val signal: Signal<T>
}

public sealed interface Output<in T : Type> {
    public fun connectDriver(driver: Signal<T>)
}

public class Port<T : Type>(type: T) : Input<T>, Output<T> {
    override val signal: Signal<T> = Signal(type, WireNode(type.bitWidth))

    override fun connectDriver(driver: Signal<T>) {
        require(driver.type == signal.type)
        (signal.node as WireNode).connectDriver(driver.node)
    }
}
