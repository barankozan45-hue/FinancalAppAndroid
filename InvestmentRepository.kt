package com.example.sql_arac

import kotlin.math.roundToInt

/**
 * Yatırım işlemlerini yöneten depo sınıfı.
 * Investments tablosuna veri yazar ve Accounts tablosundaki bakiyeyi günceller.
 */
class InvestmentRepository(private val dbManager: DatabaseManager) {

    /**
     * Yeni bir yatırım ekler.
     * İşlem (Transaction) kapsamında hem yatırım kaydedilir hem de hesap bakiyesi düşülür.
     */
    // Repository tarafında metodun İMZASI şu şekilde olmalı:
    fun addInvestment(inv: Investment, targetAccountId: Int): Boolean {
        return dbManager.executeTransaction { conn ->
            // Parasal işlemlerde kuruş hassasiyeti için 100 ile çarpıyoruz (Örn: 150.50 TL -> 15050 Kuruş)
            val costInCents = (inv.totalCost * 100).roundToInt()

            // 1. ADIM: Yatırım Kaydını Oluştur
            // Sütun isimleri veritabanı şemanla %100 uyumludur.
            val insertSql = """
                INSERT INTO Investments (asset_name, asset_type, quantity, total_cost, account_id, target_date)
                VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent()

            conn.prepareStatement(insertSql).use { pstmt ->
                pstmt.setString(1, inv.assetName)
                pstmt.setString(2, inv.assetType)
                pstmt.setDouble(3, inv.quantity) // Miktar (Gram/Adet) REAL olduğu için Double kalır
                pstmt.setInt(4, costInCents)    // Maliyet kuruş (INTEGER) olarak saklanır
                pstmt.setInt(5, inv.accountId)
                pstmt.setString(6, inv.targetDate)
                pstmt.executeUpdate()
            }

            // 2. ADIM: Bakiyeyi Güncelle
            // Yatırım alımı bir nakit çıkışıdır, bu yüzden bakiyeyi azaltıyoruz.
            val updateSql = "UPDATE Accounts SET balance = balance - ? WHERE account_id = ?"

            conn.prepareStatement(updateSql).use { pstmt ->
                pstmt.setInt(1, costInCents)
                pstmt.setInt(2, inv.accountId)
                pstmt.executeUpdate()
            }
        }
    }

    /**
     * Tüm yatırımları listeler.
     */
    fun getAllInvestments(): List<Investment> {
        val list = mutableListOf<Investment>()
        dbManager.getConnection()?.use { conn ->
            val sql = "SELECT * FROM Investments"
            val rs = conn.createStatement().executeQuery(sql)
            while (rs.next()) {
                list.add(Investment(
                    id = rs.getInt("investment_id"),
                    assetName = rs.getString("asset_name"),
                    assetType = rs.getString("asset_type"),
                    quantity = rs.getDouble("quantity"),
                    // Veritabanındaki kuruşu tekrar TL'ye çevirerek modele yüklüyoruz
                    totalCost = rs.getInt("total_cost") / 100.0,
                    accountId = rs.getInt("account_id"),
                    targetDate = rs.getString("target_date")
                ))
            }
        }
        return list
    }
}

