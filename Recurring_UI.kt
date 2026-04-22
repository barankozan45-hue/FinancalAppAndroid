package com.example.sql_arac

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RecurringSelectionFields(
    isRecurring: Boolean,
    onRecurringChange: (Boolean) -> Unit,
    dayOfMonth: String,
    onDayChange: (String) -> Unit,
    totalMonths: String,
    onTotalMonthsChange: (String) -> Unit
) {
    // Yeni sistemdeki Slate & Indigo stilini yansıtan metin stili
    val labelStyle = TextStyle(
        color = TextSecondary, // 🩶 Slate 400
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp
    )

    Surface(
        color = DeepSlate.copy(alpha = 0.05f), // 🌑 Çok hafif bir derinlik katmanı
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isRecurring,
                    onCheckedChange = onRecurringChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = AccentColor, // 🔵 Indigo Vurgu
                        uncheckedColor = MutedSlate, // 🧱 Kenarlık rengi
                        checkmarkColor = IceWhite   // 🧊 Tik işareti
                    )
                )
                Text(
                    text = "Her ay otomatik tekrarla",
                    fontWeight = FontWeight.Bold,
                    color = AppBackground, // 🌑 Slate 900 (Net okunuş)
                    fontSize = 14.sp
                )
            }

            if (isRecurring) {
                Row(
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp, top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // AYIN GÜNÜ SEÇİMİ
                    OutlinedTextField(
                        value = dayOfMonth,
                        onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) onDayChange(it) },
                        label = { Text("Gün (1-31)", style = labelStyle) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(color = AppBackground, fontWeight = FontWeight.Bold),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = AccentColor,
                            unfocusedBorderColor = MutedSlate,
                            focusedLabelColor = AccentColor,
                            unfocusedLabelColor = TextSecondary
                        )
                    )

                    // TOPLAM AY SEÇİMİ
                    OutlinedTextField(
                        value = totalMonths,
                        onValueChange = { if (it.length <= 3 && it.all { c -> c.isDigit() }) onTotalMonthsChange(it) },
                        label = { Text("Süre (Ay)", style = labelStyle) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(color = AppBackground, fontWeight = FontWeight.Bold),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = AccentColor,
                            unfocusedBorderColor = MutedSlate,
                            focusedLabelColor = AccentColor,
                            unfocusedLabelColor = TextSecondary
                        )
                    )
                }
            }
        }
    }
}

