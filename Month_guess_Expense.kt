package com.example.sql_arac

import java.time.LocalDate
import java.time.YearMonth

object MonthGuessExpense {

    /**
     * @param referenceDate: Analizin başlayacağı referans noktası (Varsayılan: Bugün)
     */
    fun predictNextMonthExpense(dbManager: DatabaseManager, referenceDate: LocalDate = LocalDate.now()): PredictionResult {

        // 1. ADIM: Referans tarihe göre Hedef Ay ve Geçmiş 3 Ayı belirle
        // 🎯 Burayı düzelttik: Artık her şey referenceDate üzerinden hesaplanıyor
        val targetMonth = referenceDate.plusMonths(1)
        val last3Months = (1..3).map { referenceDate.minusMonths(it.toLong()) }

        val dailyAverages = mutableListOf<Double>()

        dbManager.getConnection()?.use { conn ->
            last3Months.forEach { date ->
                val year = date.year
                val month = date.monthValue
                val daysInMonth = YearMonth.of(year, month).lengthOfMonth()

                // SQL: Belirli bir ayın toplam giderini çek
                val sql = """
                    SELECT SUM(amount) FROM Expenses 
                    WHERE strftime('%Y', date) = ? 
                    AND strftime('%m', date) = ?
                """.trimIndent()

                conn.prepareStatement(sql).use { pstmt ->
                    pstmt.setString(1, year.toString())
                    pstmt.setString(2, String.format("%02d", month))
                    val rs = pstmt.executeQuery()

                    if (rs.next()) {
                        val totalCents = rs.getLong(1) // Güvenlik için Long
                        val totalLira = totalCents / 100.0

                        if (totalLira > 0) {
                            dailyAverages.add(totalLira / daysInMonth)
                        }
                    }
                }
            }
        }

        // 2. ADIM: Ortalama Günlük Gider
        val finalDailyAverage = if (dailyAverages.isNotEmpty()) {
            dailyAverages.sum() / dailyAverages.size
        } else {
            0.0
        }

        // 3. ADIM: Gelecek Ayın Gün Sayısıyla Çarp
        val daysInTargetMonth = YearMonth.of(targetMonth.year, targetMonth.monthValue).lengthOfMonth()
        val predictedExpense = finalDailyAverage * daysInTargetMonth

        return PredictionResult(
            targetMonthName = getTurkishMonthName(targetMonth.monthValue),
            predictedAmount = predictedExpense,
            dailyAverage = finalDailyAverage,
            dataPointsCount = dailyAverages.size
        )
    }

    private fun getTurkishMonthName(month: Int): String {
        return listOf(
            "", "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
            "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
        )[month]
    }
}

data class PredictionResult(
    val targetMonthName: String,
    val predictedAmount: Double,
    val dailyAverage: Double,
    val dataPointsCount: Int
)

