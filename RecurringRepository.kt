package com.example.sql_arac

import java.time.LocalDate
import java.sql.ResultSet
import kotlin.math.roundToInt

class RecurringRepository(private val dbManager: DatabaseManager) {

    /**
     * ResultSet'ten gelen veriyi RecurringTransaction nesnesine dönüştüren yardımcı metot.
     * Bu sayede kod tekrarını (Duplicated code) engelliyoruz.
     */
    private fun mapResultSetToTransaction(rs: ResultSet): RecurringTransaction {
        return RecurringTransaction(
            id = rs.getInt("recurring_id"),
            type = rs.getString("type"),
            amount = rs.getInt("amount") / 100.0,
            accountId = rs.getInt("account_id"),
            targetAccountId = rs.getInt("target_account_id").let { if (rs.wasNull()) null else it },
            category = rs.getString("category"),
            dayOfMonth = rs.getInt("day_of_month"),
            totalMonths = rs.getInt("total_months"),
            remainingMonths = rs.getInt("remaining_months"),
            isActive = rs.getInt("is_active") == 1
        )
    }

    /**
     * Yeni bir düzenli işlem planı ekler.
     */
    fun addRecurringPlan(plan: RecurringTransaction): Boolean {
        return dbManager.executeTransaction { conn ->
            val sql = """
            INSERT INTO Recurring_Transactions 
            (type, amount, account_id, target_account_id, category, day_of_month, total_months, remaining_months, is_active) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setString(1, plan.type)
                pstmt.setInt(2, (plan.amount * 100).roundToInt())
                pstmt.setInt(3, plan.accountId)

                if (plan.targetAccountId != null) pstmt.setInt(4, plan.targetAccountId)
                else pstmt.setNull(4, java.sql.Types.INTEGER)

                pstmt.setString(5, plan.category)
                pstmt.setInt(6, plan.dayOfMonth)
                pstmt.setInt(7, plan.totalMonths)
                pstmt.setInt(8, plan.remainingMonths)
                pstmt.setInt(9, if (plan.isActive) 1 else 0) // Sabit 1 yerine modelden aldık

                pstmt.executeUpdate() > 0
            }
        }
    }

    /**
     * Son açılıştan bugüne kadar olan tüm bekleyen otomatik işlemleri listeler.
     */
    fun getPendingTransactions(): List<Pair<RecurringTransaction, LocalDate>> {
        val pendingList = mutableListOf<Pair<RecurringTransaction, LocalDate>>()

        dbManager.getConnection()?.use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeQuery("SELECT value FROM App_Settings WHERE key = 'last_process_date'").use { rsDate ->
                    if (!rsDate.next()) return@use

                    val lastCheckDate = LocalDate.parse(rsDate.getString("value"))
                    val today = LocalDate.now()
                    if (lastCheckDate.isEqual(today)) return@use

                    var checkDate = lastCheckDate.plusDays(1)
                    while (!checkDate.isAfter(today)) {
                        val day = checkDate.dayOfMonth
                        val sql = "SELECT * FROM Recurring_Transactions WHERE day_of_month = ? AND is_active = 1 AND remaining_months > 0"

                        conn.prepareStatement(sql).use { pstmt ->
                            pstmt.setInt(1, day)
                            pstmt.executeQuery().use { rsRec ->
                                while (rsRec.next()) {
                                    pendingList.add(mapResultSetToTransaction(rsRec) to checkDate)
                                }
                            }
                        }
                        checkDate = checkDate.plusDays(1)
                    }
                }
            }
        }
        return pendingList
    }

    /**
     * Onaylananları işler.
     */
    fun processApprovedTransactions(approvedList: List<Pair<RecurringTransaction, LocalDate>>) {
        dbManager.executeTransaction { conn ->
            approvedList.forEach { (plan, date) ->
                val amountInCents = (plan.amount * 100).roundToInt()

                when (plan.type) {
                    "INCOME" -> {
                        updateBalance(conn, plan.accountId, amountInCents, true)
                        insertHistory(conn, "Incomes", plan, date, amountInCents)
                    }
                    "EXPENSE" -> {
                        updateBalance(conn, plan.accountId, amountInCents, false)
                        insertHistory(conn, "Expenses", plan, date, amountInCents)
                    }
                    "INVESTMENT" -> {
                        updateBalance(conn, plan.accountId, amountInCents, false)
                        plan.targetAccountId?.let { targetId ->
                            updateBalance(conn, targetId, amountInCents, true)
                        }
                        insertHistory(conn, "Investments", plan, date, amountInCents)
                    }
                }

                conn.prepareStatement("UPDATE Recurring_Transactions SET remaining_months = remaining_months - 1 WHERE recurring_id = ?")
                    .use { pstmt ->
                        pstmt.setInt(1, plan.id)
                        pstmt.executeUpdate()
                    }
            }
            updateLastProcessDateToToday()
        }
    }

    // --- YARDIMCI METOTLAR ---

    private fun updateBalance(conn: java.sql.Connection, accId: Int, amount: Int, increase: Boolean) {
        val sql = "UPDATE Accounts SET balance = balance ${if (increase) "+" else "-"} ? WHERE account_id = ?"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setInt(1, amount)
            pstmt.setInt(2, accId)
            pstmt.executeUpdate()
        }
    }

    private fun insertHistory(conn: java.sql.Connection, table: String, plan: RecurringTransaction, date: LocalDate, amount: Int) {
        val sql = "INSERT INTO $table (amount, account_id, category, date, recurring_id) VALUES (?, ?, ?, ?, ?)"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setInt(1, amount)
            pstmt.setInt(2, plan.accountId)
            pstmt.setString(3, plan.category)
            pstmt.setString(4, date.toString())
            pstmt.setInt(5, plan.id)
            pstmt.executeUpdate()
        }
    }

    fun updateLastProcessDateToToday() {
        dbManager.executeTransaction { conn ->
            val updateSettings = "UPDATE App_Settings SET value = ? WHERE key = 'last_process_date'"
            conn.prepareStatement(updateSettings).use { pstmt ->
                pstmt.setString(1, LocalDate.now().toString())
                pstmt.executeUpdate()
            }
        }
    }

    fun getActivePlans(): List<RecurringTransaction> {
        val list = mutableListOf<RecurringTransaction>()
        dbManager.getConnection()?.use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeQuery("SELECT * FROM Recurring_Transactions WHERE is_active = 1").use { rs ->
                    while (rs.next()) {
                        list.add(mapResultSetToTransaction(rs))
                    }
                }
            }
        }
        return list
    }
}

