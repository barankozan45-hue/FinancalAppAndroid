package com.example.sql_arac

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 📊 Gelişmiş Aksiyon Paneli: Oransal Genişlik Versiyonu.
 * Buton her zaman kart genişliğinin %25'i kadar yer kaplar.
 */
@Composable
fun DownBlockUI(
    projectionResult: ProjectionResult,
    onOpenForecast: () -> Unit
) {
    // Ana kapsayıcı Row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically // Elemanları dikeyde ortalar
    ) {

        // --- 🔮 SOL: PROJEKSİYON ÖZET KARTI (%80 Pay) ---
        Box(
            modifier = Modifier
                .weight(0.8f) // 🎯 Toplam genişliğin %80'ini kaplar
                .fillMaxHeight(0.9f) // 🎯 Dikey alanı daha iyi doldurması için artırıldı
                .widthIn(min = 450.dp) // Küçük ekranlarda kartın okunurluğunu korur
        ) {
            ProjectionSummaryCard(result = projectionResult)
        }

        // --- ➖ ARA BOŞLUK (Oransal) ---
        // Boşluğun da oranla büyümesi için weight verdik
        Spacer(modifier = Modifier.weight(0.02f))

        // --- 🚀 SAĞ: GELECEĞİ ÇİZ BUTONU (%20 Pay) ---
        // 🎯 Kartın (0.8) tam olarak %25'i (0.2) genişliğinde olur.
        Button(
            onClick = onOpenForecast,
            modifier = Modifier
                .weight(0.2f) // 🎯 Toplam genişliğin %20'sini kaplar
                .height(56.dp) // Buton yüksekliği hala sabit kalarak estetiği korur
                .widthIn(min = 160.dp), // İçindeki yazıların sığması için emniyet sınırı
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentColor,
                contentColor = IceWhite
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 6.dp,
                pressedElevation = 2.dp
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = CyanAccent
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        "GELECEĞİ ÇİZ",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp,
                        maxLines = 1 // Yazının butondan taşmasını engeller
                    )
                    Text(
                        "Simülasyon Modu",
                        fontWeight = FontWeight.Light,
                        fontSize = 9.sp,
                        color = IceWhite.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

