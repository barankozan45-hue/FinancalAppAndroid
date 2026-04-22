package com.example.sql_arac

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

@Composable
fun DailyRangeSelector(
    onRangeSelected: (LocalDate, LocalDate) -> Unit
) {
    var startDate by remember { mutableStateOf(LocalDate.now().minusDays(7)) }
    var endDate by remember { mutableStateOf(LocalDate.now()) }

    Row(
        modifier = Modifier.wrapContentWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DateDropdownRow(
            startDate = startDate,
            endDate = endDate,
            onStartDateChange = { newStart ->
                startDate = newStart
                if (endDate.isBefore(startDate)) endDate = startDate
                onRangeSelected(startDate, endDate)
            },
            onEndDateChange = { newEnd ->
                if (newEnd.isAfter(startDate) || newEnd.isEqual(startDate)) {
                    endDate = newEnd
                    onRangeSelected(startDate, endDate)
                }
            }
        )
    }
}

@Composable
fun DateDropdownRow(
    startDate: LocalDate,
    endDate: LocalDate,
    onStartDateChange: (LocalDate) -> Unit,
    onEndDateChange: (LocalDate) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MiniDateGroup(date = startDate, onDateChange = onStartDateChange)

        Text(
            text = " - ",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary, // 🩶 Slate 300
            modifier = Modifier.padding(horizontal = 2.dp)
        )

        MiniDateGroup(date = endDate, onDateChange = onEndDateChange)
    }
}

@Composable
fun MiniDateGroup(date: LocalDate, onDateChange: (LocalDate) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // GÜN
        DateUnitPicker(
            value = date.dayOfMonth,
            range = 1..date.lengthOfMonth(),
            width = 24.dp // Bir tık genişlettik
        ) { onDateChange(date.withDayOfMonth(it)) }

        Text("/", fontSize = 10.sp, color = MutedSlate, modifier = Modifier.padding(horizontal = 1.dp))

        // AY
        DateUnitPicker(
            value = date.monthValue,
            range = 1..12,
            width = 24.dp
        ) { onDateChange(date.withMonth(it)) }

        Text("/", fontSize = 10.sp, color = MutedSlate, modifier = Modifier.padding(horizontal = 1.dp))

        // YIL
        DateUnitPicker(
            value = date.year % 100,
            range = 24..26,
            width = 28.dp
        ) { onDateChange(date.withYear(2000 + it)) }
    }
}

@Composable
fun DateUnitPicker(
    value: Int,
    range: IntRange,
    width: androidx.compose.ui.unit.Dp,
    onValueChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Box(
            modifier = Modifier
                .width(width)
                // 🌑 ARKA PLANI BİR TIK AÇTIK: DeepSlate yerine SurfaceColor veya özel bir ton
                .background(SurfaceColor.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                .border(1.dp, MutedSlate.copy(0.4f), RoundedCornerShape(4.dp)) // Kenarlık daha belirgin
                .clickable { expanded = true }
                .padding(vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value.toString().padStart(2, '0'),
                fontSize = 11.sp, // 10'dan 11'e çektik
                fontWeight = FontWeight.ExtraBold, // Daha kalın
                color = IceWhite // 🧊 Mavi yerine BEYAZ (Kontrast için en iyisi)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .heightIn(max = 240.dp)
                .background(SurfaceColor) // Slate 800
                .border(1.dp, MutedSlate.copy(0.3f), RoundedCornerShape(8.dp))
        ) {
            range.forEach { number ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = number.toString().padStart(2, '0'),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = IceWhite
                        )
                    },
                    onClick = {
                        onValueChange(number)
                        expanded = false
                    }
                )
            }
        }
    }
}

