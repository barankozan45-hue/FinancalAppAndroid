package com.example.sql_arac

/**
 * Investments tablosu ile %100 uyumlu model.
 */
data class Investment(
    val id: Int = 0,               // DB: investment_id
    val assetName: String,         // DB: asset_name
    val assetType: String,         // DB: asset_type
    val quantity: Double,          // DB: quantity (REAL - Adet/Gram olduğu için Double kalmalı)
    val totalCost: Double,         // DB: total_cost (INTEGER - Kuruş bazlı saklanacak)
    val accountId: Int,            // DB: account_id (Hangi hesaptan para çıktı?)
    val targetDate: String?        // DB: target_date (Opsiyonel hedef tarih)
)

