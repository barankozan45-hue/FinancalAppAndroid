package com.example.sql_arac

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Üst Kısım: Net Varlık Özeti
 * - Yeni Renk Sistemi: SurfaceColor & DeepSlate Border
 */
@Composable
fun SummaryCard(
    totals: Map<Pair<String, Boolean>, Double>,
    containerColor: Color = SurfaceColor // 🌑 Slate 800
) {
    val priorityOrder = listOf("TL", "YATIRIM", "ALTIN", "USD", "EURO", "BTC")

    val sortedTotals = totals.toList().sortedBy { (key, _) ->
        val (currency, isInvestment) = key
        val searchKey = if (isInvestment) "YATIRIM" else currency.uppercase()
        val index = priorityOrder.indexOf(searchKey)
        if (index == -1) 99 else index
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DeepSlate) // 🧱 Belirginlik katar
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .heightIn(max = 160.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "NET VARLIK ÖZETİ",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.2.sp,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                ),
                color = TextSecondary // 🩶 Slate 400: Göz yormayan başlık
            )

            sortedTotals.forEach { (key, amount) ->
                val (currency, isInvestment) = key
                // Gelir/Gider durumuna göre dinamik renkler
                val balanceColor = if (amount >= 0) PositiveGreen else NegativeRed

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (currency.uppercase() == "ALTIN") "GR ALTIN" else currency,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary // ⚪ Slate 50
                        )

                        if (isInvestment) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = AccentColor.copy(alpha = 0.15f), // 🔵 Indigo esintisi
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "PORTFÖY",
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    fontSize = 7.sp,
                                    color = AccentColor,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Text(
                        text = String.format("%,.2f", amount),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = balanceColor, // 🟢/🔴 Duruma göre renk
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

/**
 * Liste Öğesi: Tekil Hesap Kartı
 * - Yeni Renk Sistemi: DeepSlate Container
 */
@Composable
fun AccountItem(
    account: Account,
    onClick: () -> Unit
) {
    val icon = when (account.type) {
        AccountType.CASH -> "💵"
        AccountType.BANK_ACCOUNT -> "🏦"
        AccountType.CREDIT_CARD -> "💳"
        AccountType.INVESTMENT -> "📈"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .padding(vertical = 4.dp, horizontal = 12.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor), // 🌑 Kartlar aynı zeminde
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, DeepSlate) // 🧱 Çok ince bir ayrım
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DeepSlate, // 🌑 İkon arka planı biraz daha koyu
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(icon, fontSize = 18.sp)
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = IceWhite, // 🧊 En net beyaz
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = account.type.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary, // 🩶 Slate 400
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "BAKİYE",
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateSilver, // 🔘 Slate 500: Çok sönük ama orada
                    fontSize = 9.sp
                )

                val balanceColor = if (account.balance >= 0) PositiveGreen else NegativeRed
                Text(
                    text = String.format("%,.2f %s", account.balance, account.currency),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = balanceColor,
                    fontSize = 16.sp
                )
            }
        }
    }
}

