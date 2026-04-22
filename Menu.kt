package com.example.sql_arac

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SideMenu(
    selectedScreen: String,
    onScreenSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(SurfaceColor)
            .padding(24.dp)
    ) {
        // --- 💎 LOGO ALANI ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onScreenSelected("WELCOME") } // Logoya basınca ana girişe dönsün
        ) {
            Icon(
                imageVector = Icons.Default.Diamond,
                contentDescription = null,
                tint = CyanAccent,
                modifier = Modifier.size(30.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "FINANCE PRO",
                color = IceWhite,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
        Text(text = "MENÜ", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // --- 📂 MENÜ ÖĞELERİ ---

        MenuItem(
            label = "Dashboard",
            icon = Icons.Default.Dashboard,
            // 🎯 HOME veya DASHBOARD fark etmeksizin burası seçili görünür
            isSelected = selectedScreen == "DASHBOARD" || selectedScreen == "WELCOME",
            onClick = { onScreenSelected("DASHBOARD") }
        )

        MenuItem(
            label = "Hesaplarım",
            icon = Icons.Default.AccountBalance,
            isSelected = selectedScreen == "ACCOUNTS",
            onClick = { onScreenSelected("ACCOUNTS") }
        )

        MenuItem(
            label = "İstatistikler",
            icon = Icons.Default.BarChart,
            isSelected = selectedScreen == "STATS",
            onClick = { onScreenSelected("STATS") }
        )

        MenuItem(
            label = "Gelecek",
            icon = Icons.Default.Timeline,
            isSelected = selectedScreen == "FUTURE",
            onClick = { onScreenSelected("FUTURE") } // 🎯 Az önce main.kt'de kurduğumuz rota
        )

        Spacer(modifier = Modifier.weight(1f))

        MenuItem(
            label = "Ayarlar",
            icon = Icons.Default.Settings,
            isSelected = selectedScreen == "SETTINGS",
            onClick = { onScreenSelected("SETTINGS") }
        )
    }
}

@Composable
fun MenuItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Seçiliyse arka plana hafif bir vurgu ekle
    val bg = if (isSelected) AccentColor.copy(alpha = 0.15f) else androidx.compose.ui.graphics.Color.Transparent
    val contentColor = if (isSelected) CyanAccent else TextSecondary

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .height(50.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = bg
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                color = if (isSelected) IceWhite else TextSecondary,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )

            // Sağ taraftaki küçük seçim noktası
            if (isSelected) {
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(CyanAccent, RoundedCornerShape(50))
                )
            }
        }
    }
}

