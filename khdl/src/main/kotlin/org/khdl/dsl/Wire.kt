package org.khdl.dsl

public class Wire<T : Type> internal constructor(type: T) {
    public val signal: Signal<T> = Signal(type, WireNode(type.bitWidth))

    public fun connectDriver(driver: Signal<T>) {
        require(driver.type == signal.type)
        (signal.node as WireNode).connectDriver(driver.node)
    }
}
