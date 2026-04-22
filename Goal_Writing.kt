package com.example.sql_arac

import kotlin.math.roundToInt

/**
 * 🎯 HEDEF YAZMA MERKEZİ (Güncellenmiş Model ile Tam Uyumlu)
 */
class Goal_Writing(private val dbManager: DatabaseManager) {

    /**
     * 📥 YENİ HEDEF KAYDI
     */
    fun saveNewGoal(goal: FinancialGoal): Boolean {
        val sql = """
            INSERT INTO Goals (
                name, target_amount, linked_account_id, 
                auto_transfer_amount, transfer_day, total_months, is_active
            ) VALUES (?, ?, ?, ?, ?, ?, 1)
        """.trimIndent()

        return dbManager.executeTransaction { conn ->
            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setString(1, goal.name)
                // ⚠️ BURASI DÜZELDİ: Modeldeki yeni isme uygun (targetInCents)
                pstmt.setInt(2, goal.targetInCents())
                pstmt.setInt(3, goal.linkedAccountId)
                // ⚠️ BURASI DÜZELDİ: autoTransferAmount için de kuruş çevrimi ekledik
                pstmt.setInt(4, (goal.autoTransferAmount * 100).roundToInt())
                pstmt.setInt(5, goal.transferDay)
                pstmt.setInt(6, goal.totalMonths)
                pstmt.executeUpdate() > 0
            }
        }
    }

    /**
     * 🔍 AKTİF HEDEFLERİ LİSTELE
     */
    fun fetchAllActiveGoals(): List<FinancialGoal> {
        val list = mutableListOf<FinancialGoal>()
        dbManager.getConnection()?.use { conn ->
            val sql = "SELECT * FROM Goals WHERE is_active = 1"
            val rs = conn.createStatement().executeQuery(sql)
            while (rs.next()) {
                list.add(FinancialGoal(
                    id = rs.getInt("goal_id"),
                    name = rs.getString("name"),
                    // 💵 DB'den (Int) alıp UI için (Double) yapıyoruz
                    targetAmount = rs.getInt("target_amount") / 100.0,
                    linkedAccountId = rs.getInt("linked_account_id"),
                    autoTransferAmount = rs.getInt("auto_transfer_amount") / 100.0,
                    transferDay = rs.getInt("transfer_day"),
                    totalMonths = rs.getInt("total_months")
                ))
            }
        }
        return list
    }

    /**
     * 📝 HEDEF GÜNCELLEME
     */
    fun updateExistingGoal(goal: FinancialGoal): Boolean {
        val sql = """
            UPDATE Goals SET 
                name = ?, target_amount = ?, linked_account_id = ?,
                auto_transfer_amount = ?, transfer_day = ?, total_months = ?
            WHERE goal_id = ?
        """.trimIndent()

        return dbManager.executeTransaction { conn ->
            conn.prepareStatement(sql).use { pstmt ->
                pstmt.setString(1, goal.name)
                pstmt.setInt(2, goal.targetInCents()) // ✅ Yeni metot adı
                pstmt.setInt(3, goal.linkedAccountId)
                pstmt.setInt(4, (goal.autoTransferAmount * 100).roundToInt())
                pstmt.setInt(5, goal.transferDay)
                pstmt.setInt(6, goal.totalMonths)
                pstmt.setInt(7, goal.id)
                pstmt.executeUpdate() > 0
            }
        }
    }
}

