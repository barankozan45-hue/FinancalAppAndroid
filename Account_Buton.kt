package com.example.sql_arac

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Account_Buton(
    accountRepo: AccountRepository,
    onAccountAdded: () -> Unit,
    editingAccount: Account? = null,
    onDismiss: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var balanceText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AccountType.CASH) }
    var selectedCurrency by remember { mutableStateOf("TL") }

    val currencies = listOf("TL", "USD", "EUR", "GR ALTIN", "BTC")

    LaunchedEffect(editingAccount) {
        if (editingAccount != null) {
            name = editingAccount.name
            balanceText = editingAccount.balance.toString()
            selectedType = editingAccount.type
            selectedCurrency = editingAccount.currency
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.95f).wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = SurfaceColor, // 🌑 Slate 800: Derin ama siyah değil
            tonalElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, DeepSlate) // 🧱 İnce bir çerçeve derinlik katar
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (editingAccount == null) "Yeni Hesap Oluştur" else "Hesabı Düzenle",
                    style = MaterialTheme.typography.titleLarge,
                    color = IceWhite, // 🧊 En parlak beyaz
                    fontWeight = FontWeight.Bold
                )

                // 1. HESAP ADI
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Hesap Adı", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = TextStyle(color = IceWhite),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentColor, // 🔵 Indigo Vurgusu
                        unfocusedBorderColor = MutedSlate, // 🧱 Pasif gri
                        focusedLabelColor = AccentColor,
                        cursorColor = AccentColor,
                        focusedContainerColor = AppBackground.copy(alpha = 0.3f) // İçini hafif karartalım
                    )
                )

                // 2. BAKİYE
                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input == "-" || input.matches(Regex("^-?\\d*\\.?\\d*$"))) {
                            balanceText = input
                        }
                    },
                    label = { Text("Başlangıç Bakiyesi", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = TextStyle(color = IceWhite, fontWeight = FontWeight.Bold),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentColor,
                        unfocusedBorderColor = MutedSlate,
                        focusedLabelColor = AccentColor,
                        cursorColor = AccentColor
                    )
                )

                // 3. BİRİM SEÇİMİ
                Text("Birim", color = TextSecondary, fontSize = 12.sp)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currencies.forEach { curr ->
                        FilterChip(
                            selected = (selectedCurrency == curr),
                            onClick = { selectedCurrency = curr },
                            label = { Text(curr) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentColor,
                                selectedLabelColor = IceWhite,
                                containerColor = DeepSlate, // 🌑 Seçili değilken koyu slate
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (selectedCurrency == curr) Color.Transparent else MutedSlate,
                                enabled = true,
                                selected = selectedCurrency == curr
                            )
                        )
                    }
                }

                // 4. HESAP TÜRÜ
                Text("Hesap Türü", color = TextSecondary, fontSize = 12.sp)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AccountType.entries.forEach { type ->
                        FilterChip(
                            selected = (selectedType == type),
                            onClick = { selectedType = type },
                            label = { Text(type.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentColor,
                                selectedLabelColor = IceWhite,
                                containerColor = DeepSlate,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = if (selectedType == type) Color.Transparent else MutedSlate,
                                enabled = true,
                                selected = selectedType == type
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- 🚀 AKSİYON BUTONLARI ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End // Butonları sağa yasla
                ) {
                    if (editingAccount != null) {
                        IconButton(onClick = {
                            accountRepo.deleteAccount(editingAccount.id)
                            onAccountAdded()
                            onDismiss()
                        }) {
                            Icon(Icons.Default.Delete, "Sil", tint = ActionRed) // 🚨 Aksiyon Kırmızısı
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // Vazgeç Butonu
                    TextButton(onClick = onDismiss) {
                        Text("VAZGEÇ", color = TextSecondary)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Onayla Butonu
                    Button(
                        onClick = {
                            val balance = balanceText.toDoubleOrNull() ?: 0.0
                            if (editingAccount == null) {
                                accountRepo.addAccount(Account(0, name, selectedType, balance, selectedCurrency))
                            } else {
                                accountRepo.updateAccount(editingAccount.id, name, selectedType.name, balance, selectedCurrency)
                            }
                            onAccountAdded()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentColor,
                            disabledContainerColor = MutedSlate
                        ),
                        enabled = name.isNotBlank() && balanceText.isNotEmpty() && balanceText != "-",
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (editingAccount == null) "OLUŞTUR" else "GÜNCELLE",
                            color = IceWhite,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

