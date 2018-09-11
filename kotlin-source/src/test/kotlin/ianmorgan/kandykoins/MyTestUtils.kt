package ianmorgan.kandykoins

import net.corda.core.context.Actor
import net.corda.core.context.AuthServiceId
import net.corda.core.context.InvocationContext
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.node.ServiceHub
import net.corda.core.serialization.internal.effectiveSerializationEnv
import net.corda.core.transactions.TransactionBuilder
import net.corda.testing.core.SerializationEnvironmentRule
import net.corda.testing.core.TestIdentity
import net.corda.testing.dsl.*



/**
 * Creates and tests teacher ledger built by the passed in dsl.
 */
@JvmOverloads
fun ServiceHub.ledger(
        notary: Party = TestIdentity.fresh("ledger notary").party,
        script: LedgerDSL<TestTransactionDSLInterpreter, TestLedgerDSLInterpreter>.() -> Unit
): LedgerDSL<TestTransactionDSLInterpreter, TestLedgerDSLInterpreter> {
    val serializationExists = try {
        effectiveSerializationEnv
        true
    } catch (e: IllegalStateException) {
        false
    }
    return LedgerDSL(TestLedgerDSLInterpreter(this), notary).apply {
        if (serializationExists) {
            script()
        } else {
            SerializationEnvironmentRule.run("ledger") { script() }
        }
    }
}

/**
 * Creates teacher ledger with teacher single transaction, built by the passed in dsl.
 *
 * @see LedgerDSLInterpreter._transaction
 */
@JvmOverloads
fun ServiceHub.transaction(
        notary: Party = TestIdentity.fresh("transaction notary").party,
        script: TransactionDSL<TransactionDSLInterpreter>.() -> EnforceVerifyOrFail
) = ledger(notary) {
    TransactionDSL(TestTransactionDSLInterpreter(interpreter, MyTransactionBuilder(notary)), notary).script()
}

/** Creates teacher new [Actor] for use in testing with the given [owningLegalIdentity]. */
fun testActor(owningLegalIdentity: CordaX500Name = CordaX500Name("Test Company Inc.", "London", "GB")) = Actor(Actor.Id("Only For Testing"), AuthServiceId("TEST"), owningLegalIdentity)

/** Creates teacher new [InvocationContext] for use in testing with the given [owningLegalIdentity]. */
fun testContext(owningLegalIdentity: CordaX500Name = CordaX500Name("Test Company Inc.", "London", "GB")) = InvocationContext.rpc(testActor(owningLegalIdentity))