package com.example.sql_arac

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * 🗓️ Ay Bazlı Tarih Hesaplayıcı
 */
object Month_Calculator { // <--- Bu 'object' bloğunu ekledik

    /**
     * Seçilen aya göre başlangıç ve bitiş tarihlerini belirler.
     * format: "YYYY-MM-DD"
     */
    fun getMonthDateRange(selectedMonth: YearMonth): Pair<String, String> {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val today = LocalDate.now()
        val currentMonth = YearMonth.from(today)

        // 1. Başlangıç Tarihi her zaman seçilen ayın 1. günüdür (ör: 2026-03-01)
        val startDate = selectedMonth.atDay(1).format(formatter)

        // 2. Bitiş Tarihi Kontrolü
        val endDate = if (selectedMonth == currentMonth) {
            // Eğer içinde bulunduğumuz aysa: BUGÜNÜ al (ör: 2026-03-21)
            today.plusDays(1).format(formatter)
        } else {
            // Geçmiş bir aysa: O ayın SON GÜNÜNÜ al (ör: 2026-02-28)
            selectedMonth.atEndOfMonth().format(formatter)
        }

        // Pair(Başlangıç, Bitiş) olarak döndürür
        return Pair(startDate, endDate)
    }
}

