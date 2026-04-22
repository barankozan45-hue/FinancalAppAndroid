package com.example.sql_arac

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.sql.Connection
import java.time.LocalDate

@Composable
fun TransactionFilterCard(
    modifier: Modifier = Modifier,
    connection: Connection?,
    startDate: LocalDate?,
    endDate: LocalDate?,
    refreshTrigger: Int = 0,
    onSeeAllClick: (String) -> Unit
) {
    var selectedTable by remember { mutableStateOf("Expenses") }
    var expanded by remember { mutableStateOf(false) }

    val filterManager = remember(connection) { connection?.let { FilterManager(it) } }

    // Verileri çekme mantığı
    val transactions = remember(selectedTable, startDate, endDate, refreshTrigger) {
        filterManager?.getFilteredTransactions(selectedTable, startDate, endDate)?.take(5) ?: emptyList()
    }

    Card(
        modifier = modifier
            .width(300.dp)
            .height(200.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor), // 🌑 Slate 800
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- ÜST SATIR: BAŞLIK VE SEÇİCİ ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Son Hareketler",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = IceWhite // 🧊 Net beyaz
                )

                // Tablo Seçici (Dropdown)
                Box {
                    Surface(
                        color = DeepSlate, // 🌑 Slate 700
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { expanded = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = when(selectedTable) {
                                    "Expenses" -> "Gider"
                                    "Incomes" -> "Gelir"
                                    else -> "Yatırım"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentColor // 🔵 Indigo Vurgu
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = AccentColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(DeepSlate)
                    ) {
                        listOf("Expenses" to "Giderler", "Incomes" to "Gelirler", "Investments" to "Yatırımlar").forEach { (key, label) ->
                            DropdownMenuItem(
                                text = { Text(label, color = IceWhite, fontSize = 13.sp) },
                                onClick = { selectedTable = key; expanded = false }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- LİSTE ALANI ---
            Box(modifier = Modifier.weight(1f)) {
                if (connection == null) {
                    Text("Veri Bağlantısı Kesildi", fontSize = 11.sp, color = ActionRed)
                } else {
                    // Not: SmallSummaryTable içindeki metinlerin IceWhite/TextSecondary olması iyi olur
                    SmallSummaryTable(items = transactions, selectedTable = selectedTable)
                }
            }

            // --- ALT SATIR: TÜMÜNÜ GÖR ---
            Text(
                text = "Tümünü Gör →",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AccentColor,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { onSeeAllClick(selectedTable) }
                    .padding(top = 4.dp)
            )
        }
    }
}

