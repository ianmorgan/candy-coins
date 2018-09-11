package ianmorgan.kandykoins

import co.paralleluniverse.strands.Strand
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.TransactionState
import net.corda.core.identity.Party
import net.corda.core.internal.FlowStateMachine
import net.corda.core.transactions.TransactionBuilder
import java.util.*

class MyTransactionBuilder : TransactionBuilder {
    constructor(notary: Party) : super(notary, (Strand.currentStrand() as? FlowStateMachine<*>)?.id?.uuid
            ?: UUID.randomUUID())

    override fun addInputState(stateAndRef: StateAndRef<*>): TransactionBuilder {
        println("in addInputState")
        return super.addInputState(stateAndRef);
    }

    fun report () : Unit {
        println(super.inputs)
        println(super.outputs)
    }
}
