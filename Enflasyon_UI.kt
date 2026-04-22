package com.example.sql_arac

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EnflasyonCardUI(
    onEnflasyonUpdate: (Double, Int) -> Unit // (Değer, Mod) -> 0:Aylık, 1:Yıllık Bileşik, 2:Yıllık Eşit
) {
    var useInflation by remember { mutableStateOf(false) }
    var monthlyStr by remember { mutableStateOf("") }
    var yearlyStr by remember { mutableStateOf("") }

    // 0: Aylık, 1: Yıllık Bileşik, 2: Yıllık Eşit
    var selectedYearlyMode by remember { mutableStateOf(1) }

    // Herhangi bir değer değiştiğinde ana tabloya haberi uçur
    LaunchedEffect(useInflation, monthlyStr, yearlyStr, selectedYearlyMode) {
        if (!useInflation) {
            onEnflasyonUpdate(0.0, 0)
        } else {
            val mVal = monthlyStr.toDoubleOrNull() ?: 0.0
            val yVal = yearlyStr.toDoubleOrNull() ?: 0.0

            if (mVal > 0) {
                onEnflasyonUpdate(mVal, 0) // Aylık Mod
            } else if (yVal > 0) {
                onEnflasyonUpdate(yVal, selectedYearlyMode) // Seçilen Yıllık Mod
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // --- BAŞLIK VE SWITCH ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "📉 Enflasyon İle Hesapla",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Checkbox(
                checked = useInflation,
                onCheckedChange = { useInflation = it },
                colors = CheckboxDefaults.colors(checkedColor = SuccessGreen, uncheckedColor = Color.White)
            )
        }

        // --- GİRİŞ ALANLARI (Açılır Panel) ---
        AnimatedVisibility(
            visible = useInflation,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                // AYLIK GİRİŞ
                OutlinedTextField(
                    value = monthlyStr,
                    onValueChange = {
                        if (it.isEmpty() || it.toDoubleOrNull() != null) {
                            monthlyStr = it
                            if (it.isNotEmpty()) yearlyStr = "" // Yıllığı temizle/kilitle
                        }
                    },
                    label = { Text("Ortalama Aylık Enf. (%)", color = Color.White.copy(0.6f), fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = yearlyStr.isEmpty(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Text(
                    text = "Veya Yıllık Tahmin Girin",
                    modifier = Modifier.padding(vertical = 8.dp).align(Alignment.CenterHorizontally),
                    fontSize = 10.sp,
                    color = Color.White.copy(0.4f)
                )

                // YILLIK GİRİŞ
                OutlinedTextField(
                    value = yearlyStr,
                    onValueChange = {
                        if (it.isEmpty() || it.toDoubleOrNull() != null) {
                            yearlyStr = it
                            if (it.isNotEmpty()) monthlyStr = "" // Aylığı temizle/kilitle
                        }
                    },
                    label = { Text("Ortalama Yıllık Enf. (%)", color = Color.White.copy(0.6f), fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = monthlyStr.isEmpty(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                // YILLIK HESAPLAMA MODU SEÇİCİ (Sadece Yıllık Doluysa Görünür)
                if (yearlyStr.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Yıllık Veriyi Nasıl Dağıtalım?", color = Color.White.copy(0.7f), fontSize = 11.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = selectedYearlyMode == 1,
                            onClick = { selectedYearlyMode = 1 },
                            label = { Text("Bileşik (Kümülatif)", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(labelColor = Color.White, selectedLabelColor = Color.Black, selectedContainerColor = Color.White)
                        )
                        FilterChip(
                            selected = selectedYearlyMode == 2,
                            onClick = { selectedYearlyMode = 2 },
                            label = { Text("Eşit Dağılım (Aritmetik)", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(labelColor = Color.White, selectedLabelColor = Color.Black, selectedContainerColor = Color.White)
                        )
                    }
                }
            }
        }
    }
}

