package ianmorgan.candycoins.flow

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import ianmorgan.candycoins.contract.CandyBagContract
import ianmorgan.candycoins.state.CandyBagState


@InitiatingFlow
@StartableByRPC
class CandyBagGiveFlow(val state: CandyBagState): FlowLogic<SignedTransaction>() {
    @Suspendable
    override fun call(): SignedTransaction {
        // Step 1. Locate the Notary
        val notary = serviceHub.networkMapCache.notaryIdentities.first()

        // Step 2. This is a 'Give' command.
        val giveCommand = Command(CandyBagContract.Commands.Give(), state.participants.map { it.owningKey })

        // Step 3. Create a new TransactionBuilder object.
        val builder = TransactionBuilder(notary = notary)

        // Step 4. Add the iou as an output state, as well as a command to the transaction builder.
        builder.addOutputState(state, CandyBagContract.CANDY_CONTRACT_ID)
        builder.addCommand(giveCommand)

        // Step 5. Verify and sign it with our KeyPair.
        builder.verify(serviceHub)
        val ptx = serviceHub.signInitialTransaction(builder)

        val sessions = (state.participants - ourIdentity).map { initiateFlow(it) }.toSet()
        // Step 6. Collect the other party's signature using the SignTransactionFlow.
        val stx = subFlow(CollectSignaturesFlow(ptx, sessions))

        // Step 7. Assuming no exceptions, we can now finalise the transaction.
        return subFlow(FinalityFlow(stx))

        //return stx
    }
}

/**
 * This is the flow which signs the giving out of KandyBags.
 * The signing is handled by the [SignTransactionFlow].
 */
@InitiatedBy(CandyBagGiveFlow::class)
class KandyBagGiveFlowResponder(val flowSession: FlowSession): FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        val signedTransactionFlow = object : SignTransactionFlow(flowSession) {
            override fun checkTransaction(stx: SignedTransaction) = requireThat {
                val output = stx.tx.outputs.single().data
                "This must be an Kandy transaction" using (output is CandyBagState)
            }
        }
        subFlow(signedTransactionFlow)
    }
}