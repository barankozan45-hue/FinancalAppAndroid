package com.example.sql_arac

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@Composable
fun RecurringApprovalDialog(
    pendingTransactions: List<Pair<RecurringTransaction, LocalDate>>,
    onConfirm: (List<Pair<RecurringTransaction, LocalDate>>) -> Unit,
    onDismiss: () -> Unit
) {
    // Seçili işlemleri tutan liste
    val selectedItems = remember {
        mutableStateListOf<Pair<RecurringTransaction, LocalDate>>().apply {
            addAll(pendingTransactions)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = IceWhite,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Text(
                    text = "Bekleyen İşlemler",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppBackground
                )
                Text(
                    text = "Otomatik tekrarlanan kayıtları onayla",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.height(8.dp))

                Surface(
                    color = DeepSlate.copy(alpha = 0.03f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.heightIn(max = 320.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(pendingTransactions) { item ->
                            val (plan, date) = item
                            val isChecked = selectedItems.contains(item)
                            val amountColor = if (plan.type == "INCOME") PositiveGreen else ActionRed

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isChecked) AccentColor.copy(alpha = 0.05f) else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        if (isChecked) {
                                            if (selectedItems.size > 1) selectedItems.remove(item)
                                        } else {
                                            selectedItems.add(item)
                                        }
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedItems.add(item)
                                        else if (selectedItems.size > 1) selectedItems.remove(item)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = AccentColor,
                                        uncheckedColor = MutedSlate
                                    )
                                )

                                Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
                                    Text(
                                        text = plan.category,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = AppBackground
                                    )
                                    Text(
                                        text = "${date.dayOfMonth} ${date.month.name.lowercase()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }

                                Text(
                                    text = String.format("${if (plan.type == "INCOME") "+" else "-"} %,.0f ₺", plan.amount),
                                    color = amountColor,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = DeepSlate.copy(alpha = 0.1f))
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Onay bekleyen: ${pendingTransactions.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )

                    TextButton(
                        onClick = {
                            if (selectedItems.size == pendingTransactions.size) {
                                // En az bir tane seçili kalsın diye sıfırlama yerine ilkini bırakabiliriz
                                // veya tamamen temizleyebilirsin (Confirm butonun enabled kontrolüne bağlı)
                                selectedItems.clear()
                            } else {
                                selectedItems.clear()
                                selectedItems.addAll(pendingTransactions)
                            }
                        }
                    ) {
                        val label = if (selectedItems.size == pendingTransactions.size) "Temizle" else "Hepsini Seç"
                        Text(label, color = AccentColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedItems.toList()) },
                enabled = selectedItems.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Onayla (${selectedItems.size})", fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Vazgeç", color = TextSecondary, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

