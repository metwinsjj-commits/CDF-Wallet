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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.config.NativeTokenConfig
import com.example.data.local.entity.WalletAccount
import com.example.ui.theme.TrustBlue
import com.example.ui.theme.TrustGreen
import java.util.Locale

@Composable
fun ExchangeScreen(
    accounts: List<WalletAccount>,
    rateSource: String,
    onSetRateSource: (String) -> Unit,
    bccRate: Double,
    parallelRate: Double,
    onBack: () -> Unit,
    onExecuteExchange: (fromCurrency: String, fromAmount: Double) -> Unit
) {
    BackHandler { onBack() }

    val cdfAccount = accounts.find { it.currency == "CDF" } ?: WalletAccount("CDF", 2850000.0, "Principal", "CDF-243-9918-204")
    val usdAccount = accounts.find { it.currency == "USD" } ?: WalletAccount("USD", 480.0, "Devises", "USD-243-9918-204")

    var fromCurrency by remember { mutableStateOf("CDF") }
    val toCurrency = if (fromCurrency == "CDF") "USD" else "CDF"

    var inputAmountText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val activeRate = if (rateSource == "BCC") bccRate else parallelRate
    val fromBalance = if (fromCurrency == "CDF") cdfAccount.balance else usdAccount.balance
    val toBalance = if (toCurrency == "CDF") cdfAccount.balance else usdAccount.balance

    val parsedInput = inputAmountText.toDoubleOrNull() ?: 0.0
    val calculatedOutput = if (fromCurrency == "CDF") {
        if (activeRate > 0) parsedInput / activeRate else 0.0
    } else {
        parsedInput * activeRate
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("trust_swap_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Swap Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Swap",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(
                onClick = {
                    onSetRateSource(if (rateSource == "BCC") "PARALLELE" else "BCC")
                },
                modifier = Modifier.testTag("swap_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Paramètres de taux",
                    tint = TrustBlue
                )
            }
        }

        // Trust Wallet Dual Cards Container
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // "Vous payez" Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Vous payez",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Solde : ${if (fromCurrency == "CDF") String.format(Locale.FRANCE, "%,.0f CDF", fromBalance) else String.format(Locale.US, "$%,.2f", fromBalance)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "MAX",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TrustBlue,
                                    modifier = Modifier
                                        .clickable {
                                            inputAmountText = if (fromCurrency == "CDF") {
                                                String.format(Locale.US, "%.0f", fromBalance)
                                            } else {
                                                String.format(Locale.US, "%.2f", fromBalance)
                                            }
                                        }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = inputAmountText,
                                onValueChange = {
                                    inputAmountText = it.filter { c -> c.isDigit() || c == '.' }
                                    errorMessage = null
                                },
                                placeholder = { Text("0", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
                                textStyle = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("swap_input_amount"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent
                                ),
                                singleLine = true
                            )

                            // Token Selector Pill
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        fromCurrency = if (fromCurrency == "CDF") "USD" else "CDF"
                                        inputAmountText = ""
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (fromCurrency == "CDF") TrustBlue else TrustGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (fromCurrency == "CDF") "CDF" else "$",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = fromCurrency,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Percentage shortcuts (Trust style)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(0.25 to "25%", 0.50 to "50%", 0.75 to "75%", 1.0 to "100%").forEach { (frac, label) ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            val calculated = fromBalance * frac
                                            inputAmountText = if (fromCurrency == "CDF") {
                                                String.format(Locale.US, "%.0f", calculated)
                                            } else {
                                                String.format(Locale.US, "%.2f", calculated)
                                            }
                                        }
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 5.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // "Vous recevez" Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Vous recevez",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Solde : ${if (toCurrency == "CDF") String.format(Locale.FRANCE, "%,.0f CDF", toBalance) else String.format(Locale.US, "$%,.2f", toBalance)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (calculatedOutput > 0) {
                                    if (toCurrency == "CDF") String.format(Locale.FRANCE, "%,.0f", calculatedOutput)
                                    else String.format(Locale.US, "%.2f", calculatedOutput)
                                } else "0",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (calculatedOutput > 0) TrustGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 12.dp)
                            )

                            // Target Token Pill
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (toCurrency == "CDF") TrustBlue else TrustGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (toCurrency == "CDF") "CDF" else "$",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = toCurrency,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Trust Wallet Floating Central Swap Button
            IconButton(
                onClick = {
                    fromCurrency = if (fromCurrency == "CDF") "USD" else "CDF"
                    inputAmountText = ""
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    .testTag("swap_direction_toggle")
            ) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = "Inverser",
                    tint = TrustBlue,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Trust Provider Quote Details
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Taux du fournisseur",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "1 USD ≈ ${activeRate.toInt()} CDF",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Frais protocole (2%)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "2.0% (Reversé à l'Admin)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TrustGreen
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Adresse Admin (Collecte)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = NativeTokenConfig.shortenAddress(NativeTokenConfig.ADMIN_FEE_ADDRESS),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrustBlue
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Contrat Token Natif",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = NativeTokenConfig.shortenAddress(NativeTokenConfig.CONTRACT_ADDRESS),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Source de cotation",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (rateSource == "BCC") "Banque Centrale (BCC)" else "Marché Libre Kinshasa",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TrustBlue
                    )
                }
            }
        }

        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }

        // Trust Blue Big Swap CTA Button
        Button(
            onClick = {
                if (parsedInput <= 0) {
                    errorMessage = "Veuillez entrer un montant valide"
                    return@Button
                }
                if (parsedInput > fromBalance) {
                    errorMessage = "Solde insuffisant en $fromCurrency"
                    return@Button
                }
                onExecuteExchange(fromCurrency, parsedInput)
                inputAmountText = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = TrustBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("trust_execute_swap_button"),
            shape = RoundedCornerShape(26.dp)
        ) {
            Text(
                text = "Échanger",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
