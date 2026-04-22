package com.example.sql_arac

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MultiActionButton(
    onAddAccount: () -> Unit,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onAddInvestment: () -> Unit,
    onAddGoal: () -> Unit // 🎯 1. Parametre buraya eklendi
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(bottom = 12.dp, end = 12.dp)
    ) {
        // --- ANIMASYONLU AÇILIR MENÜ ---
        AnimatedVisibility(
            visible = isMenuExpanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ✨ YENİ: HEDEF EKLE (Turkuaz Yıldız)
                MenuLabelItem(
                    label = "Yeni Hedef / Hayal",
                    icon = Icons.Default.Star,
                    color = CyanAccent, // ✨ Senin paletindeki Turkuaz
                    onClick = {
                        isMenuExpanded = false
                        onAddGoal()
                    }
                )

                // 1. HESAP EKLE
                MenuLabelItem(
                    label = "Hesap Oluştur",
                    icon = Icons.Default.AccountBalanceWallet,
                    color = AccentColor,
                    onClick = {
                        isMenuExpanded = false
                        onAddAccount()
                    }
                )

                // 2. VARLIK / DÖVİZ AL
                MenuLabelItem(
                    label = "Varlık / Döviz Al",
                    icon = Icons.Default.CurrencyExchange,
                    color = Color(0xFF06B6D4),
                    onClick = {
                        isMenuExpanded = false
                        onAddInvestment()
                    }
                )

                // 3. GELİR EKLE
                MenuLabelItem(
                    label = "Gelir Ekle",
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    color = PositiveGreen,
                    onClick = {
                        isMenuExpanded = false
                        onAddIncome()
                    }
                )

                // 4. GİDER EKLE
                MenuLabelItem(
                    label = "Harcama Gir",
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    color = ActionRed,
                    onClick = {
                        isMenuExpanded = false
                        onAddExpense()
                    }
                )
            }
        }

        // --- ANA TETİKLEYİCİ BUTON (FAB) ---
        FloatingActionButton(
            onClick = { isMenuExpanded = !isMenuExpanded },
            containerColor = if (isMenuExpanded) SurfaceColor else AccentColor,
            contentColor = IceWhite,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(12.dp)
        ) {
            Icon(
                imageVector = if (isMenuExpanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = "Menü",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * 🎨 MENÜ ELEMANI (ETİKET + KÜÇÜK BUTON)
 */
@Composable
fun MenuLabelItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        // --- ETİKET (LABEL) ---
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = DeepSlate, // 🌑 Slate 700
            tonalElevation = 4.dp,
            modifier = Modifier.padding(end = 12.dp)
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = IceWhite
            )
        }

        // --- KÜÇÜK YUVARLAK BUTON ---
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = color,
            contentColor = IceWhite,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(6.dp)
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp))
        }
    }
}

