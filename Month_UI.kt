package com.example.sql_arac

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


/**
 * Gelecek Ay Sonu Beklentisi Kartı - Sadeleştirilmiş Versiyon
 * Renk: Açık Mavi
 */
@Composable
fun ProjectionSummaryCard(result: ProjectionResult) {
    // İstediğin açık mavi tonu (Light Blue 50)
    val lightBlue = Color(0xFF2073B3)

    Card(
        modifier = Modifier
            .fillMaxWidth() //
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = lightBlue),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            // 1. BAŞLIK
            Text(
                text = "${result.targetMonthName} Ayı Projeksiyonu",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF092C53) // Biraz daha belirgin bir mavi
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. DETAYLAR (İstediğin Güncellemeler Yapıldı)
            // Mevcut Nakit satırı silindi.
            DetailRow("Planlanan Net Gelir", "+ ${String.format("%.2f", result.fixedNetFlow)} ₺")
            DetailRow("Tahmini Gider", "- ${String.format("%.2f", result.predictedExpense)} ₺")
            DetailRow("Bu Ayın Karnesi", "${String.format("%.0f", result.futureNet)} ₺")
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 1.dp, // Kalınlığı belirtebilirsin
                color = MutedSlate //
            )

            // 3. NİHAİ BEKLENTİ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Beklenen Bakiye:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${String.format("%.2f", result.finalExpectation)} ₺",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    // Artıdaysa koyu yeşil, eksideyse kırmızı
                    color = if (result.finalExpectation >= 0) DeepForestGreen else Color.Red
                )
            }
        }
    }
}

/**
 * Satır düzeni için yardımcı fonksiyon
 */
@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, color = Color(0xFF455A64))
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}
