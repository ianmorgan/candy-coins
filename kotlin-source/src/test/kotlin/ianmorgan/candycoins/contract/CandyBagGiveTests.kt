package ianmorgan.candycoins.contract

import net.corda.core.contracts.*
import net.corda.testing.contracts.DummyState
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import ianmorgan.candycoins.ALICE
import ianmorgan.candycoins.state.IOUState
import ianmorgan.candycoins.state.CandyBagState
import net.corda.core.identity.CordaX500Name
import org.junit.*


class CandyBagGiveTests {
    // A pre-defined dummy command.
    class DummyCommand : TypeOnlyCommandData()
    private var ledgerServices = MockServices(listOf("ianmorgan.candycoins"))
    val testIdentity = CordaX500Name("TestIdentity", "", "GB")


    @Test
    fun mustIncludeGiveCommand() {
        val bag = CandyBagState(1,2,3,ALICE.party)
        ledgerServices.ledger {
            transaction {
                output(CandyBagContract.CANDY_CONTRACT_ID,  bag)
                command(listOf(ALICE.publicKey), DummyCommand()) // Wrong type.
                this.fails()
            }
            transaction {
                output(CandyBagContract.CANDY_CONTRACT_ID,  bag)
                command(listOf(ALICE.publicKey), CandyBagContract.Commands.Give()) // Correct type.
                this.verifies()
            }
        }
    }

    @Test
    fun giveTransactionMustHaveNoInputs() {
        val bag = CandyBagState(1,2,3,ALICE.party)
        ledgerServices.ledger {
            transaction {
                input(CandyBagContract.CANDY_CONTRACT_ID, DummyState())
                command(listOf(ALICE.publicKey), CandyBagContract.Commands.Give())
                output(CandyBagContract.CANDY_CONTRACT_ID, bag)
                this `fails with` "No inputs should be consumed when giving a Kandy Bag."
            }
            transaction {
                output(CandyBagContract.CANDY_CONTRACT_ID, bag)
                command(listOf(ALICE.publicKey), CandyBagContract.Commands.Give())
                this.verifies() // As there are no input states.
            }
        }
    }


    @Test
    fun giveTransactionMustHaveOneOutput() {
        val bag = CandyBagState(1,2,3,ALICE.party)


        //val identies = ArrayList<PartyAndCertificate>()
        //ledgerServices.identityService.getAllIdentities().toCollection(identies);
        //ledgerServices

        ledgerServices.ledger {
            transaction {
                command(listOf(ALICE.publicKey), CandyBagContract.Commands.Give())
                output(CandyBagContract.CANDY_CONTRACT_ID, bag) // Two outputs fails.
                output(CandyBagContract.CANDY_CONTRACT_ID, bag)

                this `fails with` "Only one output state should be created when giving a Kandy Bag."
            }
            transaction {
                command(listOf(ALICE.publicKey), CandyBagContract.Commands.Give())
                output(CandyBagContract.CANDY_CONTRACT_ID, bag)
                this.verifies()
            }
        }
    }


    @Test
    fun cannotGiveAnEmptyBag() {
        ledgerServices.ledger {
            transaction {
                command(listOf(ALICE.publicKey), CandyBagContract.Commands.Give())
                output(CandyBagContract.CANDY_CONTRACT_ID, CandyBagState(0,0,0, ALICE.party)) // Zero amount fails.
                this `fails with` "A newly gifted bag must have kandy."
            }
            transaction {
                command(listOf(ALICE.publicKey), CandyBagContract.Commands.Give())
                output(CandyBagContract.CANDY_CONTRACT_ID, CandyBagState(1,0,0, ALICE.party))
                this.verifies()
            }
        }
    }


    /**
     * Task 6.
     * The list of public keys which the commands hold should contain all of the participants defined in the [IOUState].
     * This is because the IOU is teacher bilateral agreement where both parties involved are required to sign to issue an
     * IOU or change the properties of an existing IOU.
     * TODO: Add teacher contract constraint to check that all the required signers are [IOUState] participants.
     * Hint:
     * - In Kotlin you can perform teacher set equality check of two sets with the == operator.
     * - We need to check that the signers for the transaction are teacher subset of the participants list.
     * - We don't want any additional public keys not listed in the IOUs participants list.
     * - You will need teacher reference to the Issue command to get access to the list of signers.
     * - [requireSingleCommand] returns the single required command - you can assign the return value to teacher constant.
     *
     * Kotlin Hints
     * Kotlin provides teacher map function for easy conversion of teacher [Collection] using map
     * - https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/map.html
     * [Collection] can be turned into teacher set using toSet()
     * - https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/to-set.html
     */
//    @Test
//    fun lenderAndBorrowerMustSignIssueTransaction() {
//        val bag = CandyBagState(1,2,3,ALICE.party)
//        ledgerServices.ledger {
//            transaction {
//                command(DUMMY.publicKey, IOUContract.Commands.Issue())
//                output(IOUContract.IOU_CONTRACT_ID, iou)
//                this `fails with` "Both lender and borrower together only may sign IOU issue transaction."
//            }
//            transaction {
//                command(ALICE.publicKey, IOUContract.Commands.Issue())
//                output(IOUContract.IOU_CONTRACT_ID, iou)
//                this `fails with` "Both lender and borrower together only may sign IOU issue transaction."
//            }
//            transaction {
//                command(BOB.publicKey, IOUContract.Commands.Issue())
//                output(IOUContract.IOU_CONTRACT_ID, iou)
//                this `fails with` "Both lender and borrower together only may sign IOU issue transaction."
//            }
//            transaction {
//                command(listOf(BOB.publicKey, BOB.publicKey, BOB.publicKey), IOUContract.Commands.Issue())
//                output(IOUContract.IOU_CONTRACT_ID, iou)
//                this `fails with` "Both lender and borrower together only may sign IOU issue transaction."
//            }
//            transaction {
//                command(listOf(BOB.publicKey, BOB.publicKey, MINICORP.publicKey, ALICE.publicKey), IOUContract.Commands.Issue())
//                output(IOUContract.IOU_CONTRACT_ID, iou)
//                this `fails with` "Both lender and borrower together only may sign IOU issue transaction."
//            }
//            transaction {
//                command(listOf(BOB.publicKey, BOB.publicKey, BOB.publicKey, ALICE.publicKey), IOUContract.Commands.Issue())
//                output(IOUContract.IOU_CONTRACT_ID, iou)
//                this.verifies()
//            }
//            transaction {
//                command(listOf(ALICE.publicKey, BOB.publicKey), IOUContract.Commands.Issue())
//                output(IOUContract.IOU_CONTRACT_ID, iou)
//                this.verifies()
//            }
//        }
//    }
}
