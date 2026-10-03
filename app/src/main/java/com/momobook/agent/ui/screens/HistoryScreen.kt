package com.momobook.agent.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.momobook.agent.data.TransactionType
import com.momobook.agent.ui.EmptyState
import com.momobook.agent.ui.SummaryCard
import com.momobook.agent.ui.TransactionRow
import com.momobook.agent.ui.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: TransactionViewModel,
    onBack: () -> Unit,
    onOpenTransaction: (Long) -> Unit
) {
    val filters by viewModel.filters.collectAsStateWithLifecycle()
    val results by viewModel.historyResults.collectAsStateWithLifecycle()
    val summary by viewModel.historySummary.collectAsStateWithLifecycle()
    val hasFilters = filters.query.isNotBlank() || filters.type != null ||
        filters.dateKey != null || filters.minAmount != null || filters.maxAmount != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction history") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (hasFilters) {
                        IconButton(onClick = { viewModel.clearFilters() }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear filters")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = filters.query,
                    onValueChange = { value -> viewModel.updateFilters { it.copy(query = value) } },
                    placeholder = { Text("Search ID, MoMo number or name") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (filters.query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateFilters { it.copy(query = "") } }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = { },
                        label = { Text("Filters") },
                        leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null) }
                    )
                    TransactionType.entries.forEach { option ->
                        FilterChip(
                            selected = filters.type == option,
                            onClick = {
                                viewModel.updateFilters {
                                    it.copy(type = if (it.type == option) null else option)
                                }
                            },
                            label = { Text(option.label) }
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = filters.dateKey.orEmpty(),
                    onValueChange = { value ->
                        viewModel.updateFilters { it.copy(dateKey = value.ifBlank { null }) }
                    },
                    label = { Text("Date") },
                    placeholder = { Text("YYYY-MM-DD") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = filters.minAmount?.toString().orEmpty(),
                        onValueChange = { value -> viewModel.updateFilters { it.copy(minAmount = value.toDoubleOrNull()) } },
                        label = { Text("Minimum") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = filters.maxAmount?.toString().orEmpty(),
                        onValueChange = { value -> viewModel.updateFilters { it.copy(maxAmount = value.toDoubleOrNull()) } },
                        label = { Text("Maximum") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Results", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${results.size} transaction${if (results.size == 1) "" else "s"}", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                }
            }
            item { SummaryCard(title = "Matching transactions", summary = summary) }
            if (results.isEmpty()) {
                item { EmptyState("No transactions match your search.") }
            } else {
                items(results, key = { it.id }) { transaction ->
                    TransactionRow(transaction) { onOpenTransaction(transaction.id) }
                }
            }
        }
    }
}
