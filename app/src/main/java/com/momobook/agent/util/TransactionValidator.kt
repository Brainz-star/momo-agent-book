package com.momobook.agent.util

data class TransactionInput(
    val typeSelected: Boolean,
    val amount: String,
    val commission: String,
    val momoNumber: String
)

data class ValidationResult(
    val amountError: String? = null,
    val commissionError: String? = null,
    val momoNumberError: String? = null,
    val typeError: String? = null
) {
    val isValid: Boolean
        get() = amountError == null && commissionError == null &&
            momoNumberError == null && typeError == null
}

object TransactionValidator {

    fun validate(input: TransactionInput): ValidationResult {
        val amount = input.amount.trim().toDoubleOrNull()
        val commission = input.commission.trim().let {
            if (it.isEmpty()) 0.0 else it.toDoubleOrNull()
        }
        val momoNumber = input.momoNumber.trim()

        return ValidationResult(
            typeError = if (input.typeSelected) null else "Select a transaction type",
            amountError = when {
                input.amount.isBlank() -> "Enter the transaction amount"
                amount == null -> "Amount must be a number"
                !amount.isFinite() -> "Enter a valid amount"
                amount <= 0 -> "Amount must be greater than zero"
                else -> null
            },
            commissionError = when {
                commission == null -> "Commission must be a number"
                !commission.isFinite() -> "Enter a valid commission"
                commission < 0 -> "Commission cannot be negative"
                amount != null && commission > amount -> "Commission cannot exceed the amount"
                else -> null
            },
            momoNumberError = when {
                momoNumber.isEmpty() -> "Customer MoMo number is required"
                normalizeGhanaMobileNumber(momoNumber) == null ->
                    "Enter a valid Ghana mobile number, e.g. 0241234567"
                else -> null
            }
        )
    }
}
