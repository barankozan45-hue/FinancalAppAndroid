package com.example.sql_arac

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncomeDialog(
    accounts: List<Account>,
    categoryData: List<Pair<String, String>>,
    editingItem: TransactionData? = null,
    onDismiss: () -> Unit,
    onConfirm: (account: Account, amount: Double, category: String, description: String, isRecurring: Boolean, dayOfMonth: Int, totalMonths: Int) -> Unit
) {
    var selectedAccount by remember {
        mutableStateOf(accounts.find { it.name == editingItem?.accountName } ?: accounts.first())
    }

    var amountText by remember {
        mutableStateOf(editingItem?.amount?.toString() ?: "")
    }

    var selectedCategory by remember {
        mutableStateOf(editingItem?.categoryOrAsset ?: "")
    }

    var isRecurring by remember {
        mutableStateOf(editingItem?.isRecurring ?: false)
    }

    var descriptionText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var dayOfMonth by remember { mutableStateOf("1") }
    var totalMonths by remember { mutableStateOf("12") }

    LaunchedEffect(categoryData) {
        if (categoryData.isNotEmpty() && selectedCategory.isEmpty()) {
            selectedCategory = categoryData.first().first
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PositiveGreen,
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (editingItem == null) Icons.Default.Upgrade else Icons.Default.Edit,
                    contentDescription = null,
                    tint = PositiveGreen, // 🟢 Daha canlı bir yeşil
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = if (editingItem == null) "Yeni Gelir Kaydı" else "Geliri Düzenle",
                    color = IceWhite, // ⚪ Yazı artık bembeyaz ve net
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- 1. HESAP SEÇİMİ ---
                Text("Gelir Hesabı", color = MutedSlate, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    accounts.forEach { acc ->
                        FilterChip(
                            selected = (selectedAccount.id == acc.id),
                            onClick = { selectedAccount = acc },
                            label = { Text(acc.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SettlementGreen,
                                selectedLabelColor = IceWhite,
                                labelColor = MutedSlate,
                                containerColor = DeepSlate.copy(alpha = 0.1f)
                            )
                        )
                    }
                }

                // --- 2. TUTAR GİRİŞİ ---
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() || c == '.' }) amountText = it },
                    label = { Text("Miktar (₺)", color = MutedSlate) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = TextStyle(
                        color = Color.Black, // 🟢 Para girişi yeşil parlasın
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SettlementGreen,
                        unfocusedBorderColor = MutedSlate,
                        focusedLabelColor = SettlementGreen
                    )
                )

                // --- 3. KATEGORİ SEÇİMİ ---
                Box {
                    OutlinedTextField(
                        value = selectedCategory.ifEmpty { "Seçiniz" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategori", color = MutedSlate) },
                        leadingIcon = {
                            val icon = categoryData.find { it.first == selectedCategory }?.second ?: "💰"
                            Text(icon, modifier = Modifier.padding(start = 12.dp), fontSize = 20.sp)
                        },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = SettlementGreen) },
                        modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                        shape = RoundedCornerShape(12.dp),
                        textStyle = TextStyle(color = AppBackground, fontWeight = FontWeight.Bold),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SettlementGreen,
                            unfocusedBorderColor = MutedSlate
                        )
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { expanded = true })
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(IceWhite)
                    ) {
                        categoryData.forEach { (name, icon) ->
                            DropdownMenuItem(
                                text = { Text("$icon $name", color = AppBackground, fontWeight = FontWeight.Bold) },
                                onClick = { selectedCategory = name; expanded = false }
                            )
                        }
                    }
                }

                // --- 4. TEKRAR BÖLÜMÜ ---
                RecurringSelectionFields(
                    isRecurring = isRecurring, onRecurringChange = { isRecurring = it },
                    dayOfMonth = dayOfMonth, onDayChange = { dayOfMonth = it },
                    totalMonths = totalMonths, onTotalMonthsChange = { totalMonths = it }
                )

                // --- 5. AÇIKLAMA ---
                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    label = { Text("Açıklama (Opsiyonel)", color = Color.Black) }, // 🌫️ Daha yumuşak bir gri
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    // ✍️ YAZI STİLİ: Burada rengi netleştirelim
                    textStyle = TextStyle(
                        color = Color.Black, // ⚪ Koyu zeminde bembeyaz parlasın
                        fontSize = 16.sp
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = IceWhite,    // Yazarken beyaz
                        unfocusedTextColor = TextPrimary, // Dururken açık gri
                        focusedBorderColor = CyanAccent, // ✨ Odaklanınca turkuaz parlasın (Daha şık!)
                        unfocusedBorderColor = DeepSlate, // Pasifken koyu gri
                        cursorColor = CyanAccent        // Yazma imleci bile uyumlu olsun
                    )
                )
            }
        },
        confirmButton = {
            DialogActionButtons(
                onDismiss = onDismiss,
                onConfirm = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    onConfirm(
                        selectedAccount, amount, selectedCategory, descriptionText,
                        isRecurring, dayOfMonth.toIntOrNull()?.coerceIn(1, 31) ?: 1,
                        totalMonths.toIntOrNull() ?: 1
                    )
                },
                confirmText = if (editingItem == null) "KAYDET" else "GÜNCELLE",
                confirmColor = SettlementGreen, // 🔥 Gelir olduğu için güven veren yeşil buton
                isConfirmEnabled = amountText.isNotEmpty()
            )
        }
    )
}

