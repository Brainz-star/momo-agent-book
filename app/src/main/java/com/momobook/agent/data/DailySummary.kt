package com.momobook.agent.data

data class DailySummary(
    val cashIn: Double = 0.0,
    val cashOut: Double = 0.0,
    val commission: Double = 0.0,
    val count: Int = 0
) {
    val totalValue: Double get() = cashIn + cashOut
}
