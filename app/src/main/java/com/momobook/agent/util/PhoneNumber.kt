package com.momobook.agent.util

/**
 * Normalizes common Ghanaian mobile-number formats to 0XXXXXXXXX.
 */
fun normalizeGhanaMobileNumber(raw: String): String? {
    val digits = raw.filter(Char::isDigit)
    return when {
        digits.matches(Regex("0[235][0-9]{8}")) -> digits
        digits.matches(Regex("233[235][0-9]{8}")) -> "0" + digits.drop(3)
        else -> null
    }
}
