package com.example.sql_arac

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInvestmentDialog(
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onConfirm: (Investment, targetAccountId: Int, Boolean, Int, Int) -> Unit
) {
    // --- STATE YÖNETİMİ ---
    var fromAccount by remember { mutableStateOf<Account?>(null) }
    var totalCostText by remember { mutableStateOf("") }
    var toAccount by remember { mutableStateOf<Account?>(null) }
    var quantityText by remember { mutableStateOf("") }
    var assetName by remember { mutableStateOf("") }
    var assetType by remember { mutableStateOf("ALTIN") }
    var isRecurring by remember { mutableStateOf(false) }
    var dayOfMonth by remember { mutableStateOf("1") }
    var totalMonths by remember { mutableStateOf("12") }

    val assetTypes = listOf("TL","ALTIN", "USD", "EUR", "BORSA", "KRİPTO", "FON")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SteelBlue,
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        title = {
            Text("📈 Yatırım Transferi", fontWeight = FontWeight.ExtraBold, color = IceWhite)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. BÖLÜM: PARA ÇIKIŞI (KAYNAK HESAP)
                Column {
                    Text("1. Ödeme Yapılan Hesap", color = ActionRed, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()).padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        accounts.forEach { acc ->
                            FilterChip(
                                selected = (fromAccount?.id == acc.id),
                                onClick = { fromAccount = acc },
                                label = { Text("${acc.name} (${acc.balance} ${acc.currency})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ActionRed,
                                    selectedLabelColor = IceWhite,
                                    labelColor = MutedSlate,
                                    containerColor = DeepSlate.copy(alpha = 0.1f)
                                )
                            )
                        }
                    }
                    OutlinedTextField(
                        value = totalCostText,
                        onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) totalCostText = it },
                        label = { Text("Ödenen Toplam Maliyet (${fromAccount?.currency ?: "₺"})", color = Color.Black) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = TextStyle(color = AppBackground, fontWeight = FontWeight.Bold),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ActionRed,
                            unfocusedBorderColor = MutedSlate
                        )
                    )
                }

                // 2. BÖLÜM: VARLIK GİRİŞİ (HEDEF YATIRIM HESABI)
                Column {
                    Text("2. Yatırımın Ekleneceği Hesap", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()).padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        accounts.forEach { acc ->
                            FilterChip(
                                selected = (toAccount?.id == acc.id),
                                onClick = { toAccount = acc },
                                label = { Text(acc.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanAccent,
                                    selectedLabelColor = IceWhite,
                                    labelColor = MutedSlate,
                                    containerColor = DeepSlate.copy(alpha = 0.1f)
                                )
                            )
                        }
                    }
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) quantityText = it },
                        label = { Text("Alınan Miktar (${toAccount?.currency ?: "Birim"})", color = Color.Black) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = TextStyle(color = AppBackground, fontWeight = FontWeight.Bold),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = MutedSlate
                        )
                    )
                }

                // 3. MERKEZİLEŞTİRİLMİŞ OTOMATİK TEKRAR
                RecurringSelectionFields(
                    isRecurring = isRecurring, onRecurringChange = { isRecurring = it },
                    dayOfMonth = dayOfMonth, onDayChange = { dayOfMonth = it },
                    totalMonths = totalMonths, onTotalMonthsChange = { totalMonths = it }
                )

                // 4. BÖLÜM: VARLIK DETAYLARI
                Column {
                    Text("3. Varlık Detayı ve Türü", color = Color.Black, fontSize = 12.sp)
                    OutlinedTextField(
                        value = assetName,
                        onValueChange = { assetName = it },
                        label = { Text("Açıklama (Örn: Sasa, Ata Altın)", color = Color.Black) },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        textStyle = TextStyle(color = AppBackground),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentColor,
                            unfocusedBorderColor = MutedSlate
                        )
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()).padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        assetTypes.forEach { type ->
                            FilterChip(
                                selected = (assetType == type),
                                onClick = { assetType = type },
                                label = { Text(type, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentColor,
                                    selectedLabelColor = IceWhite,
                                    labelColor = MutedSlate
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            DialogActionButtons(
                onDismiss = onDismiss,
                onConfirm = {
                    val qty = quantityText.toDoubleOrNull() ?: 0.0
                    val cost = totalCostText.toDoubleOrNull() ?: 0.0
                    val day = dayOfMonth.toIntOrNull()?.coerceIn(1, 31) ?: 1
                    val months = totalMonths.toIntOrNull() ?: 12

                    if (fromAccount != null && toAccount != null) {
                        val inv = Investment(
                            assetName = assetName.ifBlank { toAccount!!.name },
                            assetType = assetType,
                            quantity = qty,
                            totalCost = cost,
                            accountId = fromAccount!!.id,
                            targetDate = if (isRecurring) day.toString() else null
                        )
                        onConfirm(inv, toAccount!!.id, isRecurring, day, months)
                    }
                },
                confirmText = "YATIRIMI KAYDET",
                confirmColor = AccentColor, // 🔵 İndigo: Stratejik işlem rengi
                isConfirmEnabled = fromAccount != null && toAccount != null &&
                        totalCostText.isNotEmpty() && quantityText.isNotEmpty()
            )
        }
    )
}

