package com.example.sql_arac

import androidx.compose.runtime.*
import java.sql.Connection
import kotlin.math.roundToInt

@Composable
fun Income_Buton(
    accounts: List<Account>,
    dbManager: DatabaseManager,
    editingItem: TransactionData? = null, // 👈 Regulation (Düzenleme) için eklenen parametre
    onTransactionComplete: () -> Unit
) {
    // 1. Gelir Kategorilerini Veritabanından Çek
    val incomeCategories by produceState(initialValue = emptyList()) {
        dbManager.getConnection()?.use { conn ->
            val sql = "SELECT name, icon FROM Categories WHERE type = 'INCOME'"
            val rs = conn.createStatement().executeQuery(sql)
            val categoryList = mutableListOf<Pair<String, String>>()
            while (rs.next()) {
                categoryList.add(rs.getString("name") to (rs.getString("icon") ?: "💰"))
            }
            value = categoryList
        }
    }

    // 2. Diyaloğu Çağır
    AddIncomeDialog(
        accounts = accounts,
        categoryData = incomeCategories,
        // Düzenleme modundaysak, miktarı ekrana basarken 100'e bölüyoruz (Kuruş -> Lira)
        editingItem = editingItem?.copy(amount = editingItem.amount / 100.0),
        onDismiss = { onTransactionComplete() },
        onConfirm = { account, amount, category, _, isRec, day, total ->

            // Miktarı veritabanı için kuruşa (cent) çevir
            val amountInCents = (amount * 100).roundToInt()

            dbManager.executeTransaction { conn ->
                if (editingItem == null) {
                    // --- SENARYO A: YENİ GELİR KAYDI ---
                    handleNewIncome(conn, isRec, amountInCents, account, category, day, total)
                } else {
                    // --- SENARYO B: MEVCUT GELİRİ DÜZENLEME (REGULATION) ---
                    handleUpdateIncome(conn, editingItem, amountInCents, account, category)
                }
            }
            // İşlem bittiğinde App.kt'ye haber ver ve listeleri tazele
            onTransactionComplete()
        }
    )
}

/**
 * YENİ GELİR EKLEME FONKSİYONU
 */
private fun handleNewIncome(conn: Connection, isRec: Boolean, amountInCents: Int, account: Account, category: String, day: Int, total: Int) {
    if (isRec) {
        val recSql = """
            INSERT INTO Recurring_Transactions 
            (type, amount, account_id, category, day_of_month, total_months, remaining_months, is_active) 
            VALUES ('INCOME', ?, ?, ?, ?, ?, ?, 1)
        """.trimIndent()
        conn.prepareStatement(recSql).apply {
            setInt(1, amountInCents); setInt(2, account.id); setString(3, category)
            setInt(4, day); setInt(5, total); setInt(6, total)
            executeUpdate()
        }
    } else {
        val insSql = "INSERT INTO Incomes (amount, account_id, category) VALUES (?, ?, ?)"
        conn.prepareStatement(insSql).apply {
            setInt(1, amountInCents); setInt(2, account.id); setString(3, category)
            executeUpdate()
        }
        // Bakiyeyi Artır
        val updSql = "UPDATE Accounts SET balance = balance + ? WHERE account_id = ?"
        conn.prepareStatement(updSql).apply {
            setInt(1, amountInCents); setInt(2, account.id)
            executeUpdate()
        }
    }
}

/**
 * GELİR DÜZENLEME (REGULATION) FONKSİYONU
 * Mantık: Gelir eklenince bakiye artmıştı. Düzenlerken önce eski geliri
 * bakiyeden DÜŞERİZ (-), sonra yeni geliri EKLERİZ (+).
 */
private fun handleUpdateIncome(conn: Connection, oldItem: TransactionData, newAmountCents: Int, newAccount: Account, newCategory: String) {
    // 1. ADIM: Eski geliri hesaptan geri çıkar (Sanki hiç kazanılmamış gibi)
    val undoSql = """
        UPDATE Accounts SET balance = balance - (SELECT amount FROM Incomes WHERE income_id = ?) 
        WHERE account_id = (SELECT account_id FROM Incomes WHERE income_id = ?)
    """.trimIndent()
    conn.prepareStatement(undoSql).apply {
        setInt(1, oldItem.id)
        setInt(2, oldItem.id)
        executeUpdate()
    }

    // 2. ADIM: Gelir tablosundaki veriyi güncelle
    val updateSql = "UPDATE Incomes SET amount = ?, account_id = ?, category = ? WHERE income_id = ?"
    conn.prepareStatement(updateSql).apply {
        setInt(1, newAmountCents)
        setInt(2, newAccount.id)
        setString(3, newCategory)
        setInt(4, oldItem.id)
        executeUpdate()
    }

    // 3. ADIM: Yeni (Güncel) tutarı hesaba ekle
    val redoSql = "UPDATE Accounts SET balance = balance + ? WHERE account_id = ?"
    conn.prepareStatement(redoSql).apply {
        setInt(1, newAmountCents)
        setInt(2, newAccount.id)
        executeUpdate()
    }
}

