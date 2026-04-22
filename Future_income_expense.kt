package com.example.sql_arac

import javax.swing.JOptionPane

object FutureIncomeExpense {

    fun collectSimulationInputs(): List<SimulationInput> {
        val inputList = mutableListOf<SimulationInput>()
        var continueInput = true

        // 1. Bilgi Mesajı
        JOptionPane.showMessageDialog(null, "🔮 Gelecek Veri Girişi Başlatılıyor...")

        while (continueInput) {
            // --- GELİR ---
            val incomeStr = JOptionPane.showInputDialog("💰 Ek GELİR Tutarı (Boş = 0):")
            val incomeAmount = parseDouble(incomeStr)

            if (incomeAmount > 0) {
                val monthsStr = JOptionPane.showInputDialog("📅 Kaç ay sürecek?")
                val months = parseInt(monthsStr)

                val offsetStr = JOptionPane.showInputDialog("🕒 Kaç ay sonra başlasın? (1: Bu ay)")
                val offset = parseInt(offsetStr)

                inputList.add(SimulationInput(
                    amount = incomeAmount,
                    startMonthOffset = offset,
                    totalMonths = months,
                    isIncome = true,
                    isInstallment = false
                ))
            }

            // --- GİDER ---
            val expenseStr = JOptionPane.showInputDialog("💸 Ek GİDER Tutarı (Boş = 0):")
            val expenseAmount = parseDouble(expenseStr)

            if (expenseAmount > 0) {
                val taksitStr = JOptionPane.showInputDialog("💳 Taksit sayısı?")
                val taksit = parseInt(taksitStr)

                val offsetStr = JOptionPane.showInputDialog("🕒 Kaç ay sonra başlasın? (1: Bu ay)")
                val offset = parseInt(offsetStr)

                inputList.add(SimulationInput(
                    amount = expenseAmount,
                    startMonthOffset = offset,
                    totalMonths = taksit,
                    isIncome = false,
                    isInstallment = taksit > 1
                ))
            }

            // --- DEVAM MI? ---
            val choice = JOptionPane.showConfirmDialog(
                null,
                "Başka bir kayıt eklemek ister misiniz?",
                "Devam?",
                JOptionPane.YES_NO_OPTION
            )
            if (choice != JOptionPane.YES_OPTION) continueInput = false
        }
        return inputList
    }

    private fun parseDouble(s: String?): Double = s?.trim()?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
    private fun parseInt(s: String?): Int = s?.trim()?.toIntOrNull()?.let { if (it < 1) 1 else it } ?: 1
}

