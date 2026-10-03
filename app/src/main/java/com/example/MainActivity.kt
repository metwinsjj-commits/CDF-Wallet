package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.BottomNavBar
import com.example.ui.components.TopBar
import com.example.ui.components.TopUpDialog
import com.example.ui.components.TransactionReceiptModal
import com.example.ui.screens.BillsAndServicesScreen
import com.example.ui.screens.ExchangeScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileSecurityScreen
import com.example.ui.screens.SavingsScreen
import com.example.ui.screens.SendReceiveScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ScreenTab
import com.example.ui.viewmodel.WalletViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    private val viewModel: WalletViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CDFWalletApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CDFWalletApp(viewModel: WalletViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }

    val accounts by viewModel.walletAccounts.collectAsStateWithLifecycle()
    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val savingsGoals by viewModel.savingsGoals.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isBalanceVisible by viewModel.isBalanceVisible.collectAsStateWithLifecycle()
    val activeTxDetail by viewModel.activeTransactionDetail.collectAsStateWithLifecycle()
    val rateSource by viewModel.selectedRateSource.collectAsStateWithLifecycle()

    var showTopUpDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopBar(
                isBalanceVisible = isBalanceVisible,
                onToggleBalanceVisibility = { viewModel.toggleBalanceVisibility() },
                onOpenProfile = { viewModel.setScreen(ScreenTab.PROFILE) },
                onScanQrClick = { viewModel.setScreen(ScreenTab.SEND_RECEIVE) },
                onSearchClick = { showTopUpDialog = true },
                onNotificationClick = {
                    transactions.firstOrNull()?.let {
                        viewModel.selectTransactionForReceipt(it)
                    }
                }
            )
        },
        bottomBar = {
            BottomNavBar(
                currentTab = currentScreen,
                onTabSelected = { viewModel.setScreen(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                ScreenTab.HOME -> {
                    HomeScreen(
                        accounts = accounts,
                        transactions = transactions,
                        isBalanceVisible = isBalanceVisible,
                        onNavigate = { viewModel.setScreen(it) },
                        onOpenTopUp = { showTopUpDialog = true },
                        onSelectTransaction = { viewModel.selectTransactionForReceipt(it) }
                    )
                }

                ScreenTab.SEND_RECEIVE -> {
                    SendReceiveScreen(
                        accounts = accounts,
                        contacts = contacts,
                        onBack = { viewModel.setScreen(ScreenTab.HOME) },
                        onSendMoney = { phone, name, op, amount, curr, note ->
                            viewModel.sendMoney(phone, name, op, amount, curr, note)
                        }
                    )
                }

                ScreenTab.EXCHANGE -> {
                    ExchangeScreen(
                        accounts = accounts,
                        rateSource = rateSource,
                        onSetRateSource = { viewModel.setRateSource(it) },
                        bccRate = viewModel.bccOfficialRate,
                        parallelRate = viewModel.parallelMarketRate,
                        onBack = { viewModel.setScreen(ScreenTab.HOME) },
                        onExecuteExchange = { fromCurr, amt ->
                            viewModel.executeExchange(fromCurr, amt)
                        }
                    )
                }

                ScreenTab.BILLS -> {
                    BillsAndServicesScreen(
                        accounts = accounts,
                        onBack = { viewModel.setScreen(ScreenTab.HOME) },
                        onPayBill = { service, accountOrMeter, amount, currency, isPrepaid ->
                            viewModel.payBill(service, accountOrMeter, amount, currency, isPrepaid)
                        },
                        onBuyAirtime = { operator, phone, amount, currency, packName ->
                            viewModel.buyAirtime(operator, phone, amount, currency, packName)
                        }
                    )
                }

                ScreenTab.SAVINGS -> {
                    SavingsScreen(
                        goals = savingsGoals,
                        accounts = accounts,
                        onBack = { viewModel.setScreen(ScreenTab.HOME) },
                        onDepositToGoal = { goal, amt ->
                            viewModel.depositToSavings(goal, amt)
                        },
                        onWithdrawFromGoal = { goal, amt ->
                            viewModel.withdrawFromSavings(goal, amt)
                        },
                        onCreateGoal = { title, cat, target, curr, days, locked ->
                            viewModel.createSavingsGoal(title, cat, target, curr, days, locked)
                        }
                    )
                }

                ScreenTab.PROFILE -> {
                    ProfileSecurityScreen(
                        onBack = { viewModel.setScreen(ScreenTab.HOME) }
                    )
                }
            }
        }
    }

    // TopUp Modal Dialog
    if (showTopUpDialog) {
        TopUpDialog(
            initialCurrency = "CDF",
            onDismiss = { showTopUpDialog = false },
            onConfirmTopUp = { op, phone, amt, curr ->
                viewModel.topUp(op, phone, amt, curr)
                showTopUpDialog = false
            }
        )
    }

    // Official Receipt Modal Dialog
    activeTxDetail?.let { tx ->
        TransactionReceiptModal(
            transaction = tx,
            onDismiss = { viewModel.selectTransactionForReceipt(null) }
        )
    }
}
