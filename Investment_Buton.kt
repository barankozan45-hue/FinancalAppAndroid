package com.example.sql_arac

import androidx.compose.runtime.*
import kotlin.math.roundToInt

@Composable
fun Investment_Buton(
    accounts: List<Account>,
    dbManager: DatabaseManager,
    investmentRepo: InvestmentRepository,
    onTransactionComplete: () -> Unit
) {
    AddInvestmentDialog(
        accounts = accounts,
        onDismiss = { onTransactionComplete() },
        onConfirm = { investment, targetAccountId, isRecurring, day, totalMonths ->

            // 1. ADIM: Anlık Yatırımı Gerçekleştir
            // (Bankadan para düşer, Altın/Borsa hesabına ekleme yapar)
            val success = investmentRepo.addInvestment(investment, targetAccountId)

            if (success && isRecurring) {
                // 2. ADIM: Otomatik Talimatı Kaydet
                dbManager.executeTransaction { conn ->
                    // DİKKAT: target_account_id sütununu ve 9. soru işaretini ekledik!
                    val recSql = """
                        INSERT INTO Recurring_Transactions 
                        (type, amount, account_id, target_account_id, category, day_of_month, total_months, remaining_months, is_active) 
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1)
                    """.trimIndent()

                    conn.prepareStatement(recSql).use { pstmt ->
                        pstmt.setString(1, "INVESTMENT")
                        // Maliyet (Örn: 5000 TL)
                        pstmt.setInt(2, (investment.totalCost * 100).roundToInt())
                        // Kaynak Hesap (Örn: Maaş Bankası)
                        pstmt.setInt(3, investment.accountId)
                        // HEDEF HESAP (Örn: Altın Hesabı) - Burası hayati önemde!
                        pstmt.setInt(4, targetAccountId)

                        pstmt.setString(5, "${investment.assetName} Birikimi")
                        pstmt.setInt(6, day)
                        pstmt.setInt(7, totalMonths)
                        // İlkini bugün yaptık, kalandan 1 düştük
                        pstmt.setInt(8, totalMonths - 1)

                        pstmt.executeUpdate()
                    }
                }
            }

            onTransactionComplete()
        }
    )
}

