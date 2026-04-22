package com.example.sql_arac

import java.time.LocalDate

object MonthIncomeExpense {

    /**
     * @param referenceDate: Analizin başladığı tarih.
     * @param thresholdMonths: Kaç ay sonrasını görmek istiyoruz?
     * (Örn: Bugün Mart ise ve Nisan'ı görmek istiyorsak bu değer 0 olmalı,
     * çünkü 0'dan büyük kalan ayı olan her şey Nisan'da aktiftir.)
     */
    fun calculateNextMonthNetFlow(
        dbManager: DatabaseManager,
        referenceDate: LocalDate = LocalDate.now(),
        thresholdMonths: Int = 0 // 🎯 Sabit 0'ı değişkene taşıdık
    ): NetFlowResult {
        var totalPlannedIncome = 0.0
        var totalPlannedExpense = 0.0

        dbManager.getConnection()?.use { conn ->
            // SQL'de 0 yerine ? (parametre) kullanıyoruz
            val sql = """
                SELECT type, amount FROM Recurring_Transactions 
                WHERE is_active = 1 AND remaining_months > ?
            """.trimIndent()

            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setInt(1, thresholdMonths) // 🎯 Dışarıdan gelen eşiği bağladık
                val rs = pstmt.executeQuery()

                while (rs.next()) {
                    val type = rs.getString("type")
                    val amountLira = rs.getInt("amount") / 100.0

                    when (type.uppercase()) {
                        "INCOME" -> totalPlannedIncome += amountLira
                        "EXPENSE", "INVESTMENT" -> totalPlannedExpense += amountLira
                    }
                }
            }
        }

        return NetFlowResult(
            plannedIncome = totalPlannedIncome,
            plannedExpense = totalPlannedExpense,
            netFlow = totalPlannedIncome - totalPlannedExpense
        )
    }
}

data class NetFlowResult(
    val plannedIncome: Double,
    val plannedExpense: Double,
    val netFlow: Double
)

