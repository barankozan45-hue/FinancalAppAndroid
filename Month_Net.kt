    package com.example.sql_arac

    import java.time.LocalDate
    // 🎯 DİKKAT: Eğer bu dosyalar farklı paketlerdeyse importlarını eklemelisin.
    // Aynı pakette (com.example.sql_arac) olduklarını varsayıyorum:

    /**
     * Gelecek Ay Sonu Kasa Beklentisi Sonuç Modeli
     */
    data class ProjectionResult(
        val currentCash: Double,
        val fixedNetFlow: Double,
        val predictedExpense: Double,
        val futureNet: Double, // 🎯 Yeni eklenen: Ayın kendi içindeki kar/zararı
        val finalExpectation: Double,
        val targetMonthName: String
    )

    object MonthNet {

        fun calculateMonthEndProjection(
            dbManager: DatabaseManager,
            accountRepo: AccountRepository,
            baseDate: LocalDate = LocalDate.now()
        ): ProjectionResult {

            // 1. MEVCUT NAKİT DURUMU
            val currentBalances = accountRepo.getTotalBalanceByCurrency()
            val totalDisposableCash = currentBalances
                .filter { (key, _) ->
                    val (currency, isInvestment) = key
                    (currency == "TL" || currency == "₺") && !isInvestment
                }
                .values
                .sum()

            // 2. GELECEK SABİT PLAN
            // thresholdMonths parametresini eklemiştik, varsayılan 0 olarak gidecek
            val nextMonthFixedFlow = MonthIncomeExpense.calculateNextMonthNetFlow(dbManager, baseDate)

            // 3. GEÇMİŞTEN GELEN GİDER TAHMİNİ
            val prediction = MonthGuessExpense.predictNextMonthExpense(dbManager, baseDate)

            // 4. NİHAİ HESAPLAMA
            val futureNetValue = nextMonthFixedFlow.netFlow - prediction.predictedAmount
            val futureBalance = totalDisposableCash + futureNetValue // Formül daha sadeleşti

            return ProjectionResult(
                currentCash = totalDisposableCash,
                fixedNetFlow = nextMonthFixedFlow.netFlow,
                predictedExpense = prediction.predictedAmount,
                futureNet = futureNetValue,
                finalExpectation = futureBalance,
                targetMonthName = prediction.targetMonthName
            )
        }
    }

