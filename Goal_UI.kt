package com.example.sql_arac

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- 🎨 RENK PALETİ VE STİL ---
val GoalCardBackground = DeepSlate
val GoalProgressBarTrack = MutedSlate.copy(alpha = 0.3f)
val GoalTargetColor = IceWhite
val GoalCurrentColor = CyanAccent

/**
 * 🎯 HEDEF KARTI UI (Ana Versiyon)
 */
@Composable

fun FinancialGoalCard(
    goal: FinancialGoal,
    currentBalance: Double,
    onActionInvestment: (FinancialGoal) -> Unit, // 🎯 Goal nesnesini geri gönderiyoruz
    onActionIncome: (FinancialGoal) -> Unit,
    goalIcon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Star
) {
    val progress = goal.getProgress(currentBalance)
    val remaining = goal.getRemaining(currentBalance)

    Card(
        modifier = Modifier
            .width(280.dp) // 📏 Genişlik sabit, yükseklik içeriğe göre (wrap)
            .padding(vertical = 4.dp, horizontal = 6.dp),
        shape = RoundedCornerShape(20.dp), // Biraz daha keskin ve modern köşeler
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        border = BorderStroke(1.dp, DeepSlate.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .wrapContentHeight() // 🔥 Kartın boyu sadece içeriği kadar olsun
        ) {
            // --- Üst Başlık ve Yüzde ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Icon(goalIcon, null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = goal.name,
                        fontWeight = FontWeight.Bold,
                        color = IceWhite,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "%${(progress * 100).toInt()}",
                    fontWeight = FontWeight.Black,
                    color = CyanAccent,
                    fontSize = 16.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            // --- İlerleme Çubuğu ---
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp) // Biraz daha inceledik
                    .clip(RoundedCornerShape(3.dp)),
                color = CyanAccent,
                trackColor = DeepSlate,
                strokeCap = StrokeCap.Round
            )

            Spacer(Modifier.height(6.dp))

            // --- Kalan Tutarı ---
            Text(
                text = if (remaining > 0) "Kalan: ${String.format("%,.0f", remaining)} ₺" else "Hedef Tamamlandı! 🎉",
                fontSize = 10.sp,
                color = if (remaining > 0) TextSecondary else PositiveGreen,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(12.dp))

            // --- Aksiyon Butonları (İnce Tasarım) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onActionInvestment(goal) },
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp), // 📏 Yüksekliği kıstık
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp), // İçeriği dar alana sığdır
                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f))
                ) {
                    Text("📈 Yatırım", color = CyanAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onActionIncome(goal) },
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp), // 📏 Yüksekliği kıstık
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                ) {
                    Text("💰 Gelir", color = AppBackground, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

/**
 * 💰 HIZLI AKSİYON DİYALOĞU
 * Karttaki butonlara basıldığında bu açılacak.
 */
@Composable
fun QuickActionDialog(
    goal: FinancialGoal,
    linkedAccountName: String,
    isInvestment: Boolean,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var amountStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceColor,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isInvestment) Icons.Default.TrendingUp else Icons.Default.AddCard,
                    contentDescription = null,
                    tint = CyanAccent
                )
                Spacer(Modifier.width(10.dp))
                Text(if (isInvestment) "Yatırım Aktar" else "Gelir Ekle", color = IceWhite)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Hedef: ${goal.name}", color = IceWhite, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Tutar (₺)", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = DeepSlate,
                        focusedTextColor = IceWhite
                    )
                )

                Text(
                    "📍 Para '$linkedAccountName' hesabına işlenecek.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(amountStr.toDoubleOrNull() ?: 0.0) },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
            ) {
                Text("ONAYLA", color = AppBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("İPTAL", color = TextSecondary) }
        }
    )
}

/**
 * 🏷️ Bölüm Başlığı
 */
@Composable
fun SectionHeader(text: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = TextSecondary,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
    }
}

/**
 * ⏳ Yükleme Ekranı
 */
@Composable
fun LoadingPlaceholder() {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(110.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, DeepSlate.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp), color = CyanAccent, strokeWidth = 3.dp)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Veriler Hazırlanıyor...", color = TextSecondary, fontSize = 13.sp)
        }
    }
}

