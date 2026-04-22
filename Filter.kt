package com.example.sql_arac

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.sql.Connection
import java.sql.SQLException
import java.time.LocalDate

/**
 * 🎯 MERKEZİ VERİ TAŞIYICI
 */
data class TransactionData(
    val id: Int,
    val amount: Double,
    val categoryOrAsset: String,
    val date: String,      // 📅 Sıralama ve detay için lazım
    val accountName: String?, // 🏦 Expense_UI'daki hatayı çözer
    val isRecurring: Boolean, // 🔄 isRecurring hatasını çözer
    val type: String
)

/**
 * 🛠️ JDBC VERİ MOTORU
 */
class FilterManager(private val connection: Connection) {

    companion object {
        private val ALLOWED_TABLES = setOf("Expenses", "Incomes", "Investments")
    }

    fun getFilteredTransactions(
        tableName: String,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        limit: Int? = null
    ): List<TransactionData> {
        if (tableName !in ALLOWED_TABLES) return emptyList()

        val transactions = mutableListOf<TransactionData>()
        val isInvestment = tableName == "Investments"

        val baseQuery = """
            SELECT 
                t.${if (tableName == "Expenses") "expense_id" else if (tableName == "Incomes") "income_id" else "investment_id"},
                t.${if (tableName == "Investments") "total_cost" else "amount"},
                t.${if (tableName == "Investments") "asset_name" else "category"},
                t.date,
                a.account_name,
                t.recurring_id
            FROM $tableName t
            LEFT JOIN Accounts a ON t.account_id = a.account_id
        """

        val whereClause = StringBuilder()
        val params = mutableListOf<Any?>()

        if (startDate != null && endDate != null) {
            whereClause.append("WHERE t.date BETWEEN ? AND ?")
            params.add(startDate.toString())
            params.add(endDate.toString())
        }

        val orderLimit = if (limit != null) " ORDER BY t.date DESC LIMIT $limit" else " ORDER BY t.date DESC"

        val finalQuery = baseQuery + whereClause + orderLimit

        try {
            connection.prepareStatement(finalQuery).use { pstmt ->
                params.forEachIndexed { idx, param ->
                    when (param) {
                        is String -> pstmt.setString(idx + 1, param)
                        is Int -> pstmt.setInt(idx + 1, param)
                    }
                }

                pstmt.executeQuery().use { rs ->
                    while (rs.next()) {
                        transactions.add(
                            TransactionData(
                                id = rs.getInt(1),
                                amount = rs.getDouble(2) / 100.0,
                                categoryOrAsset = rs.getString(3) ?: "İsimsiz",
                                date = rs.getString("date") ?: "",
                                accountName = rs.getString("account_name"),
                                isRecurring = !isInvestment && rs.getObject("recurring_id") != null,
                                type = tableName
                            )
                        )
                    }
                }
            }
        } catch (e: SQLException) { java.util.logging.Logger.getLogger(javaClass.name).severe("SQL hatası") }
        catch (e: Exception) { java.util.logging.Logger.getLogger(javaClass.name).severe("Veritabanı hatası") }
        return transactions
    }
}

/**
 * 🎨 KÜÇÜK ÖZET TABLO (Dashboard Kartı)
 */
@Composable
fun SmallSummaryTable(items: List<TransactionData>, selectedTable: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (items.isEmpty()) {
            Text(
                text = "Henüz işlem kaydı yok.",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        items.forEach { item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. KATEGORİ VEYA VARLIK İSMİ
                Text(
                    text = item.categoryOrAsset,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White, // Slate tema beyazı
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )

                // 2. MİKTAR (Tutar)
                // Tablo türüne göre renk seçimi
                val amountColor = when (selectedTable) {
                    "Incomes" -> Color(0xFF10B981)    // Zümrüt Yeşil
                    "Expenses" -> Color(0xFFEF4444)   // Parlak Kırmızı
                    "Investments" -> Color(0xFF22D3EE) // Turkuaz (Cyan)
                    else -> Color.White
                }

                Text(
                    text = "${String.format("%,.0f", item.amount)} ₺",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = amountColor,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

