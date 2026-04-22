package com.example.sql_arac

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.util.*

@Composable
fun MonthlyTrendResult(dbManager: DatabaseManager) {
    // 1. Referans Tarihi
    val referenceDate = LocalDate.now().minusMonths(1).withDayOfMonth(1)
    val monthName = referenceDate.month.getDisplayName(
        java.time.format.TextStyle.FULL,
        Locale.forLanguageTag("tr")
    ).uppercase()

    // 2. Trend Hesaplamaları
    val trendM2 = remember {
        StatisticsCalculator.getTopIncreasingTrends(dbManager, referenceDate.toString(), 2)
    }

    val trendM3 = remember {
        StatisticsCalculator.getTopIncreasingTrends(dbManager, referenceDate.toString(), 3)
    }

    // TASARIM: Eflatun Zeminli Kart
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp), // Dashboard içine sığması için paddingleri daralttık
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF8F4FEF) // Şık Eflatun
        ),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // --- BAŞLIK ---
            Text(
                text = "$monthName ANALİZİ",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Red.copy(alpha = 0.7f) // Eflatun üstünde beyaz başlık daha iyi durur
            )

            Spacer(modifier = Modifier.height(6.dp))

            // --- SON AYDA ARTIŞ YAPANLAR ---
            if (trendM2.isNotEmpty()) {
                TrendRowText(
                    label = "Aylık Artışlar:",
                    trends = trendM2
                )
            }

            // --- SON İKİ AYDIR ARTIŞ YAPANLAR ---
            if (trendM3.isNotEmpty()) {
                if (trendM2.isNotEmpty()) Spacer(modifier = Modifier.height(8.dp))
                TrendRowText(
                    label = "Sürekli Artışlar (2 Ay):",
                    trends = trendM3
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrendRowText(label: String, trends: List<TrendAlert>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Alt Başlık (Etiket)
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0A0A0A).copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 2.dp)
        )

        // Veri Kısmı: Artık Metin Değil, Rozetler (FlowRow)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            trends.forEach { trend ->
                TrendChip(trend)
            }
        }
    }
}

@Composable
fun TrendChip(trend: TrendAlert) {
    // Kategori rengini alıyoruz (Hata payına karşı gri fallback)
    val catColor = try { getCategoryColor(trend.category) } catch (e: Exception) { Color.Gray }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                color = Color.White.copy(alpha = 0.25f), // Eflatun üstünde daha iyi görünmesi için hafif beyaz transparan
                shape = RoundedCornerShape(6.dp)
            )
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.4f),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        // Kategori Rengi Noktası
        Box(
            modifier = Modifier
                .size(5.dp)
                .background(catColor, CircleShape)
        )

        Spacer(modifier = Modifier.width(4.dp))

        // Kategori ve Oran
        Text(
            text = "${trend.category} %${trend.totalIncrease.toInt()}",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFCA1414) // Artış vurgusu için kırmızı kalabilir
        )
    }
}

