package com.example.sql_arac

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FullForecastScreen(dbManager: DatabaseManager, accountRepo: AccountRepository) {
    var simulationInputs by remember { mutableStateOf(listOf<SimulationInput>()) }
    var currentInfValue by remember { mutableStateOf(0.0) }
    var currentInfMode by remember { mutableStateOf(0) }

    var amountStr by remember { mutableStateOf("") }
    var monthsStr by remember { mutableStateOf("1") }
    var offsetStr by remember { mutableStateOf("1") }
    var isIncomeType by remember { mutableStateOf(true) }

    val forecastRows = remember(simulationInputs, currentInfValue, currentInfMode) {
        ForecastFuture.calculate12MonthProjection(
            dbManager = dbManager,
            accountRepo = accountRepo,
            externalInputs = simulationInputs,
            inflationValue = currentInfValue,
            inflationMode = currentInfMode
        )
    }

    // ANA ZEMİN: Slate 900
    Row(modifier = Modifier.fillMaxSize().background(AppBackground)) {

        // --- 🛠️ SOL PANEL: KONTROL MERKEZİ (%35) ---
        Column(
            modifier = Modifier
                .weight(0.35f)
                .fillMaxHeight()
                .padding(12.dp)
                .background(SurfaceColor, shape = RoundedCornerShape(16.dp)) // Slate 800
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("🔮 Senaryo Planla", color = IceWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            // Gelir/Gider Seçimi
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { isIncomeType = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isIncomeType) PositiveGreen else DeepSlate,
                        contentColor = if (isIncomeType) AppBackground else TextSecondary
                    )
                ) { Text("Gelir", fontSize = 12.sp, fontWeight = FontWeight.Bold) }

                Button(
                    onClick = { isIncomeType = false },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isIncomeType) ActionRed else DeepSlate,
                        contentColor = if (!isIncomeType) IceWhite else TextSecondary
                    )
                ) { Text("Gider", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ✍️ Giriş Alanları
            OutlinedTextField(
                value = amountStr,
                onValueChange = { amountStr = it },
                label = { Text("Miktar (TL)", color = TextSecondary, fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(color = IceWhite),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentColor,
                    unfocusedBorderColor = MutedSlate,
                    cursorColor = AccentColor
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = monthsStr,
                    onValueChange = { monthsStr = it },
                    label = { Text("Süre/Taksit", color = TextSecondary, fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = IceWhite),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentColor, unfocusedBorderColor = MutedSlate)
                )
                OutlinedTextField(
                    value = offsetStr,
                    onValueChange = { offsetStr = it },
                    label = { Text("Kaç Ay Sonra?", color = TextSecondary, fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = IceWhite),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentColor, unfocusedBorderColor = MutedSlate)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        simulationInputs = simulationInputs + SimulationInput(
                            amount = amount,
                            startMonthOffset = offsetStr.toIntOrNull() ?: 1,
                            totalMonths = monthsStr.toIntOrNull() ?: 1,
                            isIncome = isIncomeType,
                            isInstallment = !isIncomeType && (monthsStr.toIntOrNull() ?: 1) > 1
                        )
                        amountStr = ""
                    }
                },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentColor, contentColor = IceWhite)
            ) {
                Text("Ekle ve Hesapla", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = DeepSlate)
            Spacer(modifier = Modifier.height(12.dp))

            // Not: EnflasyonCardUI kendi içinde renklerini güncellemeli
            EnflasyonCardUI(onEnflasyonUpdate = { value, mode ->
                currentInfValue = value
                currentInfMode = mode
            })

            Spacer(modifier = Modifier.height(24.dp))

            TextButton(
                onClick = { simulationInputs = emptyList() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("🗑️ Listeyi Temizle", color = SoftRed, fontSize = 12.sp)
            }
        }

        // --- 📈 SAĞ PANEL: DETAYLI PROJEKSİYON TABLOSU (%65) ---
        Column(modifier = Modifier.weight(0.65f).padding(20.dp)) {
            Text(
                text = "📈 12 Aylık Finansal Projeksiyon",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = IceWhite
            )

            Spacer(modifier = Modifier.height(16.dp))

            // BAŞLIK SATIRI
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepSlate, shape = RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("AY / YIL", Modifier.weight(1.2f), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = IceWhite)
                Text("SABİT GELİR", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 10.sp, color = TextSecondary)
                Text("TAH. GİDER", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 10.sp, color = TextSecondary)
                Text("EK PLAN", Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 10.sp, color = TextSecondary)
                Text("FİNAL KASA", Modifier.weight(1.2f), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = AccentColor)
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(forecastRows) { row ->
                    val netExtra = row.extraIncome - row.extraExpense
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(row.monthName, Modifier.weight(1.2f), fontSize = 13.sp, color = TextPrimary)
                            Text(String.format("%,.0f₺", row.plannedIncome), Modifier.weight(1f), fontSize = 12.sp, color = TextSecondary)
                            Text(String.format("%,.0f₺", row.predictedExpense), Modifier.weight(1f), fontSize = 12.sp, color = SoftRed)
                            Text(
                                text = if(netExtra == 0.0) "-" else String.format("%+,.0f₺", netExtra),
                                modifier = Modifier.weight(1f),
                                color = if (netExtra > 0) PositiveGreen else if (netExtra < 0) ActionRed else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = String.format("%,.0f ₺", row.closingBalance),
                                modifier = Modifier.weight(1.2f),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = if (row.closingBalance < 0) AlarmRed else IceWhite
                            )
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = DeepSlate.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

