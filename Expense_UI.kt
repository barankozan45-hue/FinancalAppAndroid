package com.example.sql_arac

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseUI(
    accounts: List<Account>,
    categoryData: List<Pair<String, String>>,
    editingItem: TransactionData? = null,
    onDismiss: () -> Unit,
    onConfirm: (Account, Double, String, String, Boolean, Int, Int) -> Unit
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
        containerColor = ExpenseCardBackground, // 🌸 Yumuşak Kırmızı Zemin (Red 100)
        modifier = Modifier.padding(16.dp).fillMaxWidth(),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (editingItem == null) Icons.Default.RemoveCircle else Icons.Default.Edit,
                    contentDescription = null,
                    tint = NegativeRed, // 🔴 Canlı Kırmızı (Gideri simgeler)
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = if (editingItem == null) "Yeni Harcama Kaydı" else "Harcamayı Düzenle",
                    color = DeepMaroon, // 🍷 Ciddiyet katan Bordo başlık
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Ödeme Yapılan Hesap
                Text("Ödeme Yapılan Hesap", color = DeepMaroon, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                                selectedContainerColor = ActionRed,
                                selectedLabelColor = IceWhite,
                                labelColor = DeepMaroon,
                                containerColor = IceWhite.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

                // 2. Harcama Tutarı
                Text("Harcama Tutarı", color = DeepMaroon, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() || c == '.' }) amountText = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = TextStyle(color = DeepMaroon, fontWeight = FontWeight.Bold),
                    suffix = { Text("TL", color = DeepMaroon, fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ActionRed,
                        unfocusedBorderColor = MutedSlate,
                        focusedContainerColor = IceWhite.copy(alpha = 0.8f),
                        unfocusedContainerColor = IceWhite.copy(alpha = 0.4f)
                    )
                )

                // 3. Kategori Seçimi
                Text("Kategori", color = DeepMaroon, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Box {
                    OutlinedTextField(
                        value = selectedCategory.ifEmpty { "Seçiniz" },
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().clickable { expanded = true },
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            val icon = categoryData.find { it.first == selectedCategory }?.second ?: "💸"
                            Text(icon, modifier = Modifier.padding(start = 12.dp), fontSize = 20.sp)
                        },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, tint = DeepMaroon) },
                        textStyle = TextStyle(color = DeepMaroon, fontWeight = FontWeight.Bold),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ActionRed,
                            unfocusedBorderColor = MutedSlate,
                            focusedContainerColor = IceWhite.copy(alpha = 0.8f)
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
                                text = { Text("$icon $name", color = DeepMaroon, fontWeight = FontWeight.Bold) },
                                onClick = { selectedCategory = name; expanded = false }
                            )
                        }
                    }
                }

                // 4. Otomatik Tekrar (Buna dokunmuyorum, içindeki renkler zaten sistemden beslenmeli)
                RecurringSelectionFields(
                    isRecurring = isRecurring, onRecurringChange = { isRecurring = it },
                    dayOfMonth = dayOfMonth, onDayChange = { dayOfMonth = it },
                    totalMonths = totalMonths, onTotalMonthsChange = { totalMonths = it }
                )

                // 5. Açıklama
                Text("Açıklama", color = DeepMaroon, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ActionRed,
                        unfocusedBorderColor = MutedSlate,
                        focusedContainerColor = SettlementGreen
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
                        isRecurring, dayOfMonth.toIntOrNull() ?: 1, totalMonths.toIntOrNull() ?: 1
                    )
                },
                confirmText = if (editingItem == null) "KAYDET" else "GÜNCELLE",
                confirmColor = ActionRed, // 🔥 Harcama olduğu için kırmızı butonu çaktık!
                isConfirmEnabled = amountText.isNotBlank()
            )
        }
    )
}

