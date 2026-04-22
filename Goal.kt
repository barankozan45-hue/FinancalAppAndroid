package com.example.sql_arac

import kotlin.math.roundToInt

/**
 * 🎯 GÜNCEL HEDEF MODELİ: Yatırım hesapları ve Otomatik Transferlerle tam uyumlu.
 */
data class FinancialGoal(
    val id: Int = 0,
    val name: String,
    val targetAmount: Double,    // UI'da TL
    val linkedAccountId: Int,    // 🔗 Bağlı Yatırım Hesabı ID
    val autoTransferAmount: Double = 0.0,
    val transferDay: Int = 1,
    val totalMonths: Int = 12
) {
    // 🎯 İLERLEME HESAPLA: Hesap bakiyesini alır, yüzde döner (0.0 - 1.0)
    fun getProgress(currentBalance: Double): Float {
        return if (targetAmount > 0) (currentBalance / targetAmount).coerceIn(0.0, 1.0).toFloat() else 0f
    }

    // 📉 KALAN TUTAR: Hedefe ne kadar kaldı?
    fun getRemaining(currentBalance: Double): Double {
        val diff = targetAmount - currentBalance
        return if (diff > 0) diff else 0.0
    }

    // DB Yardımcıları
    fun targetInCents() = (targetAmount * 100).roundToInt()
}

