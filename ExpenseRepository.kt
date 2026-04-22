package com.example.sql_arac

import kotlin.math.roundToInt

class ExpenseRepository(private val dbManager: DatabaseManager) {

    fun addExpense(expense: Expense): Boolean {
        return dbManager.executeTransaction { conn ->
            val amountInCents = (expense.amount * 100).roundToInt()

            // 1. Gideri Kaydet (recurring_id sütunu eklendi)
            val insertSql = """
                INSERT INTO Expenses (account_id, amount, category, description, date, recurring_id) 
                VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent()

            conn.prepareStatement(insertSql).use { pstmt ->
                pstmt.setInt(1, expense.accountId)
                pstmt.setInt(2, amountInCents)
                pstmt.setString(3, expense.category)
                pstmt.setString(4, expense.note)
                pstmt.setString(5, expense.date)

                // recurringId modelinde varsa ekle, yoksa NULL gönder
                // Not: Expense modeline 'val recurringId: Int? = null' eklemiş olmalısın.
                if (expense.recurringId != null) {
                    pstmt.setInt(6, expense.recurringId)
                } else {
                    pstmt.setNull(6, java.sql.Types.INTEGER)
                }

                pstmt.executeUpdate()
            }

            // 2. Bakiyeyi Düş
            val updateSql = "UPDATE Accounts SET balance = balance - ? WHERE account_id = ?"
            conn.prepareStatement(updateSql).use { pstmt ->
                pstmt.setInt(1, amountInCents)
                pstmt.setInt(2, expense.accountId)
                pstmt.executeUpdate()
            }
        }
    }
}

