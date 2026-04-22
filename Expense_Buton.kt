package com.example.sql_arac

import androidx.compose.runtime.*
import java.sql.Connection
import kotlin.math.roundToInt

/**
 * Bu fonksiyon hem Yeni Kayıt hem de Düzenleme (Edit) işlemini yönetir.
 * editingItem null ise 'EKLEME', dolu ise 'GÜNCELLEME' modunda çalışır.
 */
@Composable
fun Expense_Buton(
    accounts: List<Account>,
    dbManager: DatabaseManager,
    editingItem: TransactionData? = null, // Düzenleme için gelen veri
    onTransactionComplete: () -> Unit
) {
    // 1. Kategorileri Veritabanından Dinamik Olarak Çek
    val expenseCategories by produceState(initialValue = emptyList()) {
        dbManager.getConnection()?.use { conn ->
            val sql = "SELECT name, icon FROM Categories WHERE type = 'EXPENSE'"
            val rs = conn.createStatement().executeQuery(sql)
            val categoryList = mutableListOf<Pair<String, String>>()
            while (rs.next()) {
                categoryList.add(rs.getString("name") to (rs.getString("icon") ?: "📦"))
            }
            value = categoryList
        }
    }

    // 2. Arayüzü (UI) Çağır
    AddExpenseUI(
        accounts = accounts,
        categoryData = expenseCategories,
        editingItem = editingItem, // UI'daki kutucukları doldurmak için gönderiyoruz
        onDismiss = { onTransactionComplete() },
        onConfirm = { account: Account, amount: Double, category: String, desc: String, isRec: Boolean, day: Int, total: Int ->

            // Parayı kuruş (cent) cinsinden işle (Hassasiyet kaybını önler)
            val amountInCents = (amount * 100).roundToInt()

            dbManager.executeTransaction { conn ->
                if (editingItem == null) {
                    // --- DURUM 1: YENİ HARCAMA EKLE ---
                    handleNewExpense(conn, isRec, amountInCents, account, category, day, total)
                } else {
                    // --- DURUM 2: MEVCUT HARCAMAYI GÜNCELLE ---
                    handleUpdateExpense(conn, editingItem, amountInCents, account, category)
                }
            }
            onTransactionComplete()
        }
    )
}

/**
 * YENİ KAYIT MANTIĞI:
 * Veritabanına ekler ve seçili hesabın bakiyesinden düşer.
 */
private fun handleNewExpense(
    conn: Connection,
    isRec: Boolean,
    amountInCents: Int,
    account: Account,
    category: String,
    day: Int,
    total: Int
) {
    if (isRec) {
        // Tekrarlı İşlem Kaydı
        val recSql = """
            INSERT INTO Recurring_Transactions 
            (type, amount, account_id, category, day_of_month, total_months, remaining_months, is_active) 
            VALUES (?, ?, ?, ?, ?, ?, ?, 1)
        """.trimIndent()
        conn.prepareStatement(recSql).apply {
            setString(1, "EXPENSE")
            setInt(2, amountInCents)
            setInt(3, account.id)
            setString(4, category)
            setInt(5, day)
            setInt(6, total)
            setInt(7, total)
            executeUpdate()
        }
    } else {
        // Normal Harcama Kaydı
        val insSql = "INSERT INTO Expenses (amount, account_id, category) VALUES (?, ?, ?)"
        conn.prepareStatement(insSql).apply {
            setInt(1, amountInCents)
            setInt(2, account.id)
            setString(3, category)
            executeUpdate()
        }

        // Bakiye Güncelleme
        val updSql = "UPDATE Accounts SET balance = balance - ? WHERE account_id = ?"
        conn.prepareStatement(updSql).apply {
            setInt(1, amountInCents)
            setInt(2, account.id)
            executeUpdate()
        }
    }
}

/**
 * GÜNCELLEME MANTIĞI:
 * 1. Eski tutarı eski hesaba iade eder (Hatalı bakiye oluşmaması için).
 * 2. Harcamayı günceller.
 * 3. Yeni tutarı yeni hesaptan düşer.
 */
private fun handleUpdateExpense(
    conn: Connection,
    oldItem: TransactionData,
    newAmountCents: Int,
    newAccount: Account,
    newCategory: String
) {
    // ADIM 1: Eski harcama tutarını hesaba GERİ YÜKLE (Refund)
    // Bu sayede bakiye sanki o harcama hiç yapılmamış gibi eski haline döner.
    val refundSql = """
        UPDATE Accounts SET balance = balance + (SELECT amount FROM Expenses WHERE expense_id = ?) 
        WHERE account_id = (SELECT account_id FROM Expenses WHERE expense_id = ?)
    """.trimIndent()
    conn.prepareStatement(refundSql).apply {
        setInt(1, oldItem.id)
        setInt(2, oldItem.id)
        executeUpdate()
    }

    // ADIM 2: Harcama satırını GÜNCELLE
    val updateSql = "UPDATE Expenses SET amount = ?, account_id = ?, category = ? WHERE expense_id = ?"
    conn.prepareStatement(updateSql).apply {
        setInt(1, newAmountCents)
        setInt(2, newAccount.id)
        setString(3, newCategory)
        setInt(4, oldItem.id)
        executeUpdate()
    }

    // ADIM 3: Yeni güncel tutarı hesaptan DÜŞ
    val deductSql = "UPDATE Accounts SET balance = balance - ? WHERE account_id = ?"
    conn.prepareStatement(deductSql).apply {
        setInt(1, newAmountCents)
        setInt(2, newAccount.id)
        executeUpdate()
    }
}

