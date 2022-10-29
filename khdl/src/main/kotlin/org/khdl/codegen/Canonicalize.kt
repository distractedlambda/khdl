package org.khdl.codegen

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import org.khdl.dsl.AndWire
import org.khdl.dsl.DontCareWire
import org.khdl.dsl.HighImpedanceWire
import org.khdl.dsl.InputWire
import org.khdl.dsl.NotWire
import org.khdl.dsl.OneWire
import org.khdl.dsl.OrWire
import org.khdl.dsl.RegisteredWire
import org.khdl.dsl.SignedEqWire
import org.khdl.dsl.SignedGeWire
import org.khdl.dsl.SignedGtWire
import org.khdl.dsl.SignedLeWire
import org.khdl.dsl.SignedLtWire
import org.khdl.dsl.SignedNeWire
import org.khdl.dsl.SplicedWire
import org.khdl.dsl.UnsignedEqWire
import org.khdl.dsl.UnsignedGeWire
import org.khdl.dsl.UnsignedGtWire
import org.khdl.dsl.UnsignedLeWire
import org.khdl.dsl.UnsignedLtWire
import org.khdl.dsl.UnsignedNeWire
import org.khdl.dsl.Wire
import org.khdl.dsl.XorWire
import org.khdl.dsl.ZeroWire

internal class Canonicalizer {
    private val notWires = hashMapOf<Wire, NotWire>()
    private val andWires = hashMapOf<Set<Wire>, AndWire>()
    private val orWires = hashMapOf<Set<Wire>, OrWire>()
    private val xorWires = hashMapOf<Set<Wire>, XorWire>()
    private val registeredWires = hashMapOf<Triple<Wire, Wire, Wire>, RegisteredWire>()
    private val unsignedEqWires = hashMapOf<Pair<PersistentList<Wire>, PersistentList<Wire>>, UnsignedEqWire>()
    private val unsignedNeWires = hashMapOf<Pair<PersistentList<Wire>, PersistentList<Wire>>, UnsignedNeWire>()
    private val unsignedLtWires = hashMapOf<Pair<PersistentList<Wire>, PersistentList<Wire>>, UnsignedLtWire>()
    private val unsignedGtWires = hashMapOf<Pair<PersistentList<Wire>, PersistentList<Wire>>, UnsignedGtWire>()
    private val unsignedLeWires = hashMapOf<Pair<PersistentList<Wire>, PersistentList<Wire>>, UnsignedLeWire>()
    private val unsignedGeWires = hashMapOf<Pair<PersistentList<Wire>, PersistentList<Wire>>, UnsignedGeWire>()
    private val signedEqWires = hashMapOf<Pair<PersistentList<Wire>, PersistentList<Wire>>, SignedEqWire>()
    private val signedNeWires = hashMapOf<Pair<PersistentList<Wire>, PersistentList<Wire>>, SignedNeWire>()
    private val signedLtWires = hashMapOf<Pair<PersistentList<Wire>, PersistentList<Wire>>, SignedLtWire>()
    private val signedGtWires = hashMapOf<Pair<PersistentList<Wire>, PersistentList<Wire>>, SignedGtWire>()
    private val signedLeWires = hashMapOf<Pair<PersistentList<Wire>, PersistentList<Wire>>, SignedLeWire>()
    private val signedGeWires = hashMapOf<Pair<PersistentList<Wire>, PersistentList<Wire>>, SignedGeWire>()

    private val canonicalizations = hashMapOf<Wire, Wire>()
    private val pathComponents = hashSetOf<Wire>()
    private val toSplice = hashMapOf<Wire, SplicedWire>()

    fun rewrite(original: Wire): Wire {
        canonicalizations[original]?.let {
            return it
        }

        if (!pathComponents.add(original)) {
            return SplicedWire().also {
                toSplice[original] = it
                canonicalizations[original] = it
            }
        }

        val canonical = run canonicalize@{
            when (original) {
                ZeroWire, OneWire, DontCareWire, HighImpedanceWire, is InputWire -> {
                    original
                }

                is NotWire -> when (val input = rewrite(original.input)) {
                    ZeroWire -> {
                        OneWire
                    }

                    OneWire -> {
                        ZeroWire
                    }

                    DontCareWire, HighImpedanceWire -> {
                        DontCareWire
                    }

                    is NotWire -> {
                        input.input
                    }

                    is UnsignedEqWire -> {
                        unsignedNeWires.getOrPut(input.lhs to input.rhs) { UnsignedNeWire(input.lhs, input.rhs) }
                    }

                    is UnsignedNeWire -> {
                        unsignedEqWires.getOrPut(input.lhs to input.rhs) { UnsignedEqWire(input.lhs, input.rhs) }
                    }

                    is UnsignedLtWire -> {
                        unsignedGeWires.getOrPut(input.lhs to input.rhs) { UnsignedGeWire(input.lhs, input.rhs) }
                    }

                    is UnsignedGtWire -> {
                        unsignedLeWires.getOrPut(input.lhs to input.rhs) { UnsignedLeWire(input.lhs, input.rhs) }
                    }

                    is UnsignedLeWire -> {
                        unsignedGtWires.getOrPut(input.lhs to input.rhs) { UnsignedGtWire(input.lhs, input.rhs) }
                    }

                    is UnsignedGeWire -> {
                        unsignedLtWires.getOrPut(input.lhs to input.rhs) { UnsignedLtWire(input.lhs, input.rhs) }
                    }

                    is SignedEqWire -> {
                        signedNeWires.getOrPut(input.lhs to input.rhs) { SignedNeWire(input.lhs, input.rhs) }
                    }

                    is SignedNeWire -> {
                        signedEqWires.getOrPut(input.lhs to input.rhs) { SignedEqWire(input.lhs, input.rhs) }
                    }

                    is SignedLtWire -> {
                        signedGeWires.getOrPut(input.lhs to input.rhs) { SignedGeWire(input.lhs, input.rhs) }
                    }

                    is SignedGtWire -> {
                        signedLeWires.getOrPut(input.lhs to input.rhs) { SignedLeWire(input.lhs, input.rhs) }
                    }

                    is SignedLeWire -> {
                        signedGtWires.getOrPut(input.lhs to input.rhs) { SignedGtWire(input.lhs, input.rhs) }
                    }

                    is SignedGeWire -> {
                        signedLtWires.getOrPut(input.lhs to input.rhs) { SignedLtWire(input.lhs, input.rhs) }
                    }

                    else -> {
                        notWires.getOrPut(input) { NotWire(input) }
                    }
                }

                is AndWire -> {
                    val inputs = buildSet {
                        original.inputs.forEach {
                            when (val input = rewrite(it)) {
                                ZeroWire -> return@canonicalize ZeroWire
                                OneWire -> {}
                                HighImpedanceWire -> add(DontCareWire)
                                is AndWire -> addAll(input.inputs)
                                else -> add(input)
                            }
                        }
                    }

                    when (inputs.size) {
                        0 -> OneWire
                        1 -> inputs.single()
                        else -> andWires.getOrPut(inputs) { AndWire(inputs.toPersistentList()) }
                    }
                }

                is OrWire -> {
                    val inputs = buildSet {
                        original.inputs.forEach {
                            when (val input = rewrite(it)) {
                                ZeroWire -> {}
                                OneWire -> return@canonicalize OneWire
                                HighImpedanceWire -> add(DontCareWire)
                                is OrWire -> addAll(input.inputs)
                                else -> add(input)
                            }
                        }
                    }

                    when (inputs.size) {
                        0 -> ZeroWire
                        1 -> inputs.single()
                        else -> orWires.getOrPut(inputs) { OrWire(inputs.toPersistentList()) }
                    }
                }

                is XorWire -> {
                    var negated = false

                    val inputs = buildSet {
                        original.inputs.forEach {
                            when (val input = rewrite(it)) {
                                ZeroWire -> {}

                                OneWire -> {
                                    negated = !negated
                                }

                                DontCareWire, HighImpedanceWire -> {
                                    return@canonicalize DontCareWire
                                }

                                is XorWire -> input.inputs.forEach { inputInput ->
                                    if (!add(inputInput)) {
                                        remove(inputInput)
                                    }
                                }

                                else -> if (!add(input)) {
                                    remove(input)
                                }
                            }
                        }
                    }

                    val base = when (inputs.size) {
                        0 -> return@canonicalize ZeroWire
                        1 -> inputs.single()
                        else -> xorWires.getOrPut(inputs) { XorWire(inputs.toPersistentList()) }
                    }

                    if (negated) {
                        notWires.getOrPut(base) { NotWire(base) }
                    } else {
                        base
                    }
                }

                is RegisteredWire -> {
                    val input = rewrite(original.input)
                    val clock = rewrite(original.clock)
                    val clockEnable = rewrite(original.clockEnable)
                    registeredWires.getOrPut(Triple(input, clock, clockEnable)) {
                        RegisteredWire(input, clock, clockEnable)
                    }
                }

                is SplicedWire -> {
                    rewrite(original.driver)
                }
            }
        }

        pathComponents.remove(original)
        toSplice.remove(original)?.driver = canonical
        canonicalizations[original] = canonical

        return canonical
    }
}
