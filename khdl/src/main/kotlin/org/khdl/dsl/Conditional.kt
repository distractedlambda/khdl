package org.khdl.dsl

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.khdl.collections.immutable.PersistentList
import kotlin.experimental.ExperimentalTypeInference

public class ConditionalScope<T : Type> internal constructor() {
    private var type: T? = null
    private val clauses = persistentListOf<ConditionalOp.Clause>().builder()
    private var default: PersistentList<Wire>? = null

    private fun propagateType(type: T) {
        if (this.type == null) {
            this.type = type
        } else {
            check(type == this.type)
        }
    }

    public fun where(condition: Signal<Bit>, value: Signal<T>) {
        propagateType(value.type)
        clauses.add(ConditionalOp.Clause(condition.wires.single(), value.wires))
    }

    public fun otherwise(value: Signal<T>) {
        propagateType(value.type)
        default = value.wires
    }

    internal fun build(): Signal<T> {
        val type = checkNotNull(type)
        val op = ConditionalOp(clauses.build(), checkNotNull(default))
        return Signal(
            type,
            PersistentList(type.bitWidth) { OpWire(op, it) },
        )
    }
}

@OptIn(ExperimentalTypeInference::class)
public fun <T : Type> conditional(@BuilderInference block: ConditionalScope<T>.() -> Unit): Signal<T> {
    return ConditionalScope<T>().apply(block).build()
}
