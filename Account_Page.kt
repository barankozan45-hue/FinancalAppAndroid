package com.example.sql_arac

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 🏦 HESAPLAR SAYFASI
 * Receives accounts from AppState via main.kt — no direct DB calls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountPage(
    accounts: List<Account>,        // ✅ from AppState, no DB call
    onAccountClick: (Account) -> Unit,
    onAddAccountClick: () -> Unit,
    onNavigate: (String) -> Unit
) {
    // ✅ Derived locally from passed-in list — no DB call
    val totals = remember(accounts) {
        accounts.groupingBy { Pair(it.currency, it.type == AccountType.INVESTMENT) }
            .fold(0.0) { acc, element -> acc + element.balance }
    }

    // ✅ Pure derivation — no DB call
    val grandTotal = accounts.sumOf { it.balance }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddAccountClick,
                containerColor = CyanAccent,
                contentColor = AppBackground,
                shape = androidx.compose.foundation.shape.CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Hesap Ekle")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            // --- 💰 1. BÖLÜM: ÜST PANEL ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TOPLAM VARLIK",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = String.format("%,.2f ₺", grandTotal),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = IceWhite
                )
            }

            // --- 📊 2. BÖLÜM: VARLIK DAĞILIMI ---
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                SummaryCard(totals = totals)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- 🏦 3. BÖLÜM: LİSTE BAŞLIĞI ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AKTİF HESAPLAR",
                    color = IceWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Surface(
                    color = AccentColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${accounts.size} HESAP",
                        color = CyanAccent,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- 📜 4. BÖLÜM: HESAP LİSTESİ ---
            if (accounts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Henüz bir hesap eklenmemiş.", color = MutedSlate)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(accounts) { account ->
                        AccountItem(
                            account = account,
                            onClick = { onAccountClick(account) }
                        )
                    }
                }
            }
        }
    }
}