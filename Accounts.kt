package com.example.sql_arac


/**
 * Veritabanındaki 'Accounts' tablosunu temsil eden ana model.
 */
data class Account(
    val id: Int,
    val name: String,
    val type: AccountType,
    var balance: Double, // Biz yine Double kullanalım, Repository bunu kuruşa çevirsin
    val currency: String = "TL"
)

/**
 * Hesap türlerini hem teknik isimle (DB için)
 * hem de Türkçe etiketle (UI için) tutuyoruz.
 */
enum class AccountType(val displayName: String) {
    CASH("Nakit"),
    BANK_ACCOUNT("Banka Hesabı"),
    CREDIT_CARD("Kredi Kartı"),
    INVESTMENT("Yatırım Hesabı")
}

