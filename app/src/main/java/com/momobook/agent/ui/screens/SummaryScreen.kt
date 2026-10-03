package com.momobook.agent.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.momobook.agent.ui.EmptyState
import com.momobook.agent.ui.SummaryCard
import com.momobook.agent.ui.TransactionRow
import com.momobook.agent.ui.TransactionViewModel
import com.momobook.agent.util.PdfReportGenerator
import com.momobook.agent.util.formatDateKey
import com.momobook.agent.util.formatDailyReport
import com.momobook.agent.util.shiftDateKey
import com.momobook.agent.util.todayKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    viewModel: TransactionViewModel,
    onBack: () -> Unit,
    onOpenTransaction: (Long) -> Unit
) {
    val dateKey by viewModel.selectedDate.collectAsStateWithLifecycle()
    val summary by viewModel.selectedDaySummary.collectAsStateWithLifecycle()
    val transactions by viewModel.selectedDayTransactions.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isGeneratingPdf by remember { mutableStateOf(false) }

    fun shareTextReport() {
        val report = formatDailyReport(dateKey, summary)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "MoMo Agent Book - ${formatDateKey(dateKey)}")
            putExtra(Intent.EXTRA_TEXT, report)
        }
        context.startActivity(Intent.createChooser(intent, "Share daily report"))
    }

    fun generateAndSharePdf() {
        if (isGeneratingPdf) return
        isGeneratingPdf = true
        scope.launch {
            try {
                val file = withContext(Dispatchers.IO) {
                    PdfReportGenerator.generate(context, dateKey, summary, transactions)
                }
                val uri = PdfReportGenerator.uriFor(context, file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_SUBJECT, "MoMo Agent Book - ${formatDateKey(dateKey)}")
                    putExtra(Intent.EXTRA_TEXT, "Daily transaction report for ${formatDateKey(dateKey)}")
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share PDF report"))
            } catch (e: Exception) {
                Toast.makeText(context, "Could not generate the PDF report.", Toast.LENGTH_LONG).show()
            } finally {
                isGeneratingPdf = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily reports", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { viewModel.selectDate(shiftDateKey(dateKey, -1)) }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Previous day")
                        }
                        Button(onClick = { viewModel.selectDate(todayKey()) }) {
                            Text(if (dateKey == todayKey()) "Today" else formatDateKey(dateKey))
                        }
                        IconButton(onClick = { viewModel.selectDate(shiftDateKey(dateKey, 1)) }) {
                            Icon(Icons.Default.ArrowForward, contentDescription = "Next day")
                        }
                    }
                }
            }
            item { SummaryCard(title = "Daily summary • ${formatDateKey(dateKey)}", summary = summary) }
            item {
                Button(
                    onClick = { generateAndSharePdf() },
                    enabled = !isGeneratingPdf,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Text(if (isGeneratingPdf) "  Generating PDF..." else "  Generate & Share PDF")
                }
            }
            item {
                OutlinedButton(onClick = { shareTextReport() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Text("  Share as text")
                }
            }
            item {
                Text("Transactions for this day", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            if (transactions.isEmpty()) {
                item { EmptyState("No transactions recorded on this day.") }
            } else {
                items(transactions, key = { it.id }) { transaction ->
                    TransactionRow(transaction) { onOpenTransaction(transaction.id) }
                }
            }
        }
    }
}
