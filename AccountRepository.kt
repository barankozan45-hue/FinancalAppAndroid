package com.example.sql_arac

import java.sql.SQLException
import kotlin.math.roundToInt


class AccountRepository(private val dbManager: DatabaseManager) {

    /**
     * Tüm hesapları çeker.
     */
    fun getAllAccounts(): List<Account> {
        val list = mutableListOf<Account>()
        try {
            dbManager.getConnection()?.use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery("SELECT * FROM Accounts ORDER BY account_name").use { rs ->
                        while (rs.next()) {
                            list.add(Account(
                                id = rs.getInt("account_id"),
                                name = rs.getString("account_name"),
                                type = try {
                                    AccountType.valueOf(rs.getString("account_type").uppercase())
                                } catch (e: IllegalArgumentException) {
                                    AccountType.CASH
                                },
                                balance = rs.getInt("balance") / 100.0,
                                currency = rs.getString("currency")
                            ))
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            java.util.logging.Logger.getLogger(javaClass.name).severe("Hesaplar çekilirken SQL hatası")
        } catch (e: Exception) {
            java.util.logging.Logger.getLogger(javaClass.name).severe("Hesaplar çekilirken hata: ${e.javaClass.simpleName}")
        }
        return list
    }

    /**
     * YENİ FONKSİYON: Varlıkları para birimine göre gruplayıp toplar.
     * Çıktı Örneği: { "TL" -> 500.0, "USD" -> 60.0, "ALTIN" -> 12.5 }
     */
    fun getTotalBalanceByCurrency(): Map<Pair<String, Boolean>, Double> {
        val accounts = getAllAccounts()
        // Hem para birimine hem de yatırım hesabı olup olmadığına göre grupla
        return accounts.groupingBy { Pair(it.currency, it.type == AccountType.INVESTMENT) }
            .fold(0.0) { acc, element -> acc + element.balance }
    }
    fun getAccountBalance(accountId: Int): Double {
        var balance = 0.0
        try {
            dbManager.getConnection()?.use { conn ->
                conn.prepareStatement("SELECT balance FROM Accounts WHERE account_id = ?").use { pstmt ->
                    pstmt.setInt(1, accountId)
                    pstmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            balance = rs.getInt("balance") / 100.0
                        }
                    }
                }
            }
        } catch (e: SQLException) {
            java.util.logging.Logger.getLogger(javaClass.name).severe("Bakiye çekilirken SQL hatası (ID: $accountId)")
        } catch (e: Exception) {
            java.util.logging.Logger.getLogger(javaClass.name).severe("Bakiye çekilirken hata (ID: $accountId)")
        }
        return balance
    }
    fun addAccount(account: Account): Boolean {
        return dbManager.executeTransaction { conn ->
            val sql = "INSERT INTO Accounts (account_name, account_type, balance, currency) VALUES (?, ?, ?, ?)"
            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setString(1, account.name)
                pstmt.setString(2, account.type.name)
                pstmt.setInt(3, (account.balance * 100).roundToInt())
                pstmt.setString(4, account.currency)
                pstmt.executeUpdate()
            }
        }
    }

    // Mevcut updateAccount'u kuruş hesabına uygun şekilde güncelleyelim
    fun updateAccount(id: Int, name: String, type: String, balance: Double, currency: String) {
        dbManager.executeTransaction { conn ->
            val sql = "UPDATE Accounts SET account_name = ?, account_type = ?, balance = ?, currency = ? WHERE account_id = ?"
            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setString(1, name)
                pstmt.setString(2, type)
                pstmt.setInt(3, (balance * 100).roundToInt())// Kuruş çevrimi burada da şart!
                pstmt.setString(4, currency)
                pstmt.setInt(5, id)
                pstmt.executeUpdate()
            }
        }
    }

    fun deleteAccount(accountId: Int) {
        try {
            dbManager.deleteRecord("Accounts", "account_id", accountId)
            java.util.logging.Logger.getLogger(javaClass.name).info("Hesap silindi (ID: $accountId)")
        } catch (e: SQLException) {
            java.util.logging.Logger.getLogger(javaClass.name).severe("Hesap silinirken SQL hatası")
        } catch (e: Exception) {
            java.util.logging.Logger.getLogger(javaClass.name).severe("Hesap silinirken hata")
        }
    }
}

