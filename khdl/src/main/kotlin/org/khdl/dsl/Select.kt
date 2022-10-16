package org.khdl.dsl

import kotlin.experimental.ExperimentalTypeInference

public class SelectScope<T : Type> internal constructor() {
    private var type: T? = null
    private val clauses = mutableListOf<Pair<Node, Node>>()
    private var defaultClause: Node? = null

    private fun propagateType(type: T) {
        if (this.type == null) {
            this.type = type
        } else {
            check(type == this.type)
        }
    }

    public fun where(condition: Signal<Bit>, value: Signal<T>) {
        propagateType(value.type)
        clauses.add(condition.node to value.node)
    }

    public fun otherwise(value: Signal<T>) {
        propagateType(value.type)
        defaultClause = value.node
    }

    internal fun build(): Signal<T> {
        return Signal(
            checkNotNull(type),
            clauses.foldRight(checkNotNull(defaultClause)) { (condition, ifTrue), ifFalse ->
                ConditionalNode(condition, ifTrue, ifFalse)
            },
        )
    }
}

@OptIn(ExperimentalTypeInference::class)
public fun <T : Type> select(@BuilderInference block: SelectScope<T>.() -> Unit): Signal<T> {
    return SelectScope<T>().apply(block).build()
}
