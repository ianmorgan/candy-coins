package ianmorgan.kandykoins.state

import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party


/**
 * The Kandy Bag State object, with the following properties:
 * - [chocolates] The number of chocolate bars
 * - [gobstoppers] The number of Gob Stoppers.
 * - [jellybeans] The number of Jelly Beans.
 * - [linearId] A unique id shared by all LinearState states representing the same agreement throughout history within
 *   the vaults of all parties. Verify methods should check that one input and one output share the id in a transaction,
 *   except at issuance/termination.
 */
data class KandyBagState(val chocolates: Int,
                         val gobstoppers: Int,
                         val jellybeans: Int,
                         val owner: Party,
                         override val linearId: UniqueIdentifier = UniqueIdentifier()) : LinearState {
    /**
     *  This property holds a list of the nodes which can "use" this state in a valid transaction.
     */
    override val participants: List<Party> get() = listOf(owner)


    /**
     * Fill this bag with some more kandy
     */
    fun fill(chocs: Int, gobs: Int, beans: Int) = copy(chocolates = chocs + this.chocolates,
            gobstoppers = gobs + this.gobstoppers,
            jellybeans = beans + this.jellybeans)

    /**
     * Takes some kandy from the bag.
     */
    fun take(chocs: Int, gobs: Int, beans: Int) = copy(chocolates = this.chocolates - chocs,
            gobstoppers = this.gobstoppers - gobs,
            jellybeans = this.jellybeans - beans)

    fun hasEnough (chocs: Int, gobs: Int, beans: Int) : Boolean {
        return (chocolates >= chocs) && (gobstoppers >= gobs) && (jellybeans >= beans)
    }

    fun isEmpty(): Boolean {
        return (chocolates == 0) && (gobstoppers == 0) && (jellybeans == 0)
    }
}