package com.example.sql_arac

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun StatisticChartScreen(dbManager: DatabaseManager, refreshTrigger: Int = 0) {
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    var monthDepth by remember { mutableStateOf(3) }

    val dateRange = remember(selectedMonth) {
        Month_Calculator.getMonthDateRange(selectedMonth)
    }

    val categoryPercentages = remember(dateRange, refreshTrigger) {
        StatisticsManager.getCategoryPercentages(dbManager, dateRange.first, dateRange.second)
    }

    val totalExpense = remember(dateRange, refreshTrigger) {
        StatisticsManager.getTotalExpenses(dbManager, dateRange.first, dateRange.second)
    }

    val topTrends = remember(selectedMonth, monthDepth, refreshTrigger) {
        val dateStr = selectedMonth.atDay(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        StatisticsCalculator.getTopIncreasingTrends(dbManager, dateStr, monthDepth)
    }

    // Ana Arka Plan: AppBackground (Slate 900)
    Box(modifier = Modifier.fillMaxSize().background(AppBackground)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    "Finansal Analiz",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = IceWhite // 🧊 Net Beyaz
                )
                Spacer(modifier = Modifier.height(16.dp))
                MonthSelectorRow(selectedMonth) { selectedMonth = it }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- PASTA GRAFİĞİ BÖLÜMÜ ---
            item {
                if (categoryPercentages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(SurfaceColor, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Bu dönemde harcama kaydı yok ✨", color = TextSecondary, fontSize = 14.sp)
                    }
                } else {
                    Box(modifier = Modifier.size(260.dp), contentAlignment = Alignment.Center) {
                        DonutChartCanvas(categoryPercentages)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Toplam Gider", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                String.format("%,.0f ₺", totalExpense),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = IceWhite
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    CategoryLegendList(categoryPercentages)
                }
            }

            // --- KRİTİK TRENDLER BÖLÜMÜ ---
            item {
                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MutedSlate.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(20.dp))

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("🚨 Kritik Trendler", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = IceWhite)
                    Spacer(Modifier.weight(1f))
                    // Analiz Butonu
                    Surface(
                        onClick = { monthDepth = if (monthDepth == 3) 2 else 3 },
                        color = AccentColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "$monthDepth Aylık Analiz",
                            fontSize = 11.sp,
                            color = AccentColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (topTrends.isEmpty()) {
                item {
                    Text(
                        "Sürekli artış gösteren harcama yok. ✅",
                        fontSize = 13.sp, color = TextSecondary,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                }
            } else {
                items(topTrends) { trend ->
                    TrendAlertCard(trend)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun TrendAlertCard(trend: TrendAlert) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor), // 🌑 Slate 800
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MutedSlate.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    trend.category.uppercase(),
                    fontWeight = FontWeight.ExtraBold,
                    color = ActionRed, // 🔴 Harcama uyarısı
                    fontSize = 13.sp
                )
                Text(
                    "Sürekli Artış: %${String.format("%.1f", trend.totalIncrease)}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            val barCount = (trend.totalIncrease / 10).toInt().coerceIn(1, 5)
            Text("🔥".repeat(barCount), fontSize = 18.sp)
        }
    }
}

@Composable
fun DonutChartCanvas(data: Map<String, Double>) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2.2f
        var startAngle = -90f

        data.forEach { (category, percentage) ->
            val sweepAngle = (percentage.toFloat() / 100f) * 360f
            val color = getCategoryColor(category) // Bu fonksiyonu projedeki renklerle senkronize etmelisin

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = size.minDimension / 6f)
            )

            if (percentage > 5.0) { // Küçük dilimlerde metin karmaşasını önlemek için 5.0 yaptım
                val middleAngle = (startAngle + sweepAngle / 2) * (Math.PI / 180).toFloat()
                val textX = center.x + cos(middleAngle.toDouble()).toFloat() * radius
                val textY = center.y + sin(middleAngle.toDouble()).toFloat() * radius

                val label = "%${percentage.toInt()}"

                val textLayoutResult = textMeasurer.measure(
                    text = label,
                    style = TextStyle(
                        color = IceWhite.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(
                        x = textX - textLayoutResult.size.width / 2,
                        y = textY - textLayoutResult.size.height / 2
                    )
                )
            }
            startAngle += sweepAngle
        }
    }
}

@Composable
fun CategoryLegendList(data: Map<String, Double>) {
    val sortedList = data.toList().sortedByDescending { it.second }.take(6)
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
        sortedList.forEach { (category, percentage) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 6.dp)
            ) {
                Box(modifier = Modifier.size(10.dp).background(getCategoryColor(category), RoundedCornerShape(3.dp)))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = category, fontSize = 13.sp, modifier = Modifier.weight(1f), color = IceWhite)
                Text(text = "%${percentage.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
            }
        }
    }
}

@Composable
fun MonthSelectorRow(selectedMonth: YearMonth, onMonthSelected: (YearMonth) -> Unit) {
    val months = remember {
        (0..5).map { YearMonth.now().minusMonths(it.toLong()) }.reversed()
    }
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(months) { month ->
            val isSelected = month == selectedMonth
            val monthName = month.format(DateTimeFormatter.ofPattern("MMMM", Locale.forLanguageTag("tr")))

            Surface(
                onClick = { onMonthSelected(month) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) AccentColor else SurfaceColor,
                border = if (!isSelected) BorderStroke(0.5.dp, MutedSlate.copy(alpha = 0.3f)) else null
            ) {
                Text(
                    text = monthName.replaceFirstChar { it.uppercase() },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    color = if (isSelected) IceWhite else TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

