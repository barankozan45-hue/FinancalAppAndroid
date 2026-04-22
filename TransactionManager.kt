package com.example.sql_arac

import java.time.LocalDate

/**
 * Bu fonksiyon bir "Zaman Makinesi" gibi çalışır.
 * Uygulama her açıldığında kaçırılan günleri kontrol eder.
 */
fun processRecurringTransactions(dbManager: DatabaseManager) {
    dbManager.executeTransaction { conn ->
        val stmt = conn.createStatement()

        // --- IDE KIRMIZI ÇİZGİ ENGELLEYİCİ DEĞİŞKENLER ---
        val tbl = "App_Settings"
        val colKey = "key"
        val colVal = "value"
        val rowKey = "last_process_date"

        // 1. Son kontrol tarihini al
        val rs = stmt.executeQuery("SELECT [$colVal] FROM [$tbl] WHERE [$colKey] = '$rowKey'")

        if (!rs.next()) return@executeTransaction

        val lastDateStr = rs.getString(colVal)
        val lastCheckDate = LocalDate.parse(lastDateStr)
        val today = LocalDate.now()

        // Eğer bugün zaten kontrol edildiyse işlemi bitir
        if (lastCheckDate.isEqual(today)) return@executeTransaction

        // 2. Aradaki her günü tek tek tara
        var processingDate = lastCheckDate.plusDays(1)

        while (!processingDate.isAfter(today)) {
            val dayOfMonthToProcess = processingDate.dayOfMonth

            // O güne ait aktif işlemleri getir
            val recSql = "SELECT * FROM Recurring_Transactions WHERE day_of_month = ? AND is_active = 1 AND remaining_months > 0"
            val pstmt = conn.prepareStatement(recSql)
            pstmt.setInt(1, dayOfMonthToProcess)
            val rsRec = pstmt.executeQuery()

            while (rsRec.next()) {
                val recId = rsRec.getInt("recurring_id")
                val type = rsRec.getString("type")
                val amount = rsRec.getInt("amount")
                val accId = rsRec.getInt("account_id")
                val category = rsRec.getString("category")

                // A. Hesap Bakiyesini Güncelle
                val updateAccSql = if (type == "INCOME") {
                    "UPDATE Accounts SET balance = balance + ? WHERE account_id = ?"
                } else {
                    "UPDATE Accounts SET balance = balance - ? WHERE account_id = ?"
                }
                conn.prepareStatement(updateAccSql).use { pAcc ->
                    pAcc.setInt(1, amount)
                    pAcc.setInt(2, accId)
                    pAcc.executeUpdate()
                }

                // B. Kalıcı Kayıtlara (Incomes/Expenses) Ekle
                val targetTable = if (type == "INCOME") "Incomes" else "Expenses"
                val insHistory = "INSERT INTO $targetTable (amount, account_id, category, date, recurring_id) VALUES (?, ?, ?, ?, ?)"
                conn.prepareStatement(insHistory).use { pHist ->
                    pHist.setInt(1, amount)
                    pHist.setInt(2, accId)
                    pHist.setString(3, category)
                    pHist.setString(4, processingDate.toString())
                    pHist.setInt(5, recId)
                    pHist.executeUpdate()
                }

                // C. Kalan Ay Sayısını Azalt
                conn.prepareStatement("UPDATE Recurring_Transactions SET remaining_months = remaining_months - 1 WHERE recurring_id = ?")
                    .use { pRec ->
                        pRec.setInt(1, recId)
                        pRec.executeUpdate()
                    }
            }
            processingDate = processingDate.plusDays(1)
        }

        // 3. Kontrol tarihini bugüne çek
        val updateSql = "UPDATE [$tbl] SET [$colVal] = ? WHERE [$colKey] = ?"
        conn.prepareStatement(updateSql).use { pSet ->
            pSet.setString(1, today.toString())
            pSet.setString(2, rowKey)
            pSet.executeUpdate()
        }
    }
}

