package ianmorgan.candycoins.contract

import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction
import ianmorgan.candycoins.state.CandyBagState


/**
 * The things we can do with a kandy bag
 */
@LegalProseReference(uri = "<prose_contract_uri>")
class CandyBagContract : Contract {
    companion object {
        @JvmStatic
        val CANDY_CONTRACT_ID = "ianmorgan.candycoins.contract.CandyBagContract"
    }

    /**
     * Add any commands required for this contract as classes within this interface.
     * It is useful to encapsulate your commands inside an interface, so you can use the [requireSingleCommand]
     * function to check for a number of commands which implement this interface.
     */
    interface Commands : CommandData {

        // Kandy comes into existence when someone gives out a bag.
        class Give : TypeOnlyCommandData(), Commands

        // I can eat some or all of my kandy. If there is nothing left , the bag is no more
        data class Eat (val chocolates: Int,
                        val gobstoppers: Int,
                        val jellybeans: Int) : Commands

        // I can offer my bag  or all of my kandy to someone else to eat.
        class Offer : TypeOnlyCommandData(), Commands

        // I can trade some of my kandy with someone else. The relative values of chocs, gobstoppers and jellybeans
        // must be obeyed
        class Trade : TypeOnlyCommandData(), Commands


    }


    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        when (command.value) {
            is Commands.Give -> requireThat {
                "No inputs should be consumed when giving a Kandy Bag." using (tx.inputs.isEmpty())
                "Only one output state should be created when giving a Kandy Bag." using (tx.outputs.size == 1)
                val bag = tx.outputStates.single() as CandyBagState
               "A newly gifted bag must have kandy." using (bag.isEmpty() == false)
               // "The lender and borrower cannot have the same identity." using (iou.borrower != iou.lender)
               // "Both lender and borrower together only may sign IOU issue transaction." using
               //         (command.signers.toSet() == iou.participants.map { it.owningKey }.toSet())
            }

            is Commands.Eat -> {
                requireThat {  "A Kandy Bag eat transaction should only consume one input state." using (tx.inputs.size == 1)}
                requireThat {  "A Kandy Bag eat transaction should create no more than one output state." using (tx.outputs.size <= 1)}

                val bag = tx.inputStates.single() as CandyBagState
                val cmd = command.value as Commands.Eat

                requireThat { "Only the owner may eat some Kandy." using (command.signers.toSet() == bag.participants.map { it.owningKey }.toSet())}

                if (bag.hasEnough(cmd.chocolates,cmd.gobstoppers,cmd.jellybeans)){
                    val updatedBag = bag.take(cmd.chocolates,cmd.gobstoppers,cmd.jellybeans)
                    if (updatedBag.isEmpty()){
                        requireThat {  "A Kandy Bag eat transaction should not create an output if bag is emptied." using (tx.outputs.isEmpty())}
                    }
                    else {
                        requireThat {  "A Kandy Bag eat transaction should create an output state." using (tx.outputs.size <= 1)}

                        val out = tx.outputStates.single() as CandyBagState
                        requireThat {  "A Kandy Bag eat transaction should leave the correct amount in the output state." using (out == updatedBag)}

                    }
                }
                else {
                    requireThat {  "A Kandy Bag must have sufficient contents to eat." using (false)}
                }




//                "No inputs should be consumed when giving a Kandy Bag." using (tx.inputs.isEmpty())
//                "Only one output state should be created when giving a Kandy Bag." using (tx.outputs.size == 1)
//                val bag = tx.outputStates.single() as CandyBagState
//                "A newly gifted bag must have kandy." using (bag.isEmpty() == false)
//                // "The lender and borrower cannot have the same identity." using (iou.borrower != iou.lender)
//                // "Both lender and borrower together only may sign IOU issue transaction." using
//                //         (command.signers.toSet() == iou.participants.map { it.owningKey }.toSet())
            }

        }
    }
}
