package org.khdl.dsl

public class Register<T : Type>(type: T, clock: Signal<Clock>) {
    public val signal: Signal<T> = Signal(type, RegisterNode(WireNode(type.bitWidth), clock.node))

    public val clock: Signal<Clock> get() {
        return Signal(Clock, (signal.node as RegisterNode).clock)
    }

    public fun connectInput(input: Signal<T>) {
        require(input.type == signal.type)
        ((signal.node as RegisterNode).input as WireNode).connectDriver(input.node)
    }
}
