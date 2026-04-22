package com.example.sql_arac

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@Composable
fun DetailedFullTablePage(
    dbManager: DatabaseManager,
    accounts: List<Account>,            // ✅ passed in from caller
    refreshTrigger: Int = 0,
    initialTable: String,
    onNavigate: (String) -> Unit
) {
    val connection = remember { dbManager.getConnection() } ?: return
    val filterManager = remember(connection) { FilterManager(connection) }

    var selectedTable by remember { mutableStateOf(initialTable) }
    var startDate by remember { mutableStateOf(LocalDate.now().minusDays(30)) }
    var endDate by remember { mutableStateOf(LocalDate.now()) }
    var editingItem by remember { mutableStateOf<TransactionData?>(null) }
    var refreshKey by remember { mutableStateOf(0) }

    val transactionList = remember(selectedTable, startDate, endDate, refreshKey, refreshTrigger) {
        filterManager.getFilteredTransactions(
            tableName = selectedTable,
            startDate = startDate,
            endDate = endDate
        )
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(24.dp)
    ) {
        // --- ÜST NAVİGASYON ---
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onNavigate("DASHBOARD") },
                modifier = Modifier.background(SurfaceColor, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri", tint = IceWhite)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = when(selectedTable) {
                    "Expenses" -> "Gider Geçmişi"
                    "Incomes" -> "Gelir Geçmişi"
                    else -> "İşlem Geçmişi"
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = IceWhite
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- FİLTRELEME ÇUBUĞU ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceColor, RoundedCornerShape(16.dp))
                .border(1.dp, MutedSlate.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val badgeColor = if(selectedTable == "Expenses") ActionRed else PositiveGreen
            Surface(
                color = badgeColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f))
            ) {
                Text(
                    text = if(selectedTable == "Expenses") "🛒 Giderler" else "💰 Gelirler",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = badgeColor
                )
            }
            DailyRangeSelector { start, end -> startDate = start; endDate = end }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // --- ANA LİSTE ---
        Box(modifier = Modifier.weight(1f)) {
            if (transactionList.isEmpty()) {
                Text("Kayıt bulunamadı.", modifier = Modifier.align(Alignment.Center), color = TextSecondary)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(transactionList) { item ->
                        // 🚩 İşte hata veren fonksiyon aşağıda tanımlı!
                        DetailedRowCard(item, onEditClick = { editingItem = item })
                    }
                }
            }
        }
    }

    // Editör Dialogları
    if (editingItem != null) {
        when (editingItem?.type) {
            "Expenses" -> Expense_Buton(accounts, dbManager, editingItem) { editingItem = null; refreshKey++ }
            "Incomes" -> Income_Buton(accounts, dbManager, editingItem) { editingItem = null; refreshKey++ }
            else -> { editingItem = null }
        }
    }
}

// --- 🛠️ EKSİK OLAN FONKSİYON BURADA ---
@Composable
fun DetailedRowCard(item: TransactionData, onEditClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MutedSlate.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.categoryOrAsset, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = IceWhite)
                Spacer(modifier = Modifier.height(4.dp))
                Text(item.accountName ?: "Bilinmeyen", fontSize = 12.sp, color = AccentColor)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${String.format("%.2f", item.amount )} ₺",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = if (item.type == "Incomes") PositiveGreen else ActionRed
                )
                Text(item.date, fontSize = 11.sp, color = MutedSlate)
            }
            Spacer(modifier = Modifier.width(12.dp))
            IconButton(
                onClick = onEditClick,
                modifier = Modifier.size(32.dp).background(DeepSlate, RoundedCornerShape(8.dp))
            ) {
                Icon(Icons.Default.Edit, null, tint = AccentColor, modifier = Modifier.size(16.dp))
            }
        }
    }
}

