package org.khdl.collections.immutable

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

@Suppress("FunctionName")
@OptIn(ExperimentalContracts::class)
public inline fun <E> PersistentList(size: Int, block: (Int) -> E): PersistentList<E> {
    contract {
        callsInPlace(block, InvocationKind.UNKNOWN)
    }

    return buildPersistentList {
        repeat(size) {
            add(block(it))
        }
    }
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun <E> buildPersistentList(@BuilderInference block: MutableList<E>.() -> Unit): PersistentList<E> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return persistentListOf<E>().builder().apply(block).build()
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun <E> buildPersistentSet(@BuilderInference block: MutableSet<E>.() -> Unit): PersistentSet<E> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return persistentSetOf<E>().builder().apply(block).build()
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun <E> buildPersistentHashSet(@BuilderInference block: MutableSet<E>.() -> Unit): PersistentSet<E> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return persistentHashSetOf<E>().builder().apply(block).build()
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun <K, V> buildPersistentMap(@BuilderInference block: MutableMap<K, V>.() -> Unit): PersistentMap<K, V> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return persistentMapOf<K, V>().builder().apply(block).build()
}

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
public inline fun <K, V> buildPersistentHashMap(@BuilderInference block: MutableMap<K, V>.() -> Unit): PersistentMap<K, V> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return persistentHashMapOf<K, V>().builder().apply(block).build()
}
