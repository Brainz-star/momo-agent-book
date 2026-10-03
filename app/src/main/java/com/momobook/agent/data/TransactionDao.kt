package com.momobook.agent.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("UPDATE transactions SET reference = :reference WHERE id = :id")
    suspend fun setReference(id: Long, reference: String)

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun findById(id: Long): Transaction?

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun observeById(id: Long): Flow<Transaction?>

    @Query("SELECT * FROM transactions WHERE dateKey = :dateKey ORDER BY timestamp DESC")
    fun observeByDate(dateKey: String): Flow<List<Transaction>>

    @Query(
        """
        SELECT IFNULL(SUM(CASE WHEN type = 'CASH_IN' THEN amount ELSE 0 END), 0) AS cashIn,
               IFNULL(SUM(CASE WHEN type = 'CASH_OUT' THEN amount ELSE 0 END), 0) AS cashOut,
               IFNULL(SUM(commission), 0) AS commission,
               COUNT(*) AS count
        FROM transactions WHERE dateKey = :dateKey
        """
    )
    fun observeSummary(dateKey: String): Flow<DailySummary>

    @Query(
        """
        SELECT * FROM transactions
        WHERE (:query = '' OR momoNumber LIKE '%' || :query || '%'
               OR reference LIKE '%' || :query || '%'
               OR customerName LIKE '%' || :query || '%')
          AND (:type IS NULL OR type = :type)
          AND (:dateKey IS NULL OR dateKey = :dateKey)
          AND (:minAmount IS NULL OR amount >= :minAmount)
          AND (:maxAmount IS NULL OR amount <= :maxAmount)
        ORDER BY timestamp DESC
        """
    )
    fun search(
        query: String,
        type: String?,
        dateKey: String?,
        minAmount: Double?,
        maxAmount: Double?
    ): Flow<List<Transaction>>
}
