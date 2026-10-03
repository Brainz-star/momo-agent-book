package com.momobook.agent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.momobook.agent.ui.TransactionViewModel
import com.momobook.agent.ui.screens.DashboardScreen
import com.momobook.agent.ui.screens.HistoryScreen
import com.momobook.agent.ui.screens.SummaryScreen
import com.momobook.agent.ui.screens.TransactionFormScreen
import com.momobook.agent.ui.screens.TransactionDetailsScreen
import com.momobook.agent.ui.theme.MoMoAgentBookTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoMoAgentBookTheme {
                MoMoAgentApp()
            }
        }
    }
}

@Composable
fun MoMoAgentApp() {
    val navController = rememberNavController()
    val viewModel: TransactionViewModel = viewModel(factory = TransactionViewModel.Factory)

    Surface(
        modifier = androidx.compose.ui.Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(navController = navController, startDestination = "dashboard") {
            composable("dashboard") {
                DashboardScreen(
                    viewModel = viewModel,
                    onAddTransaction = { navController.navigate("form/0") },
                    onOpenHistory = { navController.navigate("history") },
                    onOpenSummary = { navController.navigate("summary") },
                    onOpenTransaction = { id -> navController.navigate("details/" + id) }
                )
            }
            composable(
                route = "details/{transactionId}",
                arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("transactionId") ?: 0L
                TransactionDetailsScreen(
                    viewModel = viewModel,
                    transactionId = id,
                    onBack = { navController.popBackStack() },
                    onEdit = { transactionId -> navController.navigate("form/" + transactionId) }
                )
            }
            composable(
                route = "form/{transactionId}",
                arguments = listOf(navArgument("transactionId") { type = NavType.LongType })
            ) { entry ->
                val id = entry.arguments?.getLong("transactionId") ?: 0L
                TransactionFormScreen(
                    viewModel = viewModel,
                    transactionId = id.takeIf { it > 0 },
                    onDone = { navController.popBackStack() }
                )
            }
            composable("history") {
                HistoryScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onOpenTransaction = { id -> navController.navigate("details/" + id) }
                )
            }
            composable("summary") {
                SummaryScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onOpenTransaction = { id -> navController.navigate("details/" + id) }
                )
            }
        }
    }
}
