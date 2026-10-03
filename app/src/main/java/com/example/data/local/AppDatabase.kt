package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.ContactDao
import com.example.data.local.dao.SavingsDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.dao.WalletDao
import com.example.data.local.entity.ContactEntity
import com.example.data.local.entity.SavingsGoal
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.WalletAccount
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        WalletAccount::class,
        TransactionEntity::class,
        SavingsGoal::class,
        ContactEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao
    abstract fun transactionDao(): TransactionDao
    abstract fun savingsDao(): SavingsDao
    abstract fun contactDao(): ContactDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cdf_wallet_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(database: AppDatabase) {
                val walletDao = database.walletDao()
                val txDao = database.transactionDao()
                val contactDao = database.contactDao()
                val savingsDao = database.savingsDao()

                // Default Accounts
                walletDao.insertOrUpdate(
                    WalletAccount(
                        currency = "CDF",
                        balance = 2850000.0,
                        accountName = "Portefeuille Principal CDF",
                        accountNumber = "CDF-243-9918-204"
                    )
                )
                walletDao.insertOrUpdate(
                    WalletAccount(
                        currency = "USD",
                        balance = 480.00,
                        accountName = "Compte Devises USD",
                        accountNumber = "USD-243-9918-204"
                    )
                )

                // Default Contacts
                val contacts = listOf(
                    ContactEntity(name = "Maman Chantal", phoneNumber = "+243 99 876 5432", operator = "Airtel Money", isFavorite = true, avatarColorHex = "#E11D48"),
                    ContactEntity(name = "Christian Makiese", phoneNumber = "+243 89 555 4321", operator = "Orange Money", isFavorite = true, avatarColorHex = "#F59E0B"),
                    ContactEntity(name = "Papa Roger", phoneNumber = "+243 81 234 5678", operator = "Vodacom M-Pesa", isFavorite = true, avatarColorHex = "#0077C8"),
                    ContactEntity(name = "Sarah B.", phoneNumber = "+243 84 111 2233", operator = "Afrimoney", isFavorite = false, avatarColorHex = "#10B981"),
                    ContactEntity(name = "Moïse K.", phoneNumber = "+243 97 000 8899", operator = "Vodacom M-Pesa", isFavorite = false, avatarColorHex = "#6366F1")
                )
                contactDao.insertContacts(contacts)

                // Default Transactions
                val now = System.currentTimeMillis()
                val txs = listOf(
                    TransactionEntity(
                        title = "Dépôt Vodacom M-Pesa",
                        subtitle = "Rechargement compte CDF",
                        amount = 500000.0,
                        currency = "CDF",
                        type = "TOPUP",
                        status = "SUCCESS",
                        recipientOrSource = "+243 81 234 5678",
                        fee = 0.0,
                        referenceCode = "MPESA-884920",
                        category = "Mobile Money",
                        timestamp = now - 3600000L * 2
                    ),
                    TransactionEntity(
                        title = "Abonnement Canal+ RDC",
                        subtitle = "Formule Évasion+",
                        amount = 65000.0,
                        currency = "CDF",
                        type = "BILL",
                        status = "SUCCESS",
                        recipientOrSource = "Canal+ #0492837194",
                        fee = 500.0,
                        referenceCode = "CNL-2026-993",
                        category = "Factures",
                        timestamp = now - 3600000L * 14
                    ),
                    TransactionEntity(
                        title = "Change CDF vers USD",
                        subtitle = "Conversion au taux 2 850 CDF",
                        amount = 285000.0,
                        currency = "CDF",
                        type = "EXCHANGE",
                        status = "SUCCESS",
                        recipientOrSource = "Compte USD (+$100.00)",
                        fee = 0.0,
                        referenceCode = "EXC-991204",
                        category = "Change",
                        timestamp = now - 3600000L * 26
                    ),
                    TransactionEntity(
                        title = "Forfait Internet Vodacom",
                        subtitle = "Pack 15 Go Maxi Net",
                        amount = 30000.0,
                        currency = "CDF",
                        type = "AIRTIME",
                        status = "SUCCESS",
                        recipientOrSource = "+243 82 456 7890",
                        fee = 0.0,
                        referenceCode = "VDC-NET-402",
                        category = "Internet",
                        timestamp = now - 3600000L * 48
                    ),
                    TransactionEntity(
                        title = "Transfert à Maman Chantal",
                        subtitle = "Aide famille Kinshasa",
                        amount = 150000.0,
                        currency = "CDF",
                        type = "SEND",
                        status = "SUCCESS",
                        recipientOrSource = "+243 99 876 5432 (Airtel)",
                        fee = 1500.0,
                        referenceCode = "TRF-772910",
                        category = "Transfert",
                        timestamp = now - 3600000L * 72
                    ),
                    TransactionEntity(
                        title = "Reçu de Christian Makiese",
                        subtitle = "Paiement prestation dev",
                        amount = 120.0,
                        currency = "USD",
                        type = "RECEIVE",
                        status = "SUCCESS",
                        recipientOrSource = "+243 89 555 4321 (Orange)",
                        fee = 0.0,
                        referenceCode = "ORNG-661029",
                        category = "Virement",
                        timestamp = now - 3600000L * 96
                    )
                )
                for (tx in txs) {
                    txDao.insertTransaction(tx)
                }

                // Default Savings Goals
                savingsDao.insertGoal(
                    SavingsGoal(
                        title = "Tontine Kinshasa Club",
                        category = "Tontine",
                        targetAmount = 1500000.0,
                        currentAmount = 950000.0,
                        currency = "CDF",
                        deadlineDays = 18,
                        isLocked = true
                    )
                )
                savingsDao.insertGoal(
                    SavingsGoal(
                        title = "Achat Terrain Maluku",
                        category = "Projet Kinshasa",
                        targetAmount = 2500.0,
                        currentAmount = 820.0,
                        currency = "USD",
                        deadlineDays = 90,
                        isLocked = false
                    )
                )
                savingsDao.insertGoal(
                    SavingsGoal(
                        title = "Urgences Médicales",
                        category = "Urgence",
                        targetAmount = 500000.0,
                        currentAmount = 350000.0,
                        currency = "CDF",
                        deadlineDays = 45,
                        isLocked = false
                    )
                )
            }
        }
    }
}
