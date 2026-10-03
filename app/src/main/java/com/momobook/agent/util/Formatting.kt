package com.momobook.agent.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private const val DATE_KEY_PATTERN = "yyyy-MM-dd"

fun dateKeyOf(timestamp: Long): String =
    SimpleDateFormat(DATE_KEY_PATTERN, Locale.US).format(Date(timestamp))

fun todayKey(): String = dateKeyOf(System.currentTimeMillis())

fun timestampOfDateKey(dateKey: String): Long =
    SimpleDateFormat(DATE_KEY_PATTERN, Locale.US).parse(dateKey)?.time ?: System.currentTimeMillis()

fun formatDateKey(dateKey: String): String =
    SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(timestampOfDateKey(dateKey)))

fun formatTime(timestamp: Long): String =
    SimpleDateFormat("HH:mm", Locale.US).format(Date(timestamp))

fun formatDateTime(timestamp: Long): String =
    SimpleDateFormat("dd MMM yyyy HH:mm", Locale.US).format(Date(timestamp))

fun formatMoney(amount: Double): String = "GH\u20B5" + String.format(Locale.US, "%,.2f", amount)

fun shiftDateKey(dateKey: String, days: Int): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestampOfDateKey(dateKey)
    calendar.add(Calendar.DAY_OF_YEAR, days)
    return dateKeyOf(calendar.timeInMillis)
}
