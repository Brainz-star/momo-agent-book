package com.momobook.agent.data

import com.momobook.agent.util.dateKeyOf
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val dao: TransactionDao) {

    fun transactionsForDate(dateKey: String): Flow<List<Transaction>> = dao.observeByDate(dateKey)

    fun summaryForDate(dateKey: String): Flow<DailySummary> = dao.observeSummary(dateKey)

    fun search(
        query: String = "",
        type: TransactionType? = null,
        dateKey: String? = null,
        minAmount: Double? = null,
        maxAmount: Double? = null
    ): Flow<List<Transaction>> = dao.search(query.trim(), type?.name, dateKey, minAmount, maxAmount)

    suspend fun findById(id: Long): Transaction? = dao.findById(id)

    fun observeById(id: Long): Flow<Transaction?> = dao.observeById(id)

    suspend fun add(
        type: TransactionType,
        amount: Double,
        commission: Double,
        momoNumber: String,
        customerName: String,
        note: String,
        timestamp: Long
    ): Long {
        val id = dao.insert(
            Transaction(
                type = type,
                amount = amount,
                commission = commission,
                momoNumber = momoNumber,
                customerName = customerName,
                note = note,
                timestamp = timestamp,
                dateKey = dateKeyOf(timestamp)
            )
        )
        dao.setReference(id, referenceFor(id))
        return id
    }

    suspend fun update(transaction: Transaction) =
        dao.update(transaction.copy(dateKey = dateKeyOf(transaction.timestamp)))

    suspend fun delete(transaction: Transaction) = dao.delete(transaction)

    companion object {
        fun referenceFor(id: Long): String = "TXN-%06d".format(id)
    }
}
