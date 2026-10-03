package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import java.util.Locale
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.config.NativeTokenConfig
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.ContactEntity
import com.example.data.local.entity.WalletAccount
import com.example.ui.theme.TrustBlue
import com.example.ui.theme.TrustGreen

@Composable
fun SendReceiveScreen(
    accounts: List<WalletAccount>,
    contacts: List<ContactEntity>,
    onBack: () -> Unit,
    onSendMoney: (recipientPhone: String, recipientName: String, operator: String, amount: Double, currency: String, note: String) -> Unit
) {
    BackHandler { onBack() }

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0 = Envoyer, 1 = Recevoir

    val cdfAccount = accounts.find { it.currency == "CDF" } ?: WalletAccount("CDF", 2850000.0, "Principal", "CDF-243-9918-204")
    val usdAccount = accounts.find { it.currency == "USD" } ?: WalletAccount("USD", 480.0, "Devises", "USD-243-9918-204")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("trust_send_receive_screen")
    ) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = TrustBlue,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = TrustBlue,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Envoyer", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_send")
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Recevoir", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_receive")
            )
        }

        if (selectedTabIndex == 0) {
            TrustSendMoneyTab(
                cdfBalance = cdfAccount.balance,
                usdBalance = usdAccount.balance,
                contacts = contacts,
                onSend = onSendMoney
            )
        } else {
            TrustReceiveMoneyTab(
                cdfAccount = cdfAccount,
                usdAccount = usdAccount
            )
        }
    }
}

@Composable
private fun TrustSendMoneyTab(
    cdfBalance: Double,
    usdBalance: Double,
    contacts: List<ContactEntity>,
    onSend: (String, String, String, Double, String, String) -> Unit
) {
    var selectedCurrency by remember { mutableStateOf("CDF") }
    var selectedOperator by remember { mutableStateOf("Vodacom M-Pesa") }
    var recipientPhone by remember { mutableStateOf("") }
    var recipientName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var showPinDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val currentBalance = if (selectedCurrency == "CDF") cdfBalance else usdBalance
    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
    val fee = parsedAmount * NativeTokenConfig.PROTOCOL_FEE_PERCENT

    val operators = listOf(
        "Vodacom M-Pesa",
        "Airtel Money",
        "Orange Money",
        "Afrimoney",
        "CDF Wallet (0%)"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Token selection card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (selectedCurrency == "CDF") TrustBlue else TrustGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedCurrency == "CDF") "CDF" else "$",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Column {
                        Text(
                            text = if (selectedCurrency == "CDF") "Franc Congolais (CDF)" else "Dollar Américain (USD)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Solde : ${if (selectedCurrency == "CDF") String.format(Locale.FRANCE, "%,.0f CDF", cdfBalance) else String.format(Locale.US, "$%,.2f", usdBalance)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(2.dp)
                ) {
                    listOf("CDF", "USD").forEach { curr ->
                        val isSel = selectedCurrency == curr
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSel) TrustBlue else Color.Transparent)
                                .clickable { selectedCurrency = curr }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = curr,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Quick Contacts Pill Bar (Trust Wallet style)
        if (contacts.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Bénéficiaires récents",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(contacts) { c ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                recipientPhone = c.phoneNumber
                                recipientName = c.name
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(TrustBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = c.name.take(1),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(text = c.name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }

        // Recipient Address / Phone (Trust Wallet style field)
        OutlinedTextField(
            value = recipientPhone,
            onValueChange = { recipientPhone = it },
            label = { Text("Adresse ou numéro Mobile Money (+243 ...)") },
            placeholder = { Text("081XXXXXXX ou @identifiant") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = TrustBlue) },
            trailingIcon = {
                Text(
                    text = "COLLER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrustBlue,
                    modifier = Modifier
                        .clickable { recipientPhone = "+243 81 234 5678" }
                        .padding(end = 12.dp)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("trust_send_phone_input"),
            singleLine = true
        )

        // Recipient Full Name
        OutlinedTextField(
            value = recipientName,
            onValueChange = { recipientName = it },
            label = { Text("Nom du destinataire") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("trust_send_name_input"),
            singleLine = true
        )

        // Operator Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(operators) { op ->
                val isSel = selectedOperator == op
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSel) TrustBlue else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { selectedOperator = op }
                ) {
                    Text(
                        text = op,
                        fontSize = 11.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Amount with MAX
        OutlinedTextField(
            value = amountText,
            onValueChange = {
                amountText = it.filter { c -> c.isDigit() || c == '.' }
                errorMessage = null
            },
            label = { Text("Montant ($selectedCurrency)") },
            trailingIcon = {
                Text(
                    text = "MAX",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrustBlue,
                    modifier = Modifier
                        .clickable {
                            amountText = if (selectedCurrency == "CDF") {
                                String.format(Locale.US, "%.0f", currentBalance)
                            } else {
                                String.format(Locale.US, "%.2f", currentBalance)
                            }
                        }
                        .padding(end = 12.dp)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("trust_send_amount_input"),
            singleLine = true
        )

        // Optional Memo / Note
        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("Note / Motif (facultatif)") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("trust_send_note_input"),
            singleLine = true
        )

        // 2% Protocol Fee Card (routed to admin fee address)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Frais protocole (2%)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = if (selectedCurrency == "USD") String.format(Locale.US, "$%,.2f", fee) else String.format(Locale.FRANCE, "%,.0f CDF", fee),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TrustGreen
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Collecte Admin (2%)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = NativeTokenConfig.shortenAddress(NativeTokenConfig.ADMIN_FEE_ADDRESS),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TrustBlue
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total à débiter", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        text = if (selectedCurrency == "USD") String.format(Locale.US, "$%,.2f", parsedAmount + fee) else String.format(Locale.FRANCE, "%,.0f CDF", parsedAmount + fee),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }

        // Large Trust Wallet "Continuer" Button
        Button(
            onClick = {
                if (recipientPhone.isBlank()) {
                    errorMessage = "Veuillez saisir un numéro ou adresse"
                    return@Button
                }
                if (recipientName.isBlank()) {
                    errorMessage = "Veuillez renseigner le nom du destinataire"
                    return@Button
                }
                if (parsedAmount <= 0) {
                    errorMessage = "Montant invalide"
                    return@Button
                }
                if (parsedAmount + fee > currentBalance) {
                    errorMessage = "Solde insuffisant"
                    return@Button
                }
                showPinDialog = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = TrustBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("trust_send_continue_button"),
            shape = RoundedCornerShape(26.dp)
        ) {
            Text("Continuer", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }

    if (showPinDialog) {
        TrustPinDialog(
            amount = parsedAmount,
            fee = fee,
            currency = selectedCurrency,
            recipient = recipientName,
            onDismiss = { showPinDialog = false },
            onConfirm = {
                showPinDialog = false
                onSend(recipientPhone, recipientName, selectedOperator, parsedAmount, selectedCurrency, noteText)
            }
        )
    }
}

@Composable
private fun TrustReceiveMoneyTab(
    cdfAccount: WalletAccount,
    usdAccount: WalletAccount
) {
    val context = LocalContext.current
    var selectedCurrency by remember { mutableStateOf("CDF") }

    val currentAccount = if (selectedCurrency == "CDF") cdfAccount else usdAccount
    val walletAddress = currentAccount.accountNumber

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Currency Selector
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(2.dp)
        ) {
            listOf("CDF", "USD").forEach { curr ->
                val isSel = selectedCurrency == curr
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isSel) TrustBlue else Color.Transparent)
                        .clickable { selectedCurrency = curr }
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (curr == "CDF") "Franc Congolais (CDF)" else "Dollar US (USD)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Trust Wallet White QR Code Box with central Shield
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .size(240.dp)
                .padding(8.dp)
                .testTag("trust_qr_container"),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val gridSize = 10
                    val cellSize = size.width / gridSize
                    val darkColor = Color(0xFF0B0E17)

                    for (i in 0 until gridSize) {
                        for (j in 0 until gridSize) {
                            val isCorner = (i < 3 && j < 3) || (i > 6 && j < 3) || (i < 3 && j > 6)
                            val isCenter = (i in 4..5 && j in 4..5)
                            val isPattern = (i * 11 + j * 7) % 3 != 0 || isCorner
                            if (isPattern && !isCenter) {
                                drawRect(
                                    color = darkColor,
                                    topLeft = Offset(i * cellSize + 2f, j * cellSize + 2f),
                                    size = Size(cellSize - 4f, cellSize - 4f)
                                )
                            }
                        }
                    }
                }

                // Center CDF-Wallet Logo in QR code
                Image(
                    painter = painterResource(id = R.drawable.trust_cdf_logo_1791034684374),
                    contentDescription = "CDF-Wallet Logo",
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                )
            }
        }

        // Address Pill
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Adresse du portefeuille",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = walletAddress,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Adresse", walletAddress))
                        Toast.makeText(context, "Adresse copiée !", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("copy_address_button")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copier", tint = TrustBlue)
                }
            }
        }

        // Native Token & Admin Treasury Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Token Natif CDF",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = TrustBlue.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "BEP-20 / Base",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TrustBlue,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Contrat Token Natif", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = NativeTokenConfig.CONTRACT_ADDRESS,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Contrat", NativeTokenConfig.CONTRACT_ADDRESS))
                                Toast.makeText(context, "Contrat copié !", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copier", tint = TrustBlue, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Collecte Admin 2% (Achats/Ventes/Transferts)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = NativeTokenConfig.shortenAddress(NativeTokenConfig.ADMIN_FEE_ADDRESS),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrustBlue,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Text(
            text = "Envoyez uniquement des fonds en $selectedCurrency ou Mobile Money RDC vers cette adresse.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // 3 Action Buttons (Trust Wallet signature style)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TrustReceiveAction(
                label = "Copier",
                icon = Icons.Default.ContentCopy,
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Adresse", walletAddress))
                    Toast.makeText(context, "Adresse copiée !", Toast.LENGTH_SHORT).show()
                }
            )

            TrustReceiveAction(
                label = "Partager",
                icon = Icons.Default.Share,
                onClick = {
                    Toast.makeText(context, "Lien de paiement partagé !", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
private fun TrustReceiveAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = TrustBlue,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TrustPinDialog(
    amount: Double,
    fee: Double,
    currency: String,
    recipient: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var pinCode by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("trust_pin_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(TrustBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = TrustBlue,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Confirmer le transfert",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Envoi à $recipient",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Montant net", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(if (currency == "USD") "$$amount" else "${amount.toInt()} CDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Frais 2% (Admin)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(if (currency == "USD") "$$fee" else "${fee.toInt()} CDF", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TrustGreen)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Adresse Collecte", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(NativeTokenConfig.shortenAddress(NativeTokenConfig.ADMIN_FEE_ADDRESS), fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TrustBlue)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = pinCode,
                    onValueChange = {
                        if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                            pinCode = it
                            pinError = false
                        }
                    },
                    label = { Text("Code d'accès secret") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (pinError) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Veuillez entrer les 4 chiffres de votre code secret",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Annuler", color = MaterialTheme.colorScheme.onSurface)
                    }

                    Button(
                        onClick = {
                            if (pinCode.length == 4) {
                                onConfirm()
                            } else {
                                pinError = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TrustBlue),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text("Valider", color = Color.White)
                    }
                }
            }
        }
    }
}
