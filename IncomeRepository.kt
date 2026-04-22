package com.example.sql_arac

import kotlin.math.roundToInt

class IncomeRepository(private val dbManager: DatabaseManager) {

    fun addIncome(income: Income): Boolean {
        return dbManager.executeTransaction { conn ->
            // Parasal işlemler için kuruş hesabı
            val amountInCents = (income.amount * 100).roundToInt()

            // 1. Geliri Kaydet
            // Sütunlar: amount, account_id, category, date, recurring_id
            val insertSql = "INSERT INTO Incomes (amount, account_id, category, date, recurring_id) VALUES (?, ?, ?, ?, ?)"

            conn.prepareStatement(insertSql).use { pstmt ->
                pstmt.setInt(1, amountInCents)
                pstmt.setInt(2, income.accountId)
                pstmt.setString(3, income.category)
                pstmt.setString(4, income.date)

                // recurringId null olabilir (Opsiyonel alan yönetimi)
                if (income.recurringId != null) {
                    pstmt.setInt(5, income.recurringId)
                } else {
                    pstmt.setNull(5, java.sql.Types.INTEGER)
                }

                pstmt.executeUpdate()
            }

            // 2. Accounts Tablosundaki Bakiyeyi Artır
            val updateSql = "UPDATE Accounts SET balance = balance + ? WHERE account_id = ?"

            conn.prepareStatement(updateSql).use { pstmt ->
                pstmt.setInt(1, amountInCents)
                pstmt.setInt(2, income.accountId)
                pstmt.executeUpdate()
            }
        }
    }
}

