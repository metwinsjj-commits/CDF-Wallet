package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.SavingsGoal
import com.example.data.local.entity.WalletAccount
import com.example.ui.theme.CdfGoldDark
import com.example.ui.theme.CdfGoldPrimary
import com.example.ui.theme.CdfGreenDark
import com.example.ui.theme.CdfGreenSuccess
import java.util.Locale

@Composable
fun SavingsScreen(
    goals: List<SavingsGoal>,
    accounts: List<WalletAccount>,
    onBack: () -> Unit,
    onDepositToGoal: (SavingsGoal, Double) -> Unit,
    onWithdrawFromGoal: (SavingsGoal, Double) -> Unit,
    onCreateGoal: (title: String, category: String, targetAmount: Double, currency: String, deadlineDays: Int, isLocked: Boolean) -> Unit
) {
    BackHandler { onBack() }

    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedGoalForDeposit by remember { mutableStateOf<SavingsGoal?>(null) }
    var selectedGoalForWithdraw by remember { mutableStateOf<SavingsGoal?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("savings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Épargne & Tontine Digitale",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Sécurisez vos projets en Francs & Dollars",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CdfGoldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val totalCdf = goals.filter { it.currency == "CDF" }.sumOf { it.currentAmount }
                    val totalUsd = goals.filter { it.currency == "USD" }.sumOf { it.currentAmount }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Total Épargné CDF", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                                Text(
                                    text = String.format(Locale.FRANCE, "%,.0f CDF", totalCdf),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Total Épargné USD", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                                Text(
                                    text = String.format(Locale.US, "$%,.2f", totalUsd),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CdfGoldPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Header and Create Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mes Coffres & Tontines (${goals.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("create_goal_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nouveau", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Goals List
        items(goals, key = { it.id }) { goal ->
            GoalCard(
                goal = goal,
                onDeposit = { selectedGoalForDeposit = goal },
                onWithdraw = { selectedGoalForWithdraw = goal }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Create Goal Dialog
    if (showCreateDialog) {
        CreateGoalDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, cat, target, curr, days, locked ->
                onCreateGoal(title, cat, target, curr, days, locked)
                showCreateDialog = false
            }
        )
    }

    // Deposit to Goal Dialog
    selectedGoalForDeposit?.let { goal ->
        GoalActionDialog(
            title = "Alimenter ${goal.title}",
            goal = goal,
            isDeposit = true,
            onDismiss = { selectedGoalForDeposit = null },
            onConfirm = { amount ->
                onDepositToGoal(goal, amount)
                selectedGoalForDeposit = null
            }
        )
    }

    // Withdraw from Goal Dialog
    selectedGoalForWithdraw?.let { goal ->
        GoalActionDialog(
            title = "Retirer de ${goal.title}",
            goal = goal,
            isDeposit = false,
            onDismiss = { selectedGoalForWithdraw = null },
            onConfirm = { amount ->
                onWithdrawFromGoal(goal, amount)
                selectedGoalForWithdraw = null
            }
        )
    }
}

@Composable
private fun GoalCard(
    goal: SavingsGoal,
    onDeposit: () -> Unit,
    onWithdraw: () -> Unit
) {
    val progress = if (goal.targetAmount > 0) {
        (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
    } else 0f

    val percent = (progress * 100).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CdfGoldPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (goal.category == "Tontine") Icons.Default.Groups else Icons.Default.Savings,
                            contentDescription = null,
                            tint = CdfGoldDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(text = goal.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = "${goal.category} • ${goal.deadlineDays} jours restants",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Lock badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (goal.isLocked) Color(0xFFFEF3C7) else Color(0xFFD1FAE5)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = if (goal.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = if (goal.isLocked) CdfGoldDark else CdfGreenDark,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (goal.isLocked) "Bloqué" else "Flexible",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (goal.isLocked) CdfGoldDark else CdfGreenDark
                        )
                    }
                }
            }

            // Amounts and Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = if (goal.currency == "USD") String.format(Locale.US, "$%,.2f", goal.currentAmount)
                        else String.format(Locale.FRANCE, "%,.0f CDF", goal.currentAmount),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "sur ${if (goal.currency == "USD") String.format(Locale.US, "$%,.2f", goal.targetAmount) else String.format(Locale.FRANCE, "%,.0f CDF", goal.targetAmount)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "$percent%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (percent >= 100) CdfGreenDark else MaterialTheme.colorScheme.primary
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (percent >= 100) CdfGreenSuccess else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDeposit,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Alimenter", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onWithdraw,
                    enabled = !goal.isLocked && goal.currentAmount > 0,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (goal.isLocked) "Verrouillé" else "Retirer",
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, targetAmount: Double, currency: String, deadlineDays: Int, isLocked: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tontine") }
    var targetAmountText by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("CDF") }
    var deadlineDays by remember { mutableStateOf("30") }
    var isLocked by remember { mutableStateOf(false) }

    val categories = listOf("Tontine", "Projet Kinshasa", "Frais Scolaires", "Urgence")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("create_goal_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Créer un Coffre / Tontine",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nom du projet / Tontine") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Catégorie :", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.take(2).forEach { cat ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedCategory == cat) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Currency selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("CDF", "USD").forEach { curr ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (selectedCurrency == curr) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedCurrency = curr }
                        ) {
                            Text(
                                text = if (curr == "CDF") "Franc Congolais (CDF)" else "Dollar US (USD)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedCurrency == curr) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = { targetAmountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Objectif cible ($selectedCurrency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = deadlineDays,
                    onValueChange = { deadlineDays = it.filter { c -> c.isDigit() } },
                    label = { Text("Échéance (en jours)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isLocked = !isLocked }
                ) {
                    Checkbox(checked = isLocked, onCheckedChange = { isLocked = it })
                    Text(
                        text = "Coffre bloqué jusqu'à l'échéance (Épargne disciplinée)",
                        fontSize = 11.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Annuler", color = MaterialTheme.colorScheme.onSurface)
                    }

                    Button(
                        onClick = {
                            val target = targetAmountText.toDoubleOrNull() ?: 100000.0
                            val days = deadlineDays.toIntOrNull() ?: 30
                            onConfirm(
                                if (title.isBlank()) "Projet Épargne" else title,
                                selectedCategory,
                                target,
                                selectedCurrency,
                                days,
                                isLocked
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Créer")
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalActionDialog(
    title: String,
    goal: SavingsGoal,
    isDeposit: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it.filter { c -> c.isDigit() || c == '.' }
                        errorMessage = null
                    },
                    label = { Text("Montant (${goal.currency})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                errorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Annuler", color = MaterialTheme.colorScheme.onSurface)
                    }

                    Button(
                        onClick = {
                            val parsed = amountText.toDoubleOrNull() ?: 0.0
                            if (parsed <= 0) {
                                errorMessage = "Veuillez entrer un montant valide"
                                return@Button
                            }
                            if (!isDeposit && parsed > goal.currentAmount) {
                                errorMessage = "Fonds insuffisants dans ce coffre"
                                return@Button
                            }
                            onConfirm(parsed)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirmer")
                    }
                }
            }
        }
    }
}
