package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ContactEntity
import com.example.data.local.entity.SavingsGoal
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.WalletAccount
import com.example.data.repository.WalletRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenTab {
    HOME,
    SEND_RECEIVE,
    EXCHANGE,
    BILLS,
    SAVINGS,
    PROFILE
}

class WalletViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WalletRepository

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = WalletRepository(db)
    }

    val walletAccounts: StateFlow<List<WalletAccount>> = repository.walletAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTransactions: StateFlow<List<TransactionEntity>> = repository.recentTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savingsGoals: StateFlow<List<SavingsGoal>> = repository.savingsGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contacts: StateFlow<List<ContactEntity>> = repository.contacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI States
    private val _currentScreen = MutableStateFlow(ScreenTab.HOME)
    val currentScreen: StateFlow<ScreenTab> = _currentScreen.asStateFlow()

    private val _isBalanceVisible = MutableStateFlow(true)
    val isBalanceVisible: StateFlow<Boolean> = _isBalanceVisible.asStateFlow()

    private val _selectedCurrency = MutableStateFlow("CDF")
    val selectedCurrency: StateFlow<String> = _selectedCurrency.asStateFlow()

    private val _activeTransactionDetail = MutableStateFlow<TransactionEntity?>(null)
    val activeTransactionDetail: StateFlow<TransactionEntity?> = _activeTransactionDetail.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Exchange Rates
    val bccOfficialRate = 2835.50 // Banque Centrale du Congo
    val parallelMarketRate = 2865.00 // Marché libre / Cambistes Kinshasa

    private val _selectedRateSource = MutableStateFlow("PARALLELE") // "BCC" or "PARALLELE"
    val selectedRateSource: StateFlow<String> = _selectedRateSource.asStateFlow()

    fun getActiveExchangeRate(): Double {
        return if (_selectedRateSource.value == "BCC") bccOfficialRate else parallelMarketRate
    }

    fun setScreen(tab: ScreenTab) {
        _currentScreen.value = tab
    }

    fun toggleBalanceVisibility() {
        _isBalanceVisible.value = !_isBalanceVisible.value
    }

    fun setSelectedCurrency(currency: String) {
        _selectedCurrency.value = currency
    }

    fun selectTransactionForReceipt(tx: TransactionEntity?) {
        _activeTransactionDetail.value = tx
    }

    fun setRateSource(source: String) {
        _selectedRateSource.value = source
    }

    // Actions
    fun sendMoney(
        recipientPhone: String,
        recipientName: String,
        operator: String,
        amount: Double,
        currency: String,
        note: String
    ) {
        viewModelScope.launch {
            val fee = com.example.data.config.NativeTokenConfig.calculateFee(amount)
            val result = repository.sendMoney(
                recipientPhone = recipientPhone,
                recipientName = recipientName,
                operator = operator,
                amount = amount,
                currency = currency,
                note = note,
                fee = fee
            )
            result.onSuccess { tx ->
                _userMessage.emit("Envoi de ${formatCurrency(amount, currency)} réussi !")
                _activeTransactionDetail.value = tx
            }.onFailure { err ->
                _userMessage.emit("Échec : ${err.message}")
            }
        }
    }

    fun topUp(operator: String, phone: String, amount: Double, currency: String) {
        viewModelScope.launch {
            val result = repository.topUpAccount(operator, phone, amount, currency)
            result.onSuccess { tx ->
                _userMessage.emit("Rechargement de ${formatCurrency(amount, currency)} effectué !")
                _activeTransactionDetail.value = tx
            }.onFailure { err ->
                _userMessage.emit("Erreur : ${err.message}")
            }
        }
    }

    fun executeExchange(fromCurrency: String, fromAmount: Double) {
        viewModelScope.launch {
            val rate = getActiveExchangeRate()
            val toCurrency = if (fromCurrency == "CDF") "USD" else "CDF"
            val toAmount = if (fromCurrency == "CDF") {
                fromAmount / rate
            } else {
                fromAmount * rate
            }

            val result = repository.exchangeCurrency(
                fromCurrency = fromCurrency,
                toCurrency = toCurrency,
                fromAmount = fromAmount,
                toAmount = toAmount,
                rate = rate
            )
            result.onSuccess { tx ->
                _userMessage.emit("Change effectué : ${formatCurrency(toAmount, toCurrency)} crédité !")
                _activeTransactionDetail.value = tx
            }.onFailure { err ->
                _userMessage.emit("Erreur lors du change : ${err.message}")
            }
        }
    }

    fun payBill(
        serviceName: String,
        accountOrMeterNumber: String,
        amount: Double,
        currency: String,
        isCashPowerPrepaid: Boolean = false
    ) {
        viewModelScope.launch {
            val generatedToken = if (isCashPowerPrepaid) {
                // Generate realistic 20-digit SNEL CashPower token grouped in 4 digits
                (1..5).joinToString("-") { (1000..9999).random().toString() }
            } else null

            val result = repository.payBill(
                serviceName = serviceName,
                accountOrMeterNumber = accountOrMeterNumber,
                amount = amount,
                currency = currency,
                tokenGenerated = generatedToken
            )
            result.onSuccess { tx ->
                _userMessage.emit("Paiement $serviceName validé avec succès !")
                _activeTransactionDetail.value = tx
            }.onFailure { err ->
                _userMessage.emit("Erreur de paiement : ${err.message}")
            }
        }
    }

    fun buyAirtime(
        operator: String,
        phoneNumber: String,
        amount: Double,
        currency: String,
        packName: String
    ) {
        viewModelScope.launch {
            val result = repository.buyAirtime(
                operator = operator,
                phoneNumber = phoneNumber,
                amount = amount,
                currency = currency,
                packName = packName
            )
            result.onSuccess { tx ->
                _userMessage.emit("Forfait $packName activé pour $phoneNumber !")
                _activeTransactionDetail.value = tx
            }.onFailure { err ->
                _userMessage.emit("Échec achat forfait : ${err.message}")
            }
        }
    }

    fun depositToSavings(goal: SavingsGoal, amount: Double) {
        viewModelScope.launch {
            val result = repository.depositToSavings(goal, amount)
            result.onSuccess {
                _userMessage.emit("Dépôt de ${formatCurrency(amount, goal.currency)} ajouté à ${goal.title} !")
            }.onFailure { err ->
                _userMessage.emit("Erreur : ${err.message}")
            }
        }
    }

    fun withdrawFromSavings(goal: SavingsGoal, amount: Double) {
        viewModelScope.launch {
            val result = repository.withdrawFromSavings(goal, amount)
            result.onSuccess {
                _userMessage.emit("Retrait de ${formatCurrency(amount, goal.currency)} transféré au solde principal !")
            }.onFailure { err ->
                _userMessage.emit("Erreur : ${err.message}")
            }
        }
    }

    fun createSavingsGoal(
        title: String,
        category: String,
        targetAmount: Double,
        currency: String,
        deadlineDays: Int,
        isLocked: Boolean
    ) {
        viewModelScope.launch {
            repository.addSavingsGoal(
                SavingsGoal(
                    title = title,
                    category = category,
                    targetAmount = targetAmount,
                    currentAmount = 0.0,
                    currency = currency,
                    deadlineDays = deadlineDays,
                    isLocked = isLocked
                )
            )
            _userMessage.emit("Objectif '$title' créé avec succès !")
        }
    }

    fun addContact(name: String, phone: String, operator: String) {
        viewModelScope.launch {
            repository.addContact(
                ContactEntity(
                    name = name,
                    phoneNumber = phone,
                    operator = operator,
                    isFavorite = true,
                    avatarColorHex = listOf("#0F4C81", "#059669", "#D97706", "#7C3AED", "#DC2626").random()
                )
            )
            _userMessage.emit("Contact $name ajouté !")
        }
    }

    fun formatCurrency(amount: Double, currency: String): String {
        return if (currency == "USD") {
            String.format(java.util.Locale.US, "$%,.2f", amount)
        } else {
            String.format(java.util.Locale.FRANCE, "%,.0f CDF", amount)
        }
    }
}
