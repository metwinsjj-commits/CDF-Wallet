package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.data.config.NativeTokenConfig
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.WalletAccount
import com.example.ui.theme.CdfGoldAccent
import com.example.ui.theme.TrustBlue
import com.example.ui.theme.TrustGreen
import com.example.ui.theme.TrustRed
import com.example.ui.viewmodel.ScreenTab
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    accounts: List<WalletAccount>,
    transactions: List<TransactionEntity>,
    isBalanceVisible: Boolean,
    onNavigate: (ScreenTab) -> Unit,
    onOpenTopUp: () -> Unit,
    onSelectTransaction: (TransactionEntity) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Actifs (Tokens/Devises), 1 = Historique
    var primaryDisplayCurrency by remember { mutableStateOf("CDF") } // or "USD"

    val cdfAccount = accounts.find { it.currency == "CDF" } ?: WalletAccount("CDF", 2850000.0, "Principal", "CDF-243-9918-204")
    val usdAccount = accounts.find { it.currency == "USD" } ?: WalletAccount("USD", 480.0, "Devises", "USD-243-9918-204")

    // Total Combined Net Worth in Trust Wallet style
    val marketRate = 2865.0
    val totalCdfValue = cdfAccount.balance + (usdAccount.balance * marketRate)
    val totalUsdValue = (cdfAccount.balance / marketRate) + usdAccount.balance

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("trust_home_screen"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Trust Wallet Hero Section
        item {
            TrustHeroSection(
                totalCdfValue = totalCdfValue,
                totalUsdValue = totalUsdValue,
                primaryCurrency = primaryDisplayCurrency,
                isBalanceVisible = isBalanceVisible,
                onToggleCurrency = {
                    primaryDisplayCurrency = if (primaryDisplayCurrency == "CDF") "USD" else "CDF"
                },
                onSend = { onNavigate(ScreenTab.SEND_RECEIVE) },
                onReceive = { onNavigate(ScreenTab.SEND_RECEIVE) },
                onBuy = onOpenTopUp,
                onSwap = { onNavigate(ScreenTab.EXCHANGE) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Trust Wallet Tabs: "Actifs" vs "Historique"
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = TrustBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = TrustBlue,
                        height = 3.dp
                    )
                },
                divider = {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Actifs",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_assets")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Historique",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_history")
                )
            }
        }

        if (selectedTab == 0) {
            // Assets Tab (Trust Wallet Tokens List)
            item {
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Smart Contract & Admin Fee Info Banner (Trust Wallet style)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { onNavigate(ScreenTab.PROFILE) }
                        .testTag("native_token_banner"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(TrustBlue.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = TrustBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Token Natif : ${NativeTokenConfig.shortenAddress(NativeTokenConfig.CONTRACT_ADDRESS)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Admin 2% : ${NativeTokenConfig.shortenAddress(NativeTokenConfig.ADMIN_FEE_ADDRESS)}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = TrustGreen.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "2% Tax",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TrustGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // CDF Asset (Native Base Token)
            item {
                TrustAssetRow(
                    name = "CDF (Token Natif)",
                    ticker = "CDF",
                    iconColor = TrustBlue,
                    iconText = "CDF",
                    logoDrawableRes = R.drawable.trust_cdf_logo_1791034684374,
                    unitPrice = "${NativeTokenConfig.shortenAddress(NativeTokenConfig.CONTRACT_ADDRESS)} • 1 USD = 2 865 CDF",
                    balanceAmount = if (isBalanceVisible) String.format(Locale.FRANCE, "%,.0f CDF", cdfAccount.balance) else "••••••",
                    counterpartValue = if (isBalanceVisible) String.format(Locale.US, "≈ $%,.2f", cdfAccount.balance / marketRate) else "••••••",
                    change24h = "+0.15%",
                    isPositive = true,
                    onClick = { onNavigate(ScreenTab.EXCHANGE) }
                )
            }

            // USD Asset
            item {
                TrustAssetRow(
                    name = "Dollar Américain",
                    ticker = "USD",
                    iconColor = TrustGreen,
                    iconText = "$",
                    unitPrice = "$1.00",
                    balanceAmount = if (isBalanceVisible) String.format(Locale.US, "$%,.2f", usdAccount.balance) else "••••••",
                    counterpartValue = if (isBalanceVisible) String.format(Locale.FRANCE, "≈ %,.0f CDF", usdAccount.balance * marketRate) else "••••••",
                    change24h = "0.00%",
                    isPositive = true,
                    onClick = { onNavigate(ScreenTab.EXCHANGE) }
                )
            }

            // M-Pesa Mobile Money Connected Balance
            item {
                TrustAssetRow(
                    name = "Vodacom M-Pesa RDC",
                    ticker = "MPESA",
                    iconColor = Color(0xFFE60000),
                    iconText = "M",
                    unitPrice = "Mobile Money",
                    balanceAmount = "Connecté",
                    counterpartValue = "Réseau Actif",
                    change24h = "0% Frais",
                    isPositive = true,
                    onClick = onOpenTopUp
                )
            }

            // Airtel Money
            item {
                TrustAssetRow(
                    name = "Airtel Money RDC",
                    ticker = "AIRTEL",
                    iconColor = Color(0xFFFF0000),
                    iconText = "A",
                    unitPrice = "Mobile Money",
                    balanceAmount = "Connecté",
                    counterpartValue = "Réseau Actif",
                    change24h = "0% Frais",
                    isPositive = true,
                    onClick = onOpenTopUp
                )
            }

            // Orange Money
            item {
                TrustAssetRow(
                    name = "Orange Money RDC",
                    ticker = "ORANGE",
                    iconColor = Color(0xFFFF6600),
                    iconText = "O",
                    unitPrice = "Mobile Money",
                    balanceAmount = "Connecté",
                    counterpartValue = "Réseau Actif",
                    change24h = "0% Frais",
                    isPositive = true,
                    onClick = onOpenTopUp
                )
            }

            // Manage Tokens / Add Asset Button (Trust Wallet iconic style)
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = onOpenTopUp,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant)
                        ),
                        modifier = Modifier.testTag("manage_crypto_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = TrustBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gérer les devises et réseaux",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        } else {
            // Activity Tab (Trust Wallet style transactions list)
            item {
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (transactions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Aucune transaction pour le moment",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(transactions, key = { it.id }) { tx ->
                    TrustTransactionRow(
                        transaction = tx,
                        onClick = { onSelectTransaction(tx) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TrustHeroSection(
    totalCdfValue: Double,
    totalUsdValue: Double,
    primaryCurrency: String,
    isBalanceVisible: Boolean,
    onToggleCurrency: () -> Unit,
    onSend: () -> Unit,
    onReceive: () -> Unit,
    onBuy: () -> Unit,
    onSwap: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Label & Currency Switcher
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onToggleCurrency)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (primaryCurrency == "CDF") "Solde Total (CDF) 🇨🇩" else "Solde Total (USD) 💵",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Changer affichage",
                tint = TrustBlue,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Large Primary Balance
        val formattedPrimary = if (!isBalanceVisible) {
            "••••••••••"
        } else if (primaryCurrency == "CDF") {
            String.format(Locale.FRANCE, "%,.0f CDF", totalCdfValue)
        } else {
            String.format(Locale.US, "$%,.2f", totalUsdValue)
        }

        Text(
            text = formattedPrimary,
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Secondary Counterpart & Daily PnL
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val counterpart = if (!isBalanceVisible) {
                "••••"
            } else if (primaryCurrency == "CDF") {
                String.format(Locale.US, "≈ $%,.2f USD", totalUsdValue)
            } else {
                String.format(Locale.FRANCE, "≈ %,.0f CDF", totalCdfValue)
            }

            Text(
                text = counterpart,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            // Trust Wallet Green Profit Badge
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = TrustGreen.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "+0.15%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TrustGreen,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4 Circular Trust Wallet Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TrustActionButton(
                label = "Envoyer",
                icon = Icons.Default.ArrowUpward,
                onClick = onSend,
                testTag = "trust_action_send"
            )
            TrustActionButton(
                label = "Recevoir",
                icon = Icons.Default.ArrowDownward,
                onClick = onReceive,
                testTag = "trust_action_receive"
            )
            TrustActionButton(
                label = "Acheter",
                icon = Icons.Default.Add,
                onClick = onBuy,
                testTag = "trust_action_buy"
            )
            TrustActionButton(
                label = "Swap",
                icon = Icons.Default.SwapHoriz,
                onClick = onSwap,
                testTag = "trust_action_swap"
            )
        }
    }
}

@Composable
private fun TrustActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(TrustBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TrustAssetRow(
    name: String,
    ticker: String,
    iconColor: Color,
    iconText: String,
    logoDrawableRes: Int? = null,
    unitPrice: String,
    balanceAmount: String,
    counterpartValue: String,
    change24h: String,
    isPositive: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Token Logo & Names
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (logoDrawableRes != null) {
                Image(
                    painter = painterResource(id = logoDrawableRes),
                    contentDescription = name,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(iconColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iconText,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp
                    )
                }
            }

            Column {
                Text(
                    text = name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = unitPrice,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = change24h,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isPositive) TrustGreen else TrustRed
                    )
                }
            }
        }

        // Right: Balances
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = balanceAmount,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = counterpartValue,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TrustTransactionRow(
    transaction: TransactionEntity,
    onClick: () -> Unit
) {
    val isCredit = transaction.type in listOf("RECEIVE", "TOPUP")
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.FRANCE)
    val dateStr = dateFormat.format(Date(transaction.timestamp))

    val icon = when (transaction.type) {
        "SEND" -> Icons.Default.ArrowUpward
        "RECEIVE" -> Icons.Default.ArrowDownward
        "EXCHANGE" -> Icons.Default.SwapHoriz
        "BILL" -> Icons.Default.ElectricBolt
        "AIRTIME" -> Icons.Default.Call
        "SAVINGS" -> Icons.Default.Savings
        else -> Icons.Default.LocalAtm
    }

    val iconBgColor = if (isCredit) TrustGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
    val iconColor = if (isCredit) TrustGreen else MaterialTheme.colorScheme.onSurface

    val formattedAmount = if (transaction.currency == "USD") {
        String.format(Locale.US, "%s$%,.2f", if (isCredit) "+" else "-", transaction.amount)
    } else {
        String.format(Locale.FRANCE, "%s%,.0f CDF", if (isCredit) "+" else "-", transaction.amount)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = transaction.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${transaction.subtitle} • $dateStr",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formattedAmount,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCredit) TrustGreen else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Terminé",
                fontSize = 11.sp,
                color = TrustGreen,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
