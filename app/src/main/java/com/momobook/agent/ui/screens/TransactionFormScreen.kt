package com.momobook.agent.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.momobook.agent.data.Transaction
import com.momobook.agent.data.TransactionType
import com.momobook.agent.ui.TransactionViewModel
import com.momobook.agent.util.TransactionInput
import com.momobook.agent.util.TransactionValidator
import com.momobook.agent.util.formatDateTime
import com.momobook.agent.util.normalizeGhanaMobileNumber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormScreen(
    viewModel: TransactionViewModel,
    transactionId: Long?,
    onDone: () -> Unit
) {
    var existing by remember { mutableStateOf<Transaction?>(null) }
    var type by remember { mutableStateOf<TransactionType?>(null) }
    var amount by remember { mutableStateOf("") }
    var commission by remember { mutableStateOf("") }
    var momoNumber by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var errors by remember { mutableStateOf(com.momobook.agent.util.ValidationResult()) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(transactionId) {
        if (transactionId != null) {
            viewModel.findTransaction(transactionId)?.let { transaction ->
                existing = transaction
                type = transaction.type
                amount = transaction.amount.toString()
                commission = transaction.commission.toString()
                momoNumber = transaction.momoNumber
                customerName = transaction.customerName
                note = transaction.note
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) "Record transaction" else "Edit transaction") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (existing != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete transaction")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Transaction type", fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TransactionType.entries.forEach { option ->
                    FilterChip(
                        selected = type == option,
                        onClick = { type = option },
                        label = { Text(option.label) }
                    )
                }
            }
            errors.typeError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (GH\u20B5)") },
                isError = errors.amountError != null,
                supportingText = { errors.amountError?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = commission,
                onValueChange = { commission = it },
                label = { Text("Commission (GH\u20B5)") },
                isError = errors.commissionError != null,
                supportingText = { errors.commissionError?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = momoNumber,
                onValueChange = { momoNumber = it },
                label = { Text("Customer MoMo number *") },
                isError = errors.momoNumberError != null,
                supportingText = { errors.momoNumberError?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = customerName,
                onValueChange = { customerName = it },
                label = { Text("Customer name (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            existing?.let {
                Text(
                    "Reference " + it.reference + "  \u2022  " + formatDateTime(it.timestamp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (existing == null) {
                Text(
                    "Date, time and transaction ID are generated automatically.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    val result = TransactionValidator.validate(
                        TransactionInput(
                            typeSelected = type != null,
                            amount = amount,
                            commission = commission,
                            momoNumber = momoNumber
                        )
                    )
                    errors = result
                    if (!result.isValid) return@Button
                    val selectedType = type ?: return@Button
                    val amountValue = amount.trim().toDouble()
                    val commissionValue = commission.trim().ifEmpty { "0" }.toDouble()
                    val current = existing
                    if (current == null) {
                        viewModel.addTransaction(
                            type = selectedType,
                            amount = amountValue,
                            commission = commissionValue,
                            momoNumber = momoNumber,
                            customerName = customerName,
                            note = note,
                            onSaved = onDone
                        )
                    } else {
                        viewModel.updateTransaction(
                            current.copy(
                                type = selectedType,
                                amount = amountValue,
                                commission = commissionValue,
                                momoNumber = normalizeGhanaMobileNumber(momoNumber) ?: momoNumber.trim(),
                                customerName = customerName.trim(),
                                note = note.trim()
                            ),
                            onSaved = onDone
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (existing == null) "Save transaction" else "Update transaction")
            }
        }
    }

    if (showDeleteDialog) {
        val current = existing
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete transaction?") },
            text = { Text("This transaction will be removed from your records.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    if (current != null) viewModel.deleteTransaction(current, onDone)
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}
