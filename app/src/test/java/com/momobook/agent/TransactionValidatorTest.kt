package com.momobook.agent

import com.momobook.agent.data.DailySummary
import com.momobook.agent.data.TransactionRepository
import com.momobook.agent.util.TransactionInput
import com.momobook.agent.util.TransactionValidator
import com.momobook.agent.util.dateKeyOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionValidatorTest {

    private fun input(
        typeSelected: Boolean = true,
        amount: String = "100",
        commission: String = "1",
        momoNumber: String = ""
    ) = TransactionInput(typeSelected, amount, commission, momoNumber)

    @Test
    fun `valid input passes`() {
        val result = TransactionValidator.validate(input(momoNumber = "0241234567"))
        assertTrue(result.isValid)
    }

    @Test
    fun `empty amount is rejected`() {
        assertNotNull(TransactionValidator.validate(input(amount = "")).amountError)
    }

    @Test
    fun `non numeric amount is rejected`() {
        assertNotNull(TransactionValidator.validate(input(amount = "abc")).amountError)
    }

    @Test
    fun `zero and negative amounts are rejected`() {
        assertNotNull(TransactionValidator.validate(input(amount = "0")).amountError)
        assertNotNull(TransactionValidator.validate(input(amount = "-5")).amountError)
    }

    @Test
    fun `missing type is rejected`() {
        assertNotNull(TransactionValidator.validate(input(typeSelected = false)).typeError)
    }

    @Test
    fun `negative commission is rejected and empty commission allowed`() {
        assertNotNull(TransactionValidator.validate(input(commission = "-1")).commissionError)
        assertNull(TransactionValidator.validate(input(commission = "")).commissionError)
    }

    @Test
    fun `commission larger than amount is rejected`() {
        assertNotNull(TransactionValidator.validate(input(amount = "10", commission = "20")).commissionError)
    }

    @Test
    fun `missing, short or alphabetic momo numbers are rejected`() {
        assertNotNull(TransactionValidator.validate(input(momoNumber = "")).momoNumberError)
        assertNotNull(TransactionValidator.validate(input(momoNumber = "0241")).momoNumberError)
        assertNotNull(TransactionValidator.validate(input(momoNumber = "024ABC4567")).momoNumberError)
    }

    @Test
    fun `international Ghana number is accepted`() {
        assertTrue(TransactionValidator.validate(input(momoNumber = "+233241234567")).isValid)
    }
}

class SummaryAndReferenceTest {

    @Test
    fun `total value adds cash in and cash out`() {
        val summary = DailySummary(cashIn = 300.0, cashOut = 500.0, commission = 8.0, count = 3)
        assertEquals(800.0, summary.totalValue, 0.001)
    }

    @Test
    fun `reference is zero padded`() {
        assertEquals("TXN-000124", TransactionRepository.referenceFor(124))
    }

    @Test
    fun `date key uses iso format`() {
        assertTrue(dateKeyOf(System.currentTimeMillis()).matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }
}
