package com.momobook.agent.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.momobook.agent.data.AppDatabase
import com.momobook.agent.data.DailySummary
import com.momobook.agent.data.Transaction
import com.momobook.agent.data.TransactionRepository
import com.momobook.agent.data.TransactionType
import com.momobook.agent.util.normalizeGhanaMobileNumber
import com.momobook.agent.util.todayKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HistoryFilters(
    val query: String = "",
    val type: TransactionType? = null,
    val dateKey: String? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TransactionRepository(AppDatabase.get(application).transactionDao())

    private val _selectedDate = MutableStateFlow(todayKey())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _filters = MutableStateFlow(HistoryFilters())
    val filters: StateFlow<HistoryFilters> = _filters.asStateFlow()

    val todaySummary: StateFlow<DailySummary> = MutableStateFlow(todayKey())
        .flatMapLatest { repository.summaryForDate(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DailySummary())

    val todayTransactions: StateFlow<List<Transaction>> = MutableStateFlow(todayKey())
        .flatMapLatest { repository.transactionsForDate(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val selectedDaySummary: StateFlow<DailySummary> = _selectedDate
        .flatMapLatest { repository.summaryForDate(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DailySummary())

    val selectedDayTransactions: StateFlow<List<Transaction>> = _selectedDate
        .flatMapLatest { repository.transactionsForDate(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val historyResults: StateFlow<List<Transaction>> = _filters
        .flatMapLatest { filters ->
            repository.search(
                query = filters.query,
                type = filters.type,
                dateKey = filters.dateKey,
                minAmount = filters.minAmount,
                maxAmount = filters.maxAmount
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val historySummary: StateFlow<DailySummary> = combine(historyResults, _filters) { results, _ ->
        DailySummary(
            cashIn = results.filter { it.type == TransactionType.CASH_IN }.sumOf { it.amount },
            cashOut = results.filter { it.type == TransactionType.CASH_OUT }.sumOf { it.amount },
            commission = results.sumOf { it.commission },
            count = results.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DailySummary())

    fun selectDate(dateKey: String) {
        _selectedDate.value = dateKey
    }

    fun updateFilters(transform: (HistoryFilters) -> HistoryFilters) {
        _filters.value = transform(_filters.value)
    }

    fun clearFilters() {
        _filters.value = HistoryFilters()
    }

    fun addTransaction(
        type: TransactionType,
        amount: Double,
        commission: Double,
        momoNumber: String,
        customerName: String,
        note: String,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            repository.add(
                type = type,
                amount = amount,
                commission = commission,
                momoNumber = normalizeGhanaMobileNumber(momoNumber) ?: momoNumber.trim(),
                customerName = customerName.trim(),
                note = note.trim(),
                timestamp = System.currentTimeMillis()
            )
            onSaved()
        }
    }

    fun updateTransaction(transaction: Transaction, onSaved: () -> Unit) {
        viewModelScope.launch {
            repository.update(transaction)
            onSaved()
        }
    }

    fun deleteTransaction(transaction: Transaction, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            repository.delete(transaction)
            onDeleted()
        }
    }

    suspend fun findTransaction(id: Long): Transaction? = repository.findById(id)

    fun observeTransaction(id: Long): StateFlow<Transaction?> = repository.observeById(id)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: androidx.lifecycle.viewmodel.CreationExtras
            ): T {
                val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    ?: error("Application not available")
                return TransactionViewModel(application) as T
            }
        }
    }
}
