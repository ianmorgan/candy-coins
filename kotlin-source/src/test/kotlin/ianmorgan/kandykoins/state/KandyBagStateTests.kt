package ianmorgan.kandykoins.state

import net.corda.core.contracts.*
import net.corda.core.identity.Party
import net.corda.finance.*
import ianmorgan.kandykoins.ALICE
import ianmorgan.kandykoins.BOB
import ianmorgan.kandykoins.MEGACORP
import ianmorgan.kandykoins.MINICORP
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Test the state of teacher KandyBag
 */
class KandyBagStateTests {


    @Test
    fun hasFieldsOfCorrectType() {
        assertEquals(KandyBagState::class.java.getDeclaredField("chocolates").type, Int::class.java)
        assertEquals(KandyBagState::class.java.getDeclaredField("gobstoppers").type, Int::class.java)
        assertEquals(KandyBagState::class.java.getDeclaredField("jellybeans").type, Int::class.java)
        assertEquals(KandyBagState::class.java.getDeclaredField("owner").type, Party::class.java)
        assertEquals(KandyBagState::class.java.getDeclaredField("linearId").type, UniqueIdentifier::class.java)
    }


    @Test
    fun ownerIsParticipant() {
        val bag = KandyBagState(1, 2, 3, ALICE.party)
        assertNotEquals(bag.participants.indexOf(ALICE.party), -1)
    }


    @Test
    fun isLinearState() {
        assert(LinearState::class.java.isAssignableFrom(KandyBagState::class.java))
    }


    @Test
    fun checkIOUStateParameterOrdering() {
        val fields = KandyBagState::class.java.declaredFields
        val chocosIdx = fields.indexOf(KandyBagState::class.java.getDeclaredField("chocolates"))
        val gobsIdx = fields.indexOf(KandyBagState::class.java.getDeclaredField("gobstoppers"))
        val beansIdx = fields.indexOf(KandyBagState::class.java.getDeclaredField("jellybeans"))
        val ownerIdx = fields.indexOf(KandyBagState::class.java.getDeclaredField("owner"))
        val linearIdIdx = fields.indexOf(KandyBagState::class.java.getDeclaredField("linearId"))

        assert(chocosIdx < gobsIdx)
        assert(gobsIdx < beansIdx)
        assert(beansIdx < ownerIdx)
        assert(ownerIdx < linearIdIdx)
    }


    @Test
    fun checkFillHelperMethod() {
        val bag = KandyBagState(9, 12, 15, ALICE.party)
        val filledBag = bag.fill(1, 2, 3)

        assertEquals(10, filledBag.chocolates)
        assertEquals(14, filledBag.gobstoppers)
        assertEquals(18, filledBag.jellybeans)
    }

    @Test
    fun checkTakeHelperMethod() {
        val bag = KandyBagState(9, 12, 15, ALICE.party)
        val newBag = bag.take(3, 2, 1)

        assertEquals(6, newBag.chocolates)
        assertEquals(10, newBag.gobstoppers)
        assertEquals(14, newBag.jellybeans)
    }

}
