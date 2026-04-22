package com.example.sql_arac

/**
 * Harcama verilerini taşıyan model.
 * @param id Veritabanındaki 'expense_id'ye karşılık gelir.
 * @param amount Kullanıcıdan alınan TL cinsi miktar (Örn: 15.50)
 * @param note Veritabanındaki 'description' sütununa kaydedilir.
 */
data class Expense(
    val id: Int = 0,
    val accountId: Int,
    val amount: Double,
    val category: String,
    val date: String,
    val note: String,
    val recurringId: Int? = null // Bu satırı eklemeyi unutma!
)

