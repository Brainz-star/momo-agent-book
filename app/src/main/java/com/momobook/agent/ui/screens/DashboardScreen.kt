package com.momobook.agent.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.momobook.agent.ui.EmptyState
import com.momobook.agent.ui.StatCard
import com.momobook.agent.ui.TransactionRow
import com.momobook.agent.ui.TransactionViewModel
import com.momobook.agent.util.formatDateKey
import com.momobook.agent.util.formatMoney
import com.momobook.agent.util.todayKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: TransactionViewModel,
    onAddTransaction: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSummary: () -> Unit,
    onOpenTransaction: (Long) -> Unit
) {
    val summary by viewModel.todaySummary.collectAsStateWithLifecycle()
    val transactions by viewModel.todayTransactions.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("MoMo Agent Book", fontWeight = FontWeight.SemiBold) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransaction) {
                Icon(Icons.Default.Add, contentDescription = "Record transaction")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Good day", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        formatDateKey(todayKey()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Cash-In", formatMoney(summary.cashIn), Modifier.weight(1f))
                    StatCard("Cash-Out", formatMoney(summary.cashOut), Modifier.weight(1f))
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Commission", formatMoney(summary.commission), Modifier.weight(1f))
                    StatCard("Transactions", summary.count.toString(), Modifier.weight(1f))
                }
            }
            item {
                Button(onClick = onAddTransaction, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("  Record a transaction")
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onOpenHistory, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.History, contentDescription = null)
                        Text("  History")
                    }
                    OutlinedButton(onClick = onOpenSummary, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Assessment, contentDescription = null)
                        Text("  Reports")
                    }
                }
            }
            item {
                Text("Today's transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            if (transactions.isEmpty()) {
                item { EmptyState("No transactions recorded today yet. Your activity will appear here.") }
            } else {
                items(transactions, key = { it.id }) { transaction ->
                    TransactionRow(transaction) { onOpenTransaction(transaction.id) }
                }
            }
        }
    }
}
