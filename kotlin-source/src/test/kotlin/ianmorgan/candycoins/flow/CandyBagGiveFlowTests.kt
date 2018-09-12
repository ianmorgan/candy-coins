package ianmorgan.candycoins.flow

import net.corda.core.contracts.TransactionVerificationException
import net.corda.core.identity.CordaX500Name
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow
import net.corda.finance.*
import net.corda.testing.internal.chooseIdentityAndCert
import net.corda.testing.node.*
import ianmorgan.candycoins.contract.CandyBagContract
import ianmorgan.candycoins.state.IOUState
import ianmorgan.candycoins.state.CandyBagState
import org.junit.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


class CandyBagGiveFlowTests {
    lateinit var mockNetwork: MockNetwork
    lateinit var teacher: StartedMockNode
    lateinit var child: StartedMockNode

    @Before
    fun setup() {
        mockNetwork = MockNetwork(listOf("ianmorgan.candycoins"),
                notarySpecs = listOf(MockNetworkNotarySpec(CordaX500Name("Notary","London","GB"))))
        teacher = mockNetwork.createNode(MockNodeParameters())
        child = mockNetwork.createNode(MockNodeParameters())
        val startedNodes = arrayListOf(teacher, child)
        // For real nodes this happens automatically, but we have to manually register the flow for tests
        startedNodes.forEach { it.registerInitiatedFlow(KandyBagGiveFlowResponder::class.java) }
        mockNetwork.runNetwork()
    }

    @After
    fun tearDown() {
        mockNetwork.stopNodes()
    }


    @Test
    fun flowReturnsCorrectlyFormedPartiallySignedTransaction() {
        // the child you will receive the kandy bag
        val owner = child.info.chooseIdentityAndCert().party
        val bag = CandyBagState(1,2,3, owner)
        val flow = CandyBagGiveFlow(bag)
        val future = teacher.startFlow(flow)
        mockNetwork.runNetwork()

        // Return the unsigned(!) SignedTransaction object from the IOUIssueFlow.
        val ptx: SignedTransaction = future.getOrThrow()
        // Print the transaction for debugging purposes.
        println(ptx.tx)
        // Check the transaction is well formed...
        // No outputs, one input IOUState and teacher command with the right properties.
        assert(ptx.tx.inputs.isEmpty())
        assert(ptx.tx.outputs.single().data is CandyBagState)
        val command = ptx.tx.commands.single()
        assert(command.value is CandyBagContract.Commands.Give)
        assert(command.signers.toSet() == bag.participants.map { it.owningKey }.toSet())
        ptx.verifySignaturesExcept(owner.owningKey,
                mockNetwork.defaultNotaryNode.info.legalIdentitiesAndCerts.first().owningKey)
    }

    /**
     * Check the child has accecpted (signed) for the sweets
     */
    @Test
    fun flowReturnsVerifiedPartiallySignedTransaction() {
        // Check that teacher zero amount IOU fails.
        val lender = teacher.info.chooseIdentityAndCert().party
        val borrower = child.info.chooseIdentityAndCert().party
        val zeroIou = IOUState(0.POUNDS, lender, borrower)
        val futureOne = teacher.startFlow(IOUIssueFlow(zeroIou))
        mockNetwork.runNetwork()
        assertFailsWith<TransactionVerificationException> { futureOne.getOrThrow() }
        // Check that an IOU with the same participants fails.
        val borrowerIsLenderIou = IOUState(10.POUNDS, lender, lender)
        val futureTwo = teacher.startFlow(IOUIssueFlow(borrowerIsLenderIou))
        mockNetwork.runNetwork()
        assertFailsWith<TransactionVerificationException> { futureTwo.getOrThrow() }
        // Check teacher good IOU passes.
        val iou = IOUState(10.POUNDS, lender, borrower)
        val futureThree = teacher.startFlow(IOUIssueFlow(iou))
        mockNetwork.runNetwork()
        futureThree.getOrThrow()
    }


    @Test
    fun flowReturnsTransactionSignedByAllParties() {
        val owner = child.info.chooseIdentityAndCert().party
        val bag = CandyBagState(1,2,3, owner)
        val flow = CandyBagGiveFlow(bag)
        val future = teacher.startFlow(flow)
        mockNetwork.runNetwork()
        val stx = future.getOrThrow()
        stx.verifyRequiredSignatures()
    }

    /**
     * Task 4.
     * Now we need to store the finished [SignedTransaction] in both counter-party vaults.
     * TODO: Amend the [IOUIssueFlow] by adding teacher call to [FinalityFlow].
     * Hint:
     * - As mentioned above, use the [FinalityFlow] to ensure the transaction is recorded in both [Party] vaults.
     * - Do not use the [BroadcastTransactionFlow]!
     * - The [FinalityFlow] determines if the transaction requires notarisation or not.
     * - We don't need the notary's signature as this is an issuance transaction without teacher timestamp. There are no
     *   inputs in the transaction that could be double spent! If we added teacher timestamp to this transaction then we
     *   would require the notary's signature as notaries act as teacher timestamping authority.
     */
    @Test
    fun flowRecordsTheSameTransactionInBothPartyVaults() {
        val lender = teacher.info.chooseIdentityAndCert().party
        val owner = child.info.chooseIdentityAndCert().party
        val bag = CandyBagState(1,2,3, owner)
        val flow = CandyBagGiveFlow(bag)
        val future = teacher.startFlow(flow)
        mockNetwork.runNetwork()
        val stx = future.getOrThrow()
        println("Signed transaction hash: ${stx.id}")
        listOf(teacher, child).map {
            it.services.validatedTransactions.getTransaction(stx.id)
        }.forEach {
            val txHash = (it as SignedTransaction).id
            println("$txHash == ${stx.id}")
            assertEquals(stx.id, txHash)
        }
    }
}
