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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.momobook.agent.data.Transaction
import com.momobook.agent.data.TransactionType
import com.momobook.agent.ui.EmptyState
import com.momobook.agent.ui.TransactionViewModel
import com.momobook.agent.util.formatDateTime
import com.momobook.agent.util.formatMoney

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsScreen(
    viewModel: TransactionViewModel,
    transactionId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val transactionFlow = remember(transactionId) { viewModel.observeTransaction(transactionId) }
    val transaction by transactionFlow.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction details", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    transaction?.let {
                        IconButton(onClick = { onEdit(it.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit transaction")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete transaction")
                        }
                    }
                }
            )
        }
    ) { padding ->
        val current = transaction
        if (current == null) {
            Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                EmptyState("Transaction not found.")
            }
        } else {
            val isCashIn = current.type == TransactionType.CASH_IN
            val accent = if (isCashIn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(current.type.label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = accent)
                        Text(formatMoney(current.amount), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text("Ref: ${current.reference}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text("Transaction information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        DetailRow("Customer MoMo Number", current.momoNumber)
                        DetailRow("Customer Name", current.customerName.ifBlank { "Not provided" })
                        DetailRow("Commission", formatMoney(current.commission))
                        DetailRow("Date & Time", formatDateTime(current.timestamp))
                        DetailRow("Note", current.note.ifBlank { "No note" })
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { onEdit(current.id) }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Text("  Edit")
                    }
                    OutlinedButton(onClick = { showDeleteDialog = true }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Text("  Delete")
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        val current = transaction
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete transaction?") },
            text = { Text("This transaction will be permanently removed from the local records.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    if (current != null) viewModel.deleteTransaction(current, onBack)
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}
