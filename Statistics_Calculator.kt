package com.example.sql_arac

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

object StatisticsCalculator {

    /**
     * @param monthCount: Analiz edilecek toplam ay sayısı (Örn: 2 veya 3)
     */
    fun getTopIncreasingTrends(
        dbManager: DatabaseManager,
        inputDateStr: String,
        monthCount: Int = 3
    ): List<TrendAlert> {
        val alertList = mutableListOf<TrendAlert>()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val inputDate = LocalDate.parse(inputDateStr, formatter)

        // 1. Referans Ayı Belirle (Ayın sonu değilse bir önceki aydan başla)
        val isLastDay = inputDate == YearMonth.from(inputDate).atEndOfMonth()
        val referenceMonth = if (isLastDay) YearMonth.from(inputDate) else YearMonth.from(inputDate).minusMonths(1)

        // 2. Dinamik Tarih Aralıklarını Oluştur (M, M-1, M-2...)
        // Listeyi "en eskiden en yeniye" doğru oluşturuyoruz
        val periods = mutableListOf<Pair<String, String>>()
        for (i in (monthCount - 1) downTo 0) {
            val targetMonth = referenceMonth.minusMonths(i.toLong())
            periods.add(Pair(
                targetMonth.atDay(1).format(formatter),
                targetMonth.atEndOfMonth().format(formatter)
            ))
        }

        println("🔍 Analiz Periyodu: ${periods.first().first} ile ${periods.last().second} arası ($monthCount Ay)")

        // 3. Her ayın verisini bir listeye çek
        val monthlyData = periods.map { period ->
            StatisticsManager.getExpensesByCategory(dbManager, period.first, period.second)
        }

        // 4. Tüm kategorileri tara
        val allCategories = monthlyData.flatMap { it.keys }.distinct()

        for (cat in allCategories) {
            var isStrictlyIncreasing = true
            var totalPercentIncrease = 0.0

            // İlk ayın değerini al (Başlangıç noktası)
            var previousValue = monthlyData[0][cat] ?: 0.0

            // Eğer ilk ayda veri yoksa veya 0 ise sert filtre gereği trende dahil etmiyoruz
            if (previousValue <= 0.0) continue

            // İkinci aydan başlayarak zinciri kontrol et
            for (i in 1 until monthlyData.size) {
                val currentValue = monthlyData[i][cat] ?: 0.0

                if (currentValue > previousValue) {
                    // Artış yüzdesini hesapla ve toplama ekle
                    totalPercentIncrease += ((currentValue - previousValue) / previousValue) * 100
                    previousValue = currentValue
                } else {
                    // Zincir bir noktada kırıldıysa (düşüş veya eşitlik) iptal et
                    isStrictlyIncreasing = false
                    break
                }
            }

            if (isStrictlyIncreasing) {
                alertList.add(TrendAlert(cat, totalPercentIncrease, previousValue))
            }
        }

        // 5. En çok artan 3 taneyi döndür
        return alertList.sortedByDescending { it.totalIncrease }.take(3)
    }
}

