package com.example.sql_arac

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppMain(
    dbManager: DatabaseManager,
    accounts: List<Account>,            // ✅ from AppState via main.kt
    goals: List<FinancialGoal>,         // ✅ from AppState via main.kt
    onEnterApp: () -> Unit,
    onAddAccount: () -> Unit,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onAddInvestment: () -> Unit,
    onAddGoal: () -> Unit,
    onMenuClick: () -> Unit
) {
    // --- 📊 VERİ MOTORU ---
    // ✅ accounts and goals come from AppState — no DB calls here
    // Only derived/computed values remain in remember blocks

    val grandTotal = remember(accounts) {
        accounts.sumOf { it.balance }
    }

    // ✅ topGoal derived from already-loaded goals and accounts lists
    // accountRepo.getAccountBalance() removed — balance already in accounts list
    val topGoal = remember(goals, accounts) {
        goals.maxByOrNull { goal ->
            val balance = accounts.find { it.id == goal.linkedAccountId }?.balance ?: 0.0
            (balance / goal.targetAmount).coerceIn(0.0, 1.0)
        }
    }

    // These two still need a DB call — they are not in AppState yet
    val projection = remember { MonthNet.calculateMonthEndProjection(dbManager, AccountRepository(dbManager)) }
    val categoryStats = remember {
        StatisticsManager.getCategoryPercentages(dbManager, "2026-01-01", "2026-12-31")
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(3000), RepeatMode.Reverse),
        label = "pulseAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(SurfaceColor, AppBackground),
                    radius = 2200f
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            AccentColor.copy(alpha = 0.08f * pulseAlpha),
                            Color.Transparent
                        )
                    )
                )
        )

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(1200)) + slideInVertically(initialOffsetY = { 30 })
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 24.dp)
            ) {

                // --- 1. SOL ÜST: MARKA VE MENÜ ---
                Row(
                    modifier = Modifier.align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceColor.copy(alpha = 0.4f))
                            .border(1.dp, DeepSlate.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            "Menü",
                            tint = CyanAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            "FINANCE PRO",
                            color = IceWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            "Secure Terminal v3.2",
                            color = CyanAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // --- 2. MERKEZ: VERİ KARTLARI ---
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "ANLIK PORTFÖY ÖZETİ",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        letterSpacing = 4.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // TOPLAM VARLIK
                        InfoGlassCard(
                            modifier = Modifier.weight(1f),
                            label = "Toplam Varlık",
                            value = String.format("%,.0f ₺", grandTotal),
                            icon = Icons.Default.AccountBalanceWallet,
                            color = CyanAccent
                        )

                        // KRİTİK HEDEF
                        // ✅ balance derived from accounts list — no getAccountBalance() DB call
                        val goalPercent = if (topGoal != null) {
                            val bal = accounts
                                .find { it.id == topGoal.linkedAccountId }
                                ?.balance ?: 0.0
                            (bal / topGoal.targetAmount * 100).toInt()
                        } else 0

                        InfoGlassCard(
                            modifier = Modifier.weight(1f),
                            label = "Kritik Hedef: ${topGoal?.name ?: "---"}",
                            value = "%$goalPercent",
                            subValue = "Tamamlanma Oranı",
                            icon = Icons.Default.Flag,
                            color = Color(0xFFFBC02D)
                        )

                        // HARCAMA DAĞILIMI
                        Box(modifier = Modifier.weight(1f)) {
                            MiniPieChartCard(categoryStats)
                        }

                        // AY SONU BEKLENTİSİ
                        InfoGlassCard(
                            modifier = Modifier.weight(1f),
                            label = "${projection.targetMonthName} Sonu Beklentisi",
                            value = String.format("%,.0f ₺", projection.finalExpectation),
                            subValue = if (projection.futureNet >= 0) "Pozitif Trend" else "Negatif Trend",
                            icon = Icons.Default.Timeline,
                            color = if (projection.futureNet >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    }
                }

                // --- 3. SAĞ ALT: AKSİYON BUTONU ---
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 10.dp)
                ) {
                    MultiActionButton(
                        onAddAccount = onAddAccount,
                        onAddIncome = onAddIncome,
                        onAddExpense = onAddExpense,
                        onAddInvestment = onAddInvestment,
                        onAddGoal = onAddGoal
                    )
                }

                // --- 4. SOL ALT: SİSTEM NOTU ---
                Row(
                    modifier = Modifier.align(Alignment.BottomStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(CyanAccent, CircleShape)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "All systems operational. AES-256 Encrypted.",
                        color = MutedSlate,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

// --- YARDIMCI BİLEŞENLER ---

@Composable
fun InfoGlassCard(
    modifier: Modifier,
    label: String,
    value: String,
    subValue: String? = null,
    icon: ImageVector,
    color: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceColor.copy(alpha = 0.4f))
            .border(1.dp, DeepSlate.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(label, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(value, color = IceWhite, fontSize = 20.sp, fontWeight = FontWeight.Black)
        if (subValue != null) {
            Text(
                subValue,
                color = color.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun MiniPieChartCard(stats: Map<String, Double>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceColor.copy(alpha = 0.4f))
            .border(1.dp, DeepSlate.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Harcama Dağılımı",
            color = TextSecondary,
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.size(65.dp)) {
            if (stats.isNotEmpty()) {
                DonutChartCanvas(stats)
            } else {
                Icon(
                    Icons.Default.PieChart,
                    null,
                    tint = DeepSlate,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}