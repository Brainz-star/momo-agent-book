package com.momobook.agent.util

import com.momobook.agent.data.DailySummary

fun formatDailyReport(dateKey: String, summary: DailySummary): String = buildString {
    appendLine("MoMo Agent Book — Daily Report")
    appendLine("Date: ${formatDateKey(dateKey)}")
    appendLine()
    appendLine("Total Cash-In: ${formatMoney(summary.cashIn)}")
    appendLine("Total Cash-Out: ${formatMoney(summary.cashOut)}")
    appendLine("Total Transaction Value: ${formatMoney(summary.totalValue)}")
    appendLine("Total Commission: ${formatMoney(summary.commission)}")
    appendLine("Number of Transactions: ${summary.count}")
}
