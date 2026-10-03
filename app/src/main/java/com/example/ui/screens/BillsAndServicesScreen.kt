package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.WalletAccount
import com.example.ui.theme.CdfGoldDark
import com.example.ui.theme.CdfGoldPrimary
import com.example.ui.theme.CdfGreenDark
import java.util.Locale

@Composable
fun BillsAndServicesScreen(
    accounts: List<WalletAccount>,
    onBack: () -> Unit,
    onPayBill: (service: String, accountOrMeter: String, amount: Double, currency: String, isPrepaid: Boolean) -> Unit,
    onBuyAirtime: (operator: String, phone: String, amount: Double, currency: String, packName: String) -> Unit
) {
    BackHandler { onBack() }

    val cdfAccount = accounts.find { it.currency == "CDF" } ?: WalletAccount("CDF", 2850000.0, "Principal", "CDF-243-9918-204")
    var selectedCategoryIndex by remember { mutableIntStateOf(0) } // 0 = Électricité/Eau, 1 = Internet/Mobile, 2 = TV

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("bills_services_screen")
    ) {
        TabRow(
            selectedTabIndex = selectedCategoryIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedCategoryIndex == 0,
                onClick = { selectedCategoryIndex = 0 },
                text = { Text("SNEL & REGIDESO", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_utilities")
            )
            Tab(
                selected = selectedCategoryIndex == 1,
                onClick = { selectedCategoryIndex = 1 },
                text = { Text("Forfaits Mobile", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_internet")
            )
            Tab(
                selected = selectedCategoryIndex == 2,
                onClick = { selectedCategoryIndex = 2 },
                text = { Text("Canal+ & TV", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_tv")
            )
        }

        when (selectedCategoryIndex) {
            0 -> UtilitiesTab(cdfBalance = cdfAccount.balance, onPay = onPayBill)
            1 -> MobilePacksTab(cdfBalance = cdfAccount.balance, onBuy = onBuyAirtime)
            2 -> TvSubscriptionTab(cdfBalance = cdfAccount.balance, onPay = onPayBill)
        }
    }
}

@Composable
private fun UtilitiesTab(
    cdfBalance: Double,
    onPay: (String, String, Double, String, Boolean) -> Unit
) {
    var selectedProvider by remember { mutableStateOf("SNEL CashPower") }
    var meterOrPoliceNumber by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("25000") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val providers = listOf(
        "SNEL CashPower" to "Électricité prépayée (Code Token)",
        "SNEL Facture" to "Facture ordinaire post-payée",
        "REGIDESO" to "Facture d'eau de ville"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Services Publics RDC",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Provider Selector
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            providers.forEach { (prov, desc) ->
                val isSel = selectedProvider == prov
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = if (isSel) CardDefaults.outlinedCardBorder() else null,
                    tonalElevation = if (isSel) 4.dp else 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedProvider = prov }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (prov.contains("SNEL")) CdfGoldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (prov.contains("SNEL")) Icons.Default.ElectricBolt else Icons.Default.LocalDrink,
                                contentDescription = null,
                                tint = if (prov.contains("SNEL")) CdfGoldDark else MaterialTheme.colorScheme.primary
                            )
                        }

                        Column {
                            Text(text = prov, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Input meter or police number
        OutlinedTextField(
            value = meterOrPoliceNumber,
            onValueChange = {
                meterOrPoliceNumber = it
                errorMessage = null
            },
            label = {
                Text(
                    if (selectedProvider == "SNEL CashPower") "Numéro de compteur CashPower (11 chiffres)"
                    else if (selectedProvider == "REGIDESO") "Numéro de police REGIDESO"
                    else "Numéro d'abonné SNEL"
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("utility_account_input"),
            singleLine = true
        )

        // Amount input
        OutlinedTextField(
            value = amountText,
            onValueChange = {
                amountText = it.filter { c -> c.isDigit() }
                errorMessage = null
            },
            label = { Text("Montant en Francs Congolais (CDF)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("utility_amount_input"),
            singleLine = true
        )

        // Quick amount buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("10000", "25000", "50000", "100000").forEach { amt ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { amountText = amt }
                ) {
                    Text(
                        text = "${amt.toInt() / 1000}k CDF",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 6.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }

        Button(
            onClick = {
                if (meterOrPoliceNumber.isBlank()) {
                    errorMessage = "Veuillez renseigner le numéro de compteur ou de police"
                    return@Button
                }
                val parsed = amountText.toDoubleOrNull() ?: 0.0
                if (parsed <= 0) {
                    errorMessage = "Veuillez saisir un montant valide"
                    return@Button
                }
                if (parsed > cdfBalance) {
                    errorMessage = "Solde insuffisant (Solde actuel : ${String.format(Locale.FRANCE, "%,.0f CDF", cdfBalance)})"
                    return@Button
                }
                val isCashPower = selectedProvider == "SNEL CashPower"
                onPay(selectedProvider, meterOrPoliceNumber, parsed, "CDF", isCashPower)
                meterOrPoliceNumber = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_utility_button"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Payer la facture", fontWeight = FontWeight.Bold)
        }

        if (selectedProvider == "SNEL CashPower") {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CdfGoldPrimary.copy(alpha = 0.12f))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = CdfGoldDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Après validation, un code de recharge à 20 chiffres (Token) sera instantanément généré sur votre reçu numérique !",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun MobilePacksTab(
    cdfBalance: Double,
    onBuy: (String, String, Double, String, String) -> Unit
) {
    var selectedOperator by remember { mutableStateOf("Vodacom") }
    var targetPhone by remember { mutableStateOf("+243 82 000 0000") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val operators = listOf("Vodacom", "Airtel", "Orange", "Africell")

    val packages = listOf(
        Triple("Pack Maxi 1 Go (24h)", 2500.0, "Internet Rapide Jour"),
        Triple("Pack Maxi 5 Go (7 jours)", 11000.0, "Forfait Hebdo Spécial"),
        Triple("Pack Maxi 15 Go (30 jours)", 30000.0, "Mensuel Confort"),
        Triple("Pack Nuit Illimitée (23h-06h)", 4000.0, "Téléchargement & Streaming"),
        Triple("Crédit de communication simple", 5000.0, "Recharge d'appel direct")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Forfaits Internet & Crédit RDC",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Operator selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            operators.forEach { op ->
                val isSel = selectedOperator == op
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedOperator = op }
                ) {
                    Text(
                        text = op,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 10.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        // Phone Input
        OutlinedTextField(
            value = targetPhone,
            onValueChange = { targetPhone = it },
            label = { Text("Numéro à recharger (+243 ...)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("airtime_phone_input"),
            singleLine = true
        )

        // Packages List
        Text(
            text = "Choisir une offre :",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            packages.forEach { (name, price, desc) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (price > cdfBalance) {
                                errorMessage = "Solde insuffisant pour ce pack"
                            } else {
                                onBuy(selectedOperator, targetPhone, price, "CDF", name)
                            }
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = String.format(Locale.FRANCE, "%,.0f CDF", price),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }
    }
}

@Composable
private fun TvSubscriptionTab(
    cdfBalance: Double,
    onPay: (String, String, Double, String, Boolean) -> Unit
) {
    var cardNumber by remember { mutableStateOf("") }
    var selectedPlan by remember { mutableStateOf("Formule Évasion (50 000 CDF)") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val canalPlans = listOf(
        Pair("Formule Access", 25000.0),
        Pair("Formule Évasion", 50000.0),
        Pair("Formule Évasion+", 65000.0),
        Pair("Formule Tout Canal+", 110000.0)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Réabonnement Canal+ RDC",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = cardNumber,
            onValueChange = {
                cardNumber = it.filter { c -> c.isDigit() }
                errorMessage = null
            },
            label = { Text("Numéro de carte décodeur Canal+ (14 chiffres)") },
            leadingIcon = { Icon(Icons.Default.Tv, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("tv_card_input"),
            singleLine = true
        )

        Text(
            text = "Sélectionner la formule :",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            canalPlans.forEach { (name, price) ->
                val isSel = selectedPlan.startsWith(name)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = if (isSel) CardDefaults.outlinedCardBorder() else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedPlan = "$name (${price.toInt()} CDF)" }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = String.format(Locale.FRANCE, "%,.0f CDF", price),
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }

        Button(
            onClick = {
                if (cardNumber.isBlank()) {
                    errorMessage = "Veuillez entrer le numéro de carte décodeur"
                    return@Button
                }
                val selectedPair = canalPlans.firstOrNull { selectedPlan.startsWith(it.first) } ?: canalPlans[1]
                if (selectedPair.second > cdfBalance) {
                    errorMessage = "Solde insuffisant pour ce réabonnement"
                    return@Button
                }
                onPay("Canal+ RDC", "Décodeur #$cardNumber (${selectedPair.first})", selectedPair.second, "CDF", false)
                cardNumber = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("submit_tv_button"),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Tv, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Valider le réabonnement", fontWeight = FontWeight.Bold)
        }
    }
}
