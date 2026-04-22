package com.example.sql_arac

import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * 12 Aylık Projeksiyon Satır Modeli
 */
data class MonthlyForecastRow(
    val monthName: String,
    val openingBalance: Double,
    val plannedIncome: Double,
    val predictedExpense: Double,
    val extraIncome: Double,
    val extraExpense: Double,
    val closingBalance: Double
)

/**
 * Kullanıcının dışarıdan girdiği simülasyon verileri
 */
data class SimulationInput(
    val amount: Double,
    val startMonthOffset: Int, // 1 = Bu ay, 2 = Gelecek ay...
    val totalMonths: Int,      // Süre/Taksit sayısı
    val isIncome: Boolean,
    val isInstallment: Boolean // Giderse Tutar/Taksit, Gelirse her ay tam Tutar
)

object ForecastFuture {

    fun calculate12MonthProjection(
        dbManager: DatabaseManager,
        accountRepo: AccountRepository,
        externalInputs: List<SimulationInput>,
        inflationValue: Double = 0.0,
        inflationMode: Int = 0, // 🎯 YENİ: 0=Aylık, 1=Yıllık Bileşik, 2=Yıllık Eşit
        baseDate: LocalDate = LocalDate.now()
    ): List<MonthlyForecastRow> {

        val forecastList = mutableListOf<MonthlyForecastRow>()
        val trLocale = Locale.forLanguageTag("tr")

        // 1. ADIM: Kasa Başlangıcı
        val currentBalances = accountRepo.getTotalBalanceByCurrency()
        var runningBalance = currentBalances
            .filter { (key, _) -> (key.first == "TL" || key.first == "₺") && !key.second }
            .values.sum()

        // 🎯 2. ADIM: Çarpanı Seçilen Moda Göre Belirle
        // Enflasyon.kt artık 3 farklı senaryoya göre tek bir 'e' çarpanı döndürüyor.
        val e = Enflasyon.getAylikCarpan(inflationValue, inflationMode)

        // 3. ADIM: Başlangıç Gideri
        var currentPredictedExpense = MonthGuessExpense.predictNextMonthExpense(dbManager, baseDate).predictedAmount

        for (i in 1..12) {
            val targetDate = baseDate.plusMonths(i.toLong())
            val monthLabel = targetDate.month.getDisplayName(TextStyle.FULL, trLocale)
                .replaceFirstChar { it.uppercase(trLocale) }

            // A) Sabit Planlar
            val fixedFlow = MonthIncomeExpense.calculateNextMonthNetFlow(dbManager, baseDate, thresholdMonths = i - 1)

            // 🎯 B) KÜMÜLATİF GÜNCELLEME
            // i=1 (Bu ay) mevcut tahmin kullanılır.
            // i=2 ve sonrası için; gider = bir önceki ayın gideri * e
            if (i > 1) {
                currentPredictedExpense = Enflasyon.sonrakiAyGideri(currentPredictedExpense, e)
            }

            // C) Ek Simülasyonlar (External Inputs)
            var monthlyExtraIncome = 0.0
            var monthlyExtraExpense = 0.0

            externalInputs.forEach { input ->
                val endMonth = input.startMonthOffset + input.totalMonths
                if (i >= input.startMonthOffset && i < endMonth) {
                    if (input.isIncome) {
                        monthlyExtraIncome += input.amount
                    } else {
                        if (input.isInstallment && input.totalMonths > 0) {
                            monthlyExtraExpense += (input.amount / input.totalMonths)
                        } else if (i == input.startMonthOffset) {
                            monthlyExtraExpense += input.amount
                        }
                    }
                }
            }

            // D) Kasa Hesaplama
            val monthOpening = runningBalance
            val monthClosing = monthOpening +
                    (fixedFlow.plannedIncome + monthlyExtraIncome) -
                    (currentPredictedExpense + monthlyExtraExpense)

            // E) Satırı Ekle
            forecastList.add(
                MonthlyForecastRow(
                    monthName = "$monthLabel ${targetDate.year}",
                    openingBalance = monthOpening,
                    plannedIncome = fixedFlow.plannedIncome,
                    predictedExpense = currentPredictedExpense,
                    extraIncome = monthlyExtraIncome,
                    extraExpense = monthlyExtraExpense,
                    closingBalance = monthClosing
                )
            )

            // F) Devret
            runningBalance = monthClosing
        }

        return forecastList
    }
}

