package com.example.sql_arac

/**
 * Recurring_Transactions tablosuyla tam uyumlu model.
 * Yatırım (Investment) desteği için targetAccountId eklendi.
 */
data class RecurringTransaction(
    val id: Int = 0,               // DB: recurring_id
    val type: String,              // DB: type ("INCOME", "EXPENSE", "INVESTMENT")
    val amount: Double,            // DB: amount (Kuruş bazlı saklanır)
    val accountId: Int,            // DB: account_id (Para Çıkan / Gelen Ana Hesap)

    // YENİ: Yatırım için hedef hesap (Gelir/Gider'de NULL olur, sorun yaratmaz)
    val targetAccountId: Int? = null,

    val category: String,          // DB: category
    val dayOfMonth: Int,           // DB: day_of_month
    val totalMonths: Int,          // DB: total_months
    val remainingMonths: Int,      // DB: remaining_months
    val isActive: Boolean = true   // DB: is_active
)

