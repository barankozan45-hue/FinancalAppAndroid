package com.example.sql_arac

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@Composable
fun CenterBlockUI(
    dbManager: DatabaseManager,
    accountRepo: AccountRepository,
    onNavigateToStats: () -> Unit,
    onNavigateToAllTransactions: (String) -> Unit,
    refreshTrigger: Int = 0
) {
    var startDate by remember { mutableStateOf(LocalDate.now().minusDays(7)) }
    var endDate by remember { mutableStateOf(LocalDate.now()) }

    // 📉 Verileri getir ve büyüklüğe göre sırala
    val stats = remember(startDate, endDate, refreshTrigger) {
        StatisticsManager.getCategoryPercentages(dbManager, startDate.toString(), endDate.toString())
            .toList().sortedByDescending { it.second }.toMap()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        // --- ÜST SATIR: ANA BAŞLIK VE TARİH SEÇİCİ ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FİNANSAL ANALİZ VE HAREKETLER",
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )

            DailyRangeSelector { start, end ->
                startDate = start
                endDate = end
            }
        }

        // --- 1. KAT (Grafik ve Hareketler) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 🍕 1. BÖLÜM: GRAFİK PANELİ (Güncellenmiş Başlık İle)
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(160.dp)
                    .clickable { onNavigateToStats() },
                color = SurfaceColor,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, DeepSlate)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // Eklenen Başlık Bölümü
                    Text(
                        text = "KATEGORİK DAĞILIM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentColor,
                        letterSpacing = 0.4.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Grafik Alanı (Ortalanmış)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (stats.isNotEmpty()) {
                            // Grafik boyutu konteynera göre uyarlanır
                            Box(modifier = Modifier.size(80.dp)) {
                                DonutChartCanvas(stats)
                            }
                        } else {
                            Text(
                                text = "Veri Yok",
                                fontSize = 10.sp,
                                color = MutedSlate,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 📑 2. BÖLÜM: HESAP HAREKETLERİ
            Box(modifier = Modifier.weight(1f).height(160.dp)) {
                TransactionFilterCard(
                    connection = dbManager.getConnection(),
                    startDate = startDate,
                    endDate = endDate,
                    refreshTrigger = refreshTrigger,
                    onSeeAllClick = { onNavigateToAllTransactions(it) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 2. KAT (Trend ve Bakiye) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ✨ TREND KARTI
            Box(modifier = Modifier.weight(1f).height(140.dp)) {
                key(refreshTrigger) {
                    MonthlyTrendResult(dbManager = dbManager)
                }
            }

            // 💰 BAKİYE ÖZETİ
            Box(modifier = Modifier.weight(1f).height(140.dp)) {
                key(refreshTrigger) {
                    SummaryCard(
                        totals = accountRepo.getTotalBalanceByCurrency(),
                        containerColor = SurfaceColor
                    )
                }
            }
        }
    }
}