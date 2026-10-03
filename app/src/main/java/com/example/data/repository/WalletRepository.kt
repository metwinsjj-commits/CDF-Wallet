package com.example.data.repository
 
import com.example.data.config.NativeTokenConfig
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ContactEntity
import com.example.data.local.entity.SavingsGoal
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.WalletAccount
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class WalletRepository(private val database: AppDatabase) {
    private val walletDao = database.walletDao()
    private val txDao = database.transactionDao()
    private val savingsDao = database.savingsDao()
    private val contactDao = database.contactDao()

    val walletAccounts: Flow<List<WalletAccount>> = walletDao.getAllAccounts()
    val allTransactions: Flow<List<TransactionEntity>> = txDao.getAllTransactions()
    val recentTransactions: Flow<List<TransactionEntity>> = txDao.getRecentTransactions(20)
    val savingsGoals: Flow<List<SavingsGoal>> = savingsDao.getAllGoals()
    val contacts: Flow<List<ContactEntity>> = contactDao.getAllContacts()

    suspend fun sendMoney(
        recipientPhone: String,
        recipientName: String,
        operator: String,
        amount: Double,
        currency: String,
        note: String,
        fee: Double
    ): Result<TransactionEntity> {
        val totalDebit = amount + fee
        val currentAccount = walletDao.getAccountSync(currency)
            ?: return Result.failure(Exception("Compte $currency introuvable"))

        if (currentAccount.balance < totalDebit) {
            return Result.failure(Exception("Solde insuffisant ($currency). Requis: $totalDebit, Disponible: ${currentAccount.balance}"))
        }

        // Deduct balance
        walletDao.updateBalance(currency, currentAccount.balance - totalDebit)

        val txCode = "TRF-${UUID.randomUUID().toString().take(8).uppercase()}"
        val feeNote = "Frais 2% reversés à l'admin (${NativeTokenConfig.shortenAddress(NativeTokenConfig.ADMIN_FEE_ADDRESS)})"
        val fullNote = if (note.isEmpty()) feeNote else "$note • $feeNote"

        val transaction = TransactionEntity(
            title = "Transfert à $recipientName",
            subtitle = "$operator ($recipientPhone)",
            amount = amount,
            currency = currency,
            type = "SEND",
            status = "SUCCESS",
            recipientOrSource = "$recipientName ($recipientPhone)",
            fee = fee,
            referenceCode = txCode,
            note = fullNote,
            category = "Transfert",
            timestamp = System.currentTimeMillis()
        )
        val id = txDao.insertTransaction(transaction)
        return Result.success(transaction.copy(id = id))
    }

    suspend fun topUpAccount(
        operator: String,
        phoneNumber: String,
        amount: Double,
        currency: String
    ): Result<TransactionEntity> {
        val currentAccount = walletDao.getAccountSync(currency)
            ?: return Result.failure(Exception("Compte $currency introuvable"))

        walletDao.updateBalance(currency, currentAccount.balance + amount)

        val ref = "TOP-${UUID.randomUUID().toString().take(8).uppercase()}"
        val transaction = TransactionEntity(
            title = "Dépôt $operator",
            subtitle = "Rechargement via $phoneNumber",
            amount = amount,
            currency = currency,
            type = "TOPUP",
            status = "SUCCESS",
            recipientOrSource = "$operator ($phoneNumber)",
            fee = 0.0,
            referenceCode = ref,
            note = "Dépôt Mobile Money",
            category = "Rechargement",
            timestamp = System.currentTimeMillis()
        )
        val id = txDao.insertTransaction(transaction)
        return Result.success(transaction.copy(id = id))
    }

    suspend fun exchangeCurrency(
        fromCurrency: String,
        toCurrency: String,
        fromAmount: Double,
        toAmount: Double,
        rate: Double
    ): Result<TransactionEntity> {
        val fromAccount = walletDao.getAccountSync(fromCurrency)
            ?: return Result.failure(Exception("Compte source $fromCurrency introuvable"))
        val toAccount = walletDao.getAccountSync(toCurrency)
            ?: return Result.failure(Exception("Compte cible $toCurrency introuvable"))

        if (fromAccount.balance < fromAmount) {
            return Result.failure(Exception("Solde insuffisant en $fromCurrency"))
        }

        walletDao.updateBalance(fromCurrency, fromAccount.balance - fromAmount)
        walletDao.updateBalance(toCurrency, toAccount.balance + toAmount)

        val fee = fromAmount * NativeTokenConfig.PROTOCOL_FEE_PERCENT
        val ref = "EXC-${UUID.randomUUID().toString().take(8).uppercase()}"
        val transaction = TransactionEntity(
            title = "Change $fromCurrency vers $toCurrency",
            subtitle = "Taux: 1 USD = ${String.format(java.util.Locale.US, "%.0f", if (fromCurrency == "USD") rate else 1/rate)} CDF",
            amount = fromAmount,
            currency = fromCurrency,
            type = "EXCHANGE",
            status = "SUCCESS",
            recipientOrSource = "Compte $toCurrency (+${String.format(java.util.Locale.US, "%.2f", toAmount)} $toCurrency)",
            fee = fee,
            referenceCode = ref,
            note = "Swap (Achat/Vente) • Frais 2% alloués à l'admin ${NativeTokenConfig.shortenAddress(NativeTokenConfig.ADMIN_FEE_ADDRESS)}",
            category = "Change",
            timestamp = System.currentTimeMillis()
        )
        val id = txDao.insertTransaction(transaction)
        return Result.success(transaction.copy(id = id))
    }

    suspend fun payBill(
        serviceName: String,
        accountOrMeterNumber: String,
        amount: Double,
        currency: String,
        tokenGenerated: String? = null
    ): Result<TransactionEntity> {
        val currentAccount = walletDao.getAccountSync(currency)
            ?: return Result.failure(Exception("Compte $currency introuvable"))

        if (currentAccount.balance < amount) {
            return Result.failure(Exception("Solde insuffisant en $currency"))
        }

        walletDao.updateBalance(currency, currentAccount.balance - amount)

        val ref = "BIL-${UUID.randomUUID().toString().take(8).uppercase()}"
        val noteText = if (tokenGenerated != null) {
            "Code Token Rechargement: $tokenGenerated"
        } else {
            "Paiement facture $serviceName"
        }

        val transaction = TransactionEntity(
            title = "Paiement $serviceName",
            subtitle = "Réf/Compteur: $accountOrMeterNumber",
            amount = amount,
            currency = currency,
            type = "BILL",
            status = "SUCCESS",
            recipientOrSource = "$serviceName ($accountOrMeterNumber)",
            fee = 0.0,
            referenceCode = ref,
            note = noteText,
            category = "Factures",
            timestamp = System.currentTimeMillis()
        )
        val id = txDao.insertTransaction(transaction)
        return Result.success(transaction.copy(id = id))
    }

    suspend fun buyAirtime(
        operator: String,
        phoneNumber: String,
        amount: Double,
        currency: String,
        packName: String
    ): Result<TransactionEntity> {
        val currentAccount = walletDao.getAccountSync(currency)
            ?: return Result.failure(Exception("Compte $currency introuvable"))

        if (currentAccount.balance < amount) {
            return Result.failure(Exception("Solde insuffisant en $currency"))
        }

        walletDao.updateBalance(currency, currentAccount.balance - amount)

        val ref = "AIR-${UUID.randomUUID().toString().take(8).uppercase()}"
        val transaction = TransactionEntity(
            title = "Forfait $operator",
            subtitle = "$packName pour $phoneNumber",
            amount = amount,
            currency = currency,
            type = "AIRTIME",
            status = "SUCCESS",
            recipientOrSource = "$operator ($phoneNumber)",
            fee = 0.0,
            referenceCode = ref,
            note = "Activation instantanée forfait $packName",
            category = "Internet & Téléphonie",
            timestamp = System.currentTimeMillis()
        )
        val id = txDao.insertTransaction(transaction)
        return Result.success(transaction.copy(id = id))
    }

    suspend fun depositToSavings(goal: SavingsGoal, amount: Double): Result<SavingsGoal> {
        val currentAccount = walletDao.getAccountSync(goal.currency)
            ?: return Result.failure(Exception("Compte ${goal.currency} introuvable"))

        if (currentAccount.balance < amount) {
            return Result.failure(Exception("Solde insuffisant pour alimenter l'épargne"))
        }

        walletDao.updateBalance(goal.currency, currentAccount.balance - amount)
        val newGoalAmount = goal.currentAmount + amount
        savingsDao.updateGoalAmount(goal.id, newGoalAmount)

        val ref = "SAV-${UUID.randomUUID().toString().take(8).uppercase()}"
        txDao.insertTransaction(
            TransactionEntity(
                title = "Dépôt Épargne: ${goal.title}",
                subtitle = "Catégorie: ${goal.category}",
                amount = amount,
                currency = goal.currency,
                type = "SAVINGS",
                status = "SUCCESS",
                recipientOrSource = goal.title,
                fee = 0.0,
                referenceCode = ref,
                note = "Alimentation coffre/tontine",
                category = "Épargne",
                timestamp = System.currentTimeMillis()
            )
        )

        return Result.success(goal.copy(currentAmount = newGoalAmount))
    }

    suspend fun withdrawFromSavings(goal: SavingsGoal, amount: Double): Result<SavingsGoal> {
        if (goal.isLocked) {
            return Result.failure(Exception("Ce coffre est verrouillé jusqu'à son échéance"))
        }
        if (goal.currentAmount < amount) {
            return Result.failure(Exception("Fonds insuffisants dans ce coffre"))
        }

        val currentAccount = walletDao.getAccountSync(goal.currency)
            ?: return Result.failure(Exception("Compte ${goal.currency} introuvable"))

        val newGoalAmount = goal.currentAmount - amount
        savingsDao.updateGoalAmount(goal.id, newGoalAmount)
        walletDao.updateBalance(goal.currency, currentAccount.balance + amount)

        val ref = "WTH-${UUID.randomUUID().toString().take(8).uppercase()}"
        txDao.insertTransaction(
            TransactionEntity(
                title = "Retrait Épargne: ${goal.title}",
                subtitle = "Retour vers solde disponible",
                amount = amount,
                currency = goal.currency,
                type = "RECEIVE",
                status = "SUCCESS",
                recipientOrSource = goal.title,
                fee = 0.0,
                referenceCode = ref,
                note = "Retrait coffre-fort vers solde principal",
                category = "Épargne",
                timestamp = System.currentTimeMillis()
            )
        )

        return Result.success(goal.copy(currentAmount = newGoalAmount))
    }

    suspend fun addSavingsGoal(goal: SavingsGoal): Long {
        return savingsDao.insertGoal(goal)
    }

    suspend fun addContact(contact: ContactEntity): Long {
        return contactDao.insertContact(contact)
    }

    suspend fun deleteContact(id: Long) {
        contactDao.deleteContact(id)
    }
}
