package com.example.sql_arac

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 🎯 Hedef Tanımlama / Güncelleme Dialog'u
 * @param initialSelectedAccountId: Eğer bir hedef kartından geliniyorsa, bağlı hesabın ID'si.
 */
@Composable
fun Goal_Buton(
    accounts: List<Account>,
    initialSelectedAccountId: Int? = null, // 🎯 Eklenen parametre
    onAddNewAccount: () -> Unit,
    onConfirm: (FinancialGoal) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetStr by remember { mutableStateOf("") }

    val investmentAccounts = remember(accounts) {
        accounts.filter { it.type == AccountType.INVESTMENT }
    }

    // 🧠 Hesap seçimi mantığı:
    // Dışarıdan ID geldiyse o hesabı bul, yoksa listenin ilkini seç.
    var selectedAccount by remember {
        mutableStateOf<Account?>(
            investmentAccounts.find { it.id == initialSelectedAccountId } ?: investmentAccounts.firstOrNull()
        )
    }

    var isExpanded by remember { mutableStateOf(false) }

    // Eğer dışarıdan hesap ID'si gelmişse, kullanıcı yanlışlıkla değiştirmesin diye kilitleyebiliriz.
    val isAccountLocked = initialSelectedAccountId != null

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = if (isAccountLocked) "🎯 Birikime Başla" else "🎯 Yeni Hayal Kur",
                color = IceWhite,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                GoalTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Hayalin Nedir?",
                    icon = Icons.Default.Star
                )

                GoalTextField(
                    value = targetStr,
                    onValueChange = { targetStr = it },
                    label = "Hedef Tutar (₺)",
                    icon = Icons.Default.Flag,
                    keyboardType = KeyboardType.Number
                )

                Column {
                    Text(
                        text = "Bağlı Yatırım Hesabı",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    Box(Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedAccount?.name ?: "Hesap Seç",
                            onValueChange = {},
                            readOnly = true,
                            // 🔒 Eğer hesap kilitliyse tıklama özelliğini kapatıyoruz
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(if (!isAccountLocked) Modifier.clickable { isExpanded = true } else Modifier),
                            trailingIcon = {
                                // Kilitli değilse aşağı ok, kilitliyse kilit ikonu göstererek kullanıcıya bilgi veriyoruz
                                Icon(
                                    imageVector = if (isAccountLocked) Icons.Default.Lock else Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = if (isAccountLocked) TextSecondary else CyanAccent
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isAccountLocked) DeepSlate else CyanAccent,
                                unfocusedBorderColor = if (isAccountLocked) DeepSlate else MutedSlate
                            )
                        )

                        // Dropdown açma katmanı (Sadece kilitli değilse çalışır)
                        if (!isAccountLocked) {
                            Box(Modifier.matchParentSize().clickable { isExpanded = true })
                            DropdownMenu(
                                expanded = isExpanded,
                                onDismissRequest = { isExpanded = false },
                                modifier = Modifier.background(SurfaceColor)
                            ) {
                                investmentAccounts.forEach { acc ->
                                    DropdownMenuItem(
                                        text = { Text(acc.name, color = IceWhite) },
                                        onClick = {
                                            selectedAccount = acc
                                            isExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = targetStr.toDoubleOrNull() ?: 0.0
                    if (name.isNotEmpty() && amount > 0 && selectedAccount != null) {
                        onConfirm(
                            FinancialGoal(
                                name = name,
                                targetAmount = amount,
                                linkedAccountId = selectedAccount!!.id
                            )
                        )
                    }
                },
                enabled = name.isNotEmpty() && targetStr.isNotEmpty() && selectedAccount != null,
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
            ) {
                Text("BAŞLAT", color = AppBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İPTAL", color = TextSecondary)
            }
        }
    )
}

@Composable
fun GoalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, color = TextSecondary) },
        leadingIcon = { Icon(icon, null, tint = CyanAccent) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyanAccent,
            focusedTextColor = IceWhite,
            unfocusedTextColor = IceWhite,
            cursorColor = CyanAccent
        )
    )
}

