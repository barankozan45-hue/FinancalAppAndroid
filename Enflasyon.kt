package com.example.sql_arac

import kotlin.math.pow

object Enflasyon {

    /**
     * @param value: Kullanıcının girdiği oran (Örn: 60.0)
     * @param mode: 0 -> Aylık Sabit, 1 -> Yıllık Bileşik, 2 -> Yıllık Eşit Dağılım
     */
    fun getAylikCarpan(value: Double, mode: Int): Double {
        if (value <= 0.0) return 1.0

        return when (mode) {
            0 -> {
                // DOĞRUDAN AYLIK: e = 1 + (x / 100)
                1.0 + (value / 100.0)
            }
            1 -> {
                // YILLIK BİLEŞİK (Kümülatif): e = (1 + x/100)^(1/12)
                (1.0 + (value / 100.0)).pow(1.0 / 12.0)
            }
            2 -> {
                // 🎯 YILLIK EŞİT DAĞILIM (Aritmetik): (x / 12)
                // Örn: Yıllık %120 ise her ay %10 ekler.
                val aylikOran = value / 12.0
                1.0 + (aylikOran / 100.0)
            }
            else -> 1.0
        }
    }

    /**
     * Döngü içinde her ayın yeni giderini hesaplar.
     * Gider_yeni = Gider_eski * e
     */
    fun sonrakiAyGideri(mevcutGider: Double, aylikCarpan: Double): Double {
        return mevcutGider * aylikCarpan
    }
}

