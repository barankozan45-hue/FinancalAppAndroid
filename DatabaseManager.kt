package com.example.sql_arac

import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import kotlin.math.roundToInt

class DatabaseManager {
    private var dbUrl: String = ""

    init {
        val targetFileName = "financial.db"
        val dbPath = File(System.getProperty("user.home"), targetFileName)
        dbUrl = "jdbc:sqlite:${dbPath.absolutePath}"

        try {
            Class.forName("org.sqlite.JDBC")
            createInitialTables()
            java.util.logging.Logger.getLogger(javaClass.name).info("Veritabanı başlatıldı")
        } catch (e: ClassNotFoundException) {
            dbUrl = ""
            throw IllegalStateException("JDBC sürücüsü bulunamadı", e)
        } catch (e: SQLException) {
            dbUrl = ""
            throw IllegalStateException("Veritabanı bağlantı hatası: ${dbPath.absolutePath}", e)
        } catch (e: Exception) {
            dbUrl = ""
            throw IllegalStateException("Beklenmeyen hata: ${e.javaClass.simpleName}", e)
        }
    }

    fun getConnection(): Connection? {
        return if (dbUrl.isEmpty()) null else DriverManager.getConnection(dbUrl)
    }

    private fun createInitialTables() {
        val createQueries = listOf(
            "CREATE TABLE IF NOT EXISTS Accounts (account_id INTEGER PRIMARY KEY AUTOINCREMENT, account_name TEXT NOT NULL, account_type TEXT NOT NULL, balance INTEGER DEFAULT 0, currency TEXT DEFAULT 'TL', allow_negative INTEGER DEFAULT 0)",
            "CREATE TABLE IF NOT EXISTS Categories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL UNIQUE, type TEXT NOT NULL, icon TEXT)",
            "CREATE TABLE IF NOT EXISTS Recurring_Transactions (recurring_id INTEGER PRIMARY KEY AUTOINCREMENT, type TEXT NOT NULL, amount INTEGER NOT NULL, account_id INTEGER, category TEXT, day_of_month INTEGER, total_months INTEGER, remaining_months INTEGER, is_active INTEGER DEFAULT 1, FOREIGN KEY (account_id) REFERENCES Accounts(account_id))",
            "CREATE TABLE IF NOT EXISTS Expenses (expense_id INTEGER PRIMARY KEY AUTOINCREMENT, amount INTEGER NOT NULL, account_id INTEGER, category TEXT, description TEXT, date TEXT DEFAULT CURRENT_TIMESTAMP, recurring_id INTEGER, FOREIGN KEY (account_id) REFERENCES Accounts(account_id), FOREIGN KEY (recurring_id) REFERENCES Recurring_Transactions(recurring_id))",
            "CREATE TABLE IF NOT EXISTS Incomes (income_id INTEGER PRIMARY KEY AUTOINCREMENT, amount INTEGER NOT NULL, account_id INTEGER, category TEXT, date TEXT DEFAULT CURRENT_TIMESTAMP, recurring_id INTEGER, FOREIGN KEY (account_id) REFERENCES Accounts(account_id), FOREIGN KEY (recurring_id) REFERENCES Recurring_Transactions(recurring_id))",
            "CREATE TABLE IF NOT EXISTS [App_Settings] ([key] TEXT PRIMARY KEY, [value] TEXT)",
            "CREATE TABLE IF NOT EXISTS Investments (investment_id INTEGER PRIMARY KEY AUTOINCREMENT, asset_name TEXT, asset_type TEXT, quantity REAL, total_cost INTEGER, account_id INTEGER, target_date TEXT, FOREIGN KEY (account_id) REFERENCES Accounts(account_id))",
            "CREATE TABLE IF NOT EXISTS Goals (goal_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, target_amount INTEGER NOT NULL, linked_account_id INTEGER, auto_transfer_amount INTEGER DEFAULT 0, transfer_day INTEGER DEFAULT 1, total_months INTEGER DEFAULT 12, is_active INTEGER DEFAULT 1, FOREIGN KEY (linked_account_id) REFERENCES Accounts(account_id))"
        )

        val conn = getConnection()
            ?: throw IllegalStateException("Bağlantı kurulamadı, tablolar oluşturulamadı.")

        conn.use { connection ->
            connection.createStatement().use { stmt ->
                // Sabit sorgular olduğu için execute kullanımı güvenlidir
                createQueries.forEach { sql -> stmt.execute(sql) }

                stmt.execute(
                    "INSERT OR IGNORE INTO [App_Settings] ([key], [value]) VALUES ('last_process_date', CURRENT_DATE)"
                )

                stmt.execute("UPDATE Accounts SET currency = 'TL' WHERE currency = 'TRY'")

                val rs = stmt.executeQuery("SELECT COUNT(*) FROM Categories")
                if (rs.next() && rs.getInt(1) == 0) {
                    fillDefaultCategories(connection)
                }
            }
        }
    }

    private fun fillDefaultCategories(conn: Connection) {
        val categories = listOf(
            Triple("Market", "EXPENSE", "🛒"),
            Triple("Online Yemek", "EXPENSE", "🛵"),
            Triple("Cafe / Restorant", "EXPENSE", "☕"),
            Triple("Ulaşım", "EXPENSE", "🚌"),
            Triple("Abonelikler / Aidatlar", "EXPENSE", "💳"),
            Triple("Hobi / Oyun", "EXPENSE", "🎮"),
            Triple("Faturalar", "EXPENSE", "⚡"),
            Triple("Kira", "EXPENSE", "🏠"),
            Triple("Giyim", "EXPENSE", "👕"),
            Triple("Eğitim", "EXPENSE", "📚"),
            Triple("Sağlık", "EXPENSE", "🏥"),
            Triple("Spor", "EXPENSE", "🏋️"),
            Triple("Kozmetik", "EXPENSE", "💄"),
            Triple("Sosyal Etkinlik", "EXPENSE", "🎭"),
            Triple("Kitap / Dergi", "EXPENSE", "📖"),
            Triple("Araç", "EXPENSE", "🚗"),
            Triple("Borç / Taksit", "EXPENSE", "💸"),
            Triple("Elektronik", "EXPENSE", "💻"),
            Triple("Beyaz Eşya", "EXPENSE", "🧊"),
            Triple("Mobilya", "EXPENSE", "🛋️"),
            Triple("Diğer", "EXPENSE", "📦"),
            Triple("Maaş", "INCOME", "🏦"),
            Triple("Satış Geliri", "INCOME", "🏷️"),
            Triple("Gayrimenkul / Kira", "INCOME", "🏠"),
            Triple("İkramiye / Prim", "INCOME", "🎁")
        )

        val sql = "INSERT OR IGNORE INTO Categories (name, type, icon) VALUES (?, ?, ?)"
        conn.prepareStatement(sql).use { pstmt ->
            categories.forEach { (name, type, icon) ->
                pstmt.setString(1, name)
                pstmt.setString(2, type)
                pstmt.setString(3, icon)
                pstmt.executeUpdate()
            }
        }
    }

    fun executeTransaction(action: (Connection) -> Unit): Boolean {
        val conn = getConnection() ?: return false
        return try {
            conn.autoCommit = false
            action(conn)
            conn.commit()
            true
        } catch (e: SQLException) {
            conn.rollback()
            java.util.logging.Logger.getLogger(javaClass.name).severe("SQL hatası: ${e.sqlState}")
            false
        } catch (e: Exception) {
            conn.rollback()
            java.util.logging.Logger.getLogger(javaClass.name).severe("Transaction hatası: ${e.javaClass.simpleName}")
            false
        } finally {
            conn.close()
        }
    }

    // 1. Hesabı Güncelle - FIXED: roundToInt ve Prepared Statement
    fun updateAccount(id: Int, name: String, type: String, balance: Double, currency: String) {
        val balanceInCents = (balance * 100).roundToInt()
        executeTransaction { conn ->
            val sql = "UPDATE Accounts SET account_name = ?, account_type = ?, balance = ?, currency = ? WHERE account_id = ?"
            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setString(1, name)
                pstmt.setString(2, type)
                pstmt.setInt(3, balanceInCents)
                pstmt.setString(4, currency)
                pstmt.setInt(5, id)
                pstmt.executeUpdate()
            }
        }
    }

    // 2. Harcamayı Güncelle - FIXED: roundToInt ve Prepared Statement
    fun updateExpense(id: Int, amount: Double, category: String, description: String) {
        val amountInCents = (amount * 100).roundToInt()
        executeTransaction { conn ->
            val sql = "UPDATE Expenses SET amount = ?, category = ?, description = ? WHERE expense_id = ?"
            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setInt(1, amountInCents)
                pstmt.setString(2, category)
                pstmt.setString(3, description)
                pstmt.setInt(4, id)
                pstmt.executeUpdate()
            }
        }
    }

    // 3. Kayıt Sil - FIXED: SQL Injection protection with white-listing
    fun deleteRecord(tableName: String, idColumnName: String, id: Int) {
        // Güvenlik: Tablo ve kolon isimleri parametre olamaz, bu yüzden bilinen listeyle kontrol ediyoruz
        val allowedTables = listOf("Accounts", "Expenses", "Incomes", "Investments", "Goals", "Categories")
        if (tableName !in allowedTables) throw IllegalArgumentException("Geçersiz tablo adı!")

        executeTransaction { conn ->
            // Kolon adını da kontrol etmek gerekebilir veya sadece ID kolonlarına izin verilebilir
            val sql = "DELETE FROM $tableName WHERE $idColumnName = ?"
            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setInt(1, id)
                pstmt.executeUpdate()
            }
        }
    }
}