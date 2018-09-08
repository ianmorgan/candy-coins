package ianmorgan.kandykoins.contract

import ianmorgan.kandykoins.*
import net.corda.core.contracts.*
import net.corda.finance.*
import net.corda.testing.contracts.DummyState
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import ianmorgan.kandykoins.state.IOUState
import ianmorgan.kandykoins.state.KandyBagState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import org.junit.*


class KandyBagEatTests {
    class DummyCommand : TypeOnlyCommandData()
    class DummyState : ContractState {
        override val participants: List<AbstractParty> get() = listOf()
    }

    private var ledgerServices = MockServices(listOf("ianmorgan.kandykoins"))

    @Test
    fun mustHaveOneInputAndOneOutput() {
        val bag = KandyBagState(5, 5, 5, ALICE.party)
        ledgerServices.ledger {
            transaction {
                input(KandyBagContract::class.java.name, bag)
                input(KandyBagContract::class.java.name, DummyState())
                output(KandyBagContract::class.java.name, bag.take(4, 3, 2))
                command(listOf(ALICE.publicKey), eat(1, 2, 3))
                this `fails with` "A Kandy Bag eat transaction should only consume one input state."
            }
            transaction {
                output(KandyBagContract::class.java.name, bag.take(4, 3, 2))
                command(listOf(ALICE.publicKey), eat(1, 2, 3))
                this `fails with` "A Kandy Bag eat transaction should only consume one input state."
            }
            transaction {
                input(KandyBagContract::class.java.name, DummyState())
                output(KandyBagContract::class.java.name, bag.take(4, 3, 2))
                output(KandyBagContract::class.java.name, bag.take(4, 3, 2))
                command(listOf(ALICE.publicKey), eat(1, 2, 3))
                this `fails with` "A Kandy Bag eat transaction should create no more than one output state."
            }
            transaction {
                input(KandyBagContract::class.java.name, bag)
                output(KandyBagContract::class.java.name, KandyBagState(4, 3, 2, ALICE.party, bag.linearId))
                command(listOf(ALICE.publicKey), eat(1, 2, 3))
                this.verifies()
            }
        }
    }

    @Test
    fun cannotEatTooMuchKandy() {
        val bag = KandyBagState(5, 5, 5, ALICE.party)
        ledgerServices.ledger {
            transaction {
                input(KandyBagContract::class.java.name, bag)
                //output(KandyBagContract::class.java.name, net.corda.testing.contracts.DummyState())
                command(listOf(ALICE.publicKey), eat(10, 10, 10))
                this `fails with` "A Kandy Bag must have sufficient contents to eat."
            }
            transaction {
                input(KandyBagContract::class.java.name, bag)
                output(KandyBagContract::class.java.name, KandyBagState(4, 3, 2, ALICE.party, bag.linearId))
                command(listOf(ALICE.publicKey), eat(1, 2, 3))
                this.verifies()
            }
        }
    }


    @Test
    fun outputBagMustMatchQuantityEaten() {
        val bag = KandyBagState(5, 5, 5, ALICE.party)
        ledgerServices.ledger {
            transaction {
                input(KandyBagContract::class.java.name, bag)
                output(KandyBagContract::class.java.name, bag.take(3, 2, 1))
                command(listOf(ALICE.publicKey), eat(1, 2, 3))
                this `fails with` "A Kandy Bag eat transaction should leave the correct amount in the output state."
            }
            transaction {
                input(KandyBagContract::class.java.name, bag)
                output(KandyBagContract::class.java.name, KandyBagState(4, 3, 2, ALICE.party, bag.linearId))
                command(listOf(ALICE.publicKey), eat(1, 2, 3))
                this.verifies()
            }
        }
    }

    @Test
    fun onlyTheOwnerMayEatKandy() {
        val bag = KandyBagState(5, 5, 5, ALICE.party)
        ledgerServices.ledger {
            transaction {
                input(KandyBagContract::class.java.name, bag)
                output(KandyBagContract::class.java.name, bag.take(4, 3, 2))
                command(listOf(BOB.publicKey), eat(1, 2, 3))
                this `fails with` "Only the owner may eat some Kandy."
            }
            transaction {
                input(KandyBagContract::class.java.name, bag)
                output(KandyBagContract::class.java.name, KandyBagState(4, 3, 2, ALICE.party, bag.linearId))
                command(listOf(ALICE.publicKey), eat(1, 2, 3))
                this.verifies()
            }
//            transaction {
//                input(IOUContract::class.java.name, iou)
//                output(IOUContract::class.java.name, iou.withNewLender(CHARLIE.party))
//                command(listOf(ALICE.publicKey, CHARLIE.publicKey), IOUContract.Commands.Transfer())
//                this `fails with` "The borrower, old lender and new lender only must sign an IOU transfer transaction"
//            }
//            transaction {
//                input(IOUContract::class.java.name, iou)
//                output(IOUContract::class.java.name, iou.withNewLender(CHARLIE.party))
//                command(listOf(BOB.publicKey, CHARLIE.publicKey), IOUContract.Commands.Transfer())
//                this `fails with` "The borrower, old lender and new lender only must sign an IOU transfer transaction"
//            }
//            transaction {
//                input(IOUContract::class.java.name, iou)
//                output(IOUContract::class.java.name, iou.withNewLender(CHARLIE.party))
//                command(listOf(ALICE.publicKey, BOB.publicKey, MINICORP.publicKey), IOUContract.Commands.Transfer())
//                this `fails with` "The borrower, old lender and new lender only must sign an IOU transfer transaction"
//            }
//            transaction {
//                input(IOUContract::class.java.name, iou)
//                output(IOUContract::class.java.name, iou.withNewLender(CHARLIE.party))
//                command(listOf(ALICE.publicKey, BOB.publicKey, CHARLIE.publicKey, MINICORP.publicKey), IOUContract.Commands.Transfer())
//                this `fails with` "The borrower, old lender and new lender only must sign an IOU transfer transaction"
//            }
//            transaction {
//                input(IOUContract::class.java.name, iou)
//                output(IOUContract::class.java.name, iou.withNewLender(CHARLIE.party))
//                command(listOf(ALICE.publicKey, BOB.publicKey, CHARLIE.publicKey), IOUContract.Commands.Transfer())
//                this.verifies()
//            }
        }
    }

    //Only the owner may eat some Kandy.

    fun eat(chocs: Int, gobs: Int, beans: Int) = KandyBagContract.Commands.Eat(chocs, gobs, beans)
}
