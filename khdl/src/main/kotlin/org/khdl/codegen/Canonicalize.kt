package org.khdl.codegen

import kotlinx.collections.immutable.toPersistentList
import org.khdl.dsl.AndWire
import org.khdl.dsl.DontCareWire
import org.khdl.dsl.HighImpedanceWire
import org.khdl.dsl.InputWire
import org.khdl.dsl.NotWire
import org.khdl.dsl.OneWire
import org.khdl.dsl.OrWire
import org.khdl.dsl.SplicedWire
import org.khdl.dsl.Wire
import org.khdl.dsl.XorWire
import org.khdl.dsl.ZeroWire

internal class Canonicalizer {
    private val notWires = hashMapOf<Wire, Wire>()
    private val andWires = hashMapOf<Set<Wire>, Wire>()
    private val orWires = hashMapOf<Set<Wire>, Wire>()
    private val xorWires = hashMapOf<Set<Wire>, Wire>()

    private val pathComponents = hashSetOf<Wire>()
    private val toSplice = hashMapOf<Wire, SplicedWire>()

    fun rewrite(original: Wire): Wire {
        if (!pathComponents.add(original)) {
            return SplicedWire().also {
                toSplice[original] = it
            }
        }

        val canonical = run canonicalize@{
            when (original) {
                ZeroWire, OneWire, DontCareWire, HighImpedanceWire, is InputWire -> {
                    original
                }

                is NotWire -> when (val input = rewrite(original.input)) {
                    ZeroWire -> OneWire
                    OneWire -> ZeroWire
                    DontCareWire, HighImpedanceWire -> DontCareWire
                    is NotWire -> input.input
                    else -> notWires.getOrPut(input) { NotWire(input) }
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
            }
        }

        toSplice.remove(original)?.driver = canonical
        pathComponents.remove(original)
        return canonical
    }
}
