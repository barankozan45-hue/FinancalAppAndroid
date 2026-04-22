package com.example.sql_arac

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 🎯 GELECEK EKRANI (Merkezi Navigasyon Uyumlu)
 * Üst taraf hedefleri (1 birim), alt taraf projeksiyonu (2 birim) gösterir.
 */
@Composable
fun ForecastPage(
    goals: List<FinancialGoal>,
    accountRepo: AccountRepository,
    projectionResult: ProjectionResult,
    onOpenSimulation: () -> Unit,
    onGoalInvestment: (FinancialGoal) -> Unit,
    onGoalIncome: (FinancialGoal) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground) // Merkezi arka plan rengi
    ) {
        // --- 1. BÖLÜM: ÜST GRID (Toplam yüksekliğin 1/3'ü) ---
        // Bu alan hedefler arttıkça kendi içinde kaydırılabilir (scrollable) kalır.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.3f)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 340.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Başlık Satırı
                item(span = { GridItemSpan(maxLineSpan) }) {
                    SectionHeader(text = "AKTİF HEDEFLERİN")
                }

                // Hedef Kartları
                items(goals) { goal ->
                    val currentBalance = accountRepo.getAccountBalance(goal.linkedAccountId)
                    FinancialGoalCard(
                        goal = goal,
                        currentBalance = currentBalance,
                        onActionInvestment = { onGoalInvestment(it) },
                        onActionIncome = { onGoalIncome(it) }
                    )
                }
            }
        }

        // --- 2. BÖLÜM: ALT PANEL (Toplam yüksekliğin 2/3'ü) ---
        // Bu alan sabittir ve projeksiyon analizini gösterir.
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f),
            color = AppBackground,
            tonalElevation = 8.dp, // Hafif derinlik hissi
            shadowElevation = 10.dp // Üst kısımdan ayıran gölge
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Estetik Ayırıcı
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = DeepSlate.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Başlık (Panelin en üstünde çivili)
                SectionHeader(text = "GELECEK ÖNGÖRÜSÜ")

                // 🎯 ORANSAL YERLEŞİM BOŞLUĞU:
                // DownBlockUI'ı başlığın çok altına değil, biraz daha yukarıya çekmek için weight kullanıyoruz.
                Spacer(modifier = Modifier.weight(0.2f))

                // Projeksiyon Kartı ve Butonu barındıran merkezi blok
                DownBlockUI(
                    projectionResult = projectionResult,
                    onOpenForecast = onOpenSimulation
                )

                // Kartın çok aşağıda boşluk bırakmaması için alt denge ağırlığı
                Spacer(modifier = Modifier.weight(0.8f))
            }
        }
    }
}

