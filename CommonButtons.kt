package com.example.sql_arac

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DialogActionButtons(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmText: String = "KAYDET",
    dismissText: String = "Vazgeç",
    isConfirmEnabled: Boolean = true,
    confirmColor: Color = AccentColor // 🔵 Varsayılan: İndigo
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp), // Biraz daha nefes alan boşluk
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- ❌ İPTAL / VAZGEÇ BUTONU (Ghost Button Stili) ---
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.padding(end = 4.dp)
        ) {
            Text(
                text = dismissText,
                color = TextSecondary, // 🧱 Slate 400: Arka planda kalsın
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // --- ✅ ONAY / KAYDET BUTONU (Aksiyon Butonu) ---
        Button(
            onClick = onConfirm,
            enabled = isConfirmEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = confirmColor, // 🔵 İndigo veya 🔴 Kırmızı (Harcama için)
                contentColor = IceWhite,      // 🧊 Net Beyaz
                disabledContainerColor = DeepSlate.copy(alpha = 0.6f), // 🌑 Slate 700 (Pasifken)
                disabledContentColor = MutedSlate // 🧱 Slate 500 (Pasif Yazı)
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 4.dp,
                pressedElevation = 1.dp,
                disabledElevation = 0.dp
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
        ) {
            Text(
                text = confirmText.uppercase(), // Kurumsal bir hava için büyük harf
                fontWeight = FontWeight.Black,  // Daha güçlü vurgu
                fontSize = 14.sp,
                letterSpacing = 0.8.sp
            )
        }
    }
}

