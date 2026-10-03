package com.momobook.agent.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType(val label: String) {
    CASH_IN("Cash-In"),
    CASH_OUT("Cash-Out")
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reference: String = "",
    val type: TransactionType,
    val amount: Double,
    val commission: Double,
    val momoNumber: String = "",
    val customerName: String = "",
    val note: String = "",
    val timestamp: Long,
    val dateKey: String
)
