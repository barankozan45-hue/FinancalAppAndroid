package com.example.sql_arac

import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.math.roundToInt

object StatisticsManager {

    /**
     * SQL Injection'ı önlemek için sorguyu ve parametreleri ayırıyoruz.
     */
    private fun buildFilteredQuery(baseSql: String, startDate: String?, endDate: String?): String {
        return if (!startDate.isNullOrEmpty() && !endDate.isNullOrEmpty()) {
            "$baseSql WHERE date BETWEEN ? AND ?"
        } else baseSql
    }

    /**
     * Parametreleri PreparedStatement'a güvenli bir şekilde bağlar.
     */
    private fun applyParams(pstmt: PreparedStatement, startDate: String?, endDate: String?) {
        if (!startDate.isNullOrEmpty() && !endDate.isNullOrEmpty()) {
            pstmt.setString(1, startDate)
            pstmt.setString(2, endDate)
        }
    }

    /**
     * DUPLICATE CODE ÇÖZÜMÜ:
     * Veritabanı bağlantısını ve ResultSet işleme mantığını tek bir yerde topluyoruz.
     */
    private fun <T> executeQuerySecurely(
        dbManager: DatabaseManager,
        baseSql: String,
        startDate: String?,
        endDate: String?,
        mapper: (ResultSet) -> T
    ): T? {
        val finalSql = buildFilteredQuery(baseSql, startDate, endDate)
        return dbManager.getConnection()?.use { conn ->
            conn.prepareStatement(finalSql).use { pstmt ->
                applyParams(pstmt, startDate, endDate)
                val rs = pstmt.executeQuery()
                mapper(rs)
            }
        }
    }

    fun getTotalExpenses(
        dbManager: DatabaseManager,
        startDate: String? = null,
        endDate: String? = null
    ): Double {
        // baseSql parametresini kaldırdık, doğrudan içeriye yazdık
        val sql = if (!startDate.isNullOrEmpty() && !endDate.isNullOrEmpty()) {
            "SELECT SUM(amount) FROM Expenses WHERE date BETWEEN ? AND ?"
        } else {
            "SELECT SUM(amount) FROM Expenses"
        }

        return dbManager.getConnection()?.use { conn ->
            conn.prepareStatement(sql).use { pstmt ->
                // Eğer tarihler varsa parametreleri güvenle bağla
                if (!startDate.isNullOrEmpty() && !endDate.isNullOrEmpty()) {
                    pstmt.setString(1, startDate)
                    pstmt.setString(2, endDate)
                }

                val rs = pstmt.executeQuery()
                if (rs.next()) rs.getDouble(1) / 100.0 else 0.0
            }
        } ?: 0.0
    }

    fun getExpensesByCategory(
        dbManager: DatabaseManager,
        startDate: String? = null,
        endDate: String? = null
    ): Map<String, Double> {
        val baseSql = "SELECT category, SUM(amount) as cat_total FROM Expenses"
        val sqlWithGroup = buildFilteredQuery(baseSql, startDate, endDate) + " GROUP BY category ORDER BY cat_total DESC"

        val categoryMap = mutableMapOf<String, Double>()

        dbManager.getConnection()?.use { conn ->
            conn.prepareStatement(sqlWithGroup).use { pstmt ->
                applyParams(pstmt, startDate, endDate)
                val rs = pstmt.executeQuery()
                while (rs.next()) {
                    val category = rs.getString("category") ?: "Diğer"
                    categoryMap[category] = rs.getDouble("cat_total") / 100.0
                }
            }
        }
        return categoryMap
    }

    fun getCategoryPercentages(
        dbManager: DatabaseManager,
        startDate: String? = null,
        endDate: String? = null
    ): Map<String, Double> {
        val categoryAmounts = getExpensesByCategory(dbManager, startDate, endDate)
        val total = categoryAmounts.values.sum()

        if (total <= 0.0) return emptyMap()

        return categoryAmounts.mapValues { (_, amount) ->
            // KOTLIN STYLE: Math.round yerine idiomatic yaklaşım
            ((amount / total) * 10000).roundToInt() / 100.0
        }
    }
}