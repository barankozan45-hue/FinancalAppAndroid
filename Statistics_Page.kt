package com.example.sql_arac

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

/**
 * 📈 İSTATİSTİK SAYFASI - Dropdown Filtreleme Sürümü
 */
@Composable
fun StatisticPage(
    dbManager: DatabaseManager,
    onNavigate: (String) -> Unit,
    refreshTrigger: Int = 0
) {
    // 🔍 ANALİZ DURUMU (Zaman Makinesi)
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    val dateRange = remember(selectedMonth) { Month_Calculator.getMonthDateRange(selectedMonth) }

    // 📉 VERİLER
    val stats = remember(dateRange, refreshTrigger) {
        StatisticsManager.getCategoryPercentages(dbManager, dateRange.first, dateRange.second)
    }
    val totalExpense = remember(dateRange, refreshTrigger) {
        StatisticsManager.getTotalExpenses(dbManager, dateRange.first, dateRange.second)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // --- ÜST SATIR: BAŞLIK VE YENİ DROPDOWN SEÇİCİ ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "FİNANSAL ANALİZ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = IceWhite,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Harcama alışkanlıklarınızı ve trendleri izleyin",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            // 🔥 Yeni Dropdown Bileşeni
            MonthDropdownSelector(
                selectedMonth = selectedMonth,
                onMonthSelected = { selectedMonth = it }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- ANA ANALİZ PANELİ ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            // 🍕 1. BÖLÜM: GRAFİK KARTI
            Surface(
                modifier = Modifier.weight(1.8f).height(360.dp),
                color = SurfaceColor,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, DeepSlate)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "KATEGORİK DAĞILIM",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentColor,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        if (stats.isNotEmpty()) {
                            DonutChartCanvas(stats)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Toplam", fontSize = 10.sp, color = TextSecondary)
                                Text(
                                    "${String.format("%,.0f", totalExpense)} ₺",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = IceWhite
                                )
                            }
                        } else {
                            Text("Veri Bulunamadı", color = MutedSlate)
                        }
                    }
                }
            }

            // 📋 2. BÖLÜM: DETAYLAR
            Surface(
                modifier = Modifier.weight(1.2f).height(360.dp),
                color = SurfaceColor,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, DeepSlate)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "DETAYLAR",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CategoryLegendList(stats)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- ALT SATIR ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                MonthlyTrendResult(dbManager = dbManager)
            }
            Box(modifier = Modifier.weight(1.5f)) {
                TransactionFilterCard(
                    connection = dbManager.getConnection(),
                    startDate = java.time.LocalDate.parse(dateRange.first),
                    endDate = java.time.LocalDate.parse(dateRange.second),
                    refreshTrigger = refreshTrigger,
                    onSeeAllClick = { onNavigate("FULL_TABLE_$it") }
                )
            }
        }
        Spacer(modifier = Modifier.height(50.dp))
    }
}

/**
 * 🛠️ DROPDOWN AY SEÇİCİ BİLEŞENİ
 */
@Composable
fun MonthDropdownSelector(
    selectedMonth: YearMonth,
    onMonthSelected: (YearMonth) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    // Son 24 ayı listele (İhtiyaca göre artırılabilir)
    val months = remember {
        (0..23).map { YearMonth.now().minusMonths(it.toLong()) }
    }

    Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
        // Tıklanabilir Alan (Görünür Kutu)
        Surface(
            onClick = { expanded = true },
            color = SurfaceColor,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DeepSlate),
            modifier = Modifier.width(180.dp).height(48.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val monthName = selectedMonth.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("tr"))
                Text(
                    text = "${monthName.replaceFirstChar { it.uppercase() }} ${selectedMonth.year}",
                    color = IceWhite,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("▼", color = AccentColor, fontSize = 10.sp)
            }
        }

        // Açılır Menü
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(SurfaceColor)
                .width(180.dp)
                .heightIn(max = 300.dp) // Çok uzun olmasın, scroll olsun
        ) {
            months.forEach { month ->
                val mName = month.month.getDisplayName(TextStyle.FULL, Locale("tr"))
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "${mName.replaceFirstChar { it.uppercase() }} ${month.year}",
                            color = if (month == selectedMonth) AccentColor else IceWhite,
                            fontWeight = if (month == selectedMonth) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onMonthSelected(month)
                        expanded = false
                    }
                )
            }
        }
    }
}

