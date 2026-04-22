package com.example.sql_arac

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 🎨 FINANCIAL APP - ULTIMATE COLOR PALETTE (2026)
 * Koyu tema öncelikli, finansal hiyerarşi ve göz konforu odaklı tasarım sistemi.
 */

// --- 🌑 ZEMİN VE YÜZEY KATMANLARI (HIYERARŞİ) ---
val AppBackground = Color(0xFF153580)     // En dip zemin (Slate 900)
val SurfaceColor = Color(0xFF1E293B)       // Kartlar ve paneller (Slate 800)
val DeepSlate = Color(0xFF334155)          // Kenarlıklar ve etkileşim (Slate 700)
val MutedSlate = Color(0xFF475569)         // Pasif alanlar ve devre dışı butonlar (Slate 600)
val CharcoalBlueCard = Color(0xFF1E272E)   // Alternatif koyu kart rengi

// --- ⚪ IŞIK VE METİN SİSTEMİ (SLATE SCALING) ---
val IceWhite = Color(0xFFF1F5F9)           // En parlak rakamlar ve vurgular (Slate 100)
val TextPrimary = Color(0xFFF8FAFC)        // Ana başlıklar ve metinler (Slate 50)
val TextSecondary = Color(0xFF94A3B8)      // Alt başlıklar ve birimler (Slate 400)
val SlateSilver = Color(0xFF64748B)        // Grafik rehberleri ve ikonlar (Slate 500)
val SteelBlue = Color(0xFFACBEDC)          // Tarihler ve pasif notlar (Gümüş Mavi)

// --- 🔵 VURGU, AKSİYON VE MODERN TONLAR ---
val AccentColor = Color(0xFF6366F1)        // Ana İndigo Vurgu (Butonlar)
val CyanAccent = Color(0xFF00ACC1)         // Teknoloji, Transfer ve Modern Harcamalar
val SkySoftBlue = Color(0xFF81B8EE)        // Grafik çizgileri ve yumuşak ikonlar
val LightSkyBlue = Color(0xFFBBDEFB)       // Buz Mavisi (Seçili arka planlar)

// --- 🟢 YEŞİL SPEKTRUM (GELİR & GÜVEN) ---
val PositiveGreen = Color(0xFF26DE81)       // Canlı Yeşil (Günlük akış)
val SuccessGreen = Color(0xFF4CAF50)        // Onay ve Başarı butonları
val SettlementGreen = Color(0xFF2E7D32)     // Toplam Bakiye ve Net Kazanç (Koyu Yeşil)
val DeepForestGreen = Color(0xFF1B5E20)     // Yatırım ve Ciddi Birikimler

// --- 🔴 KIRMIZI SPEKTRUM (GİDER & UYARI) ---
val NegativeRed = Color(0xFFEB3B5A)         // Gider Kategorileri (Canlı)
val SoftRed = Color(0xFFE57373)             // Pastel Giderler (Grafik Dilimleri)
val ErrorRed = Color(0xFFF44336)            // Standart Harcama Rakamları
val ActionRed = Color(0xFFE53935)           // İptal Butonları ve Aktif Uyarılar
val AlarmRed = Color(0xFFB91C1C)            // Kritik Limit Aşımı (Kan Kırmızısı)
val DeepMaroon = Color(0xFF7F1D1D)          // En Koyu Bordo (Silme ve İflas Eşiği)

// --- 🌸 ZEMİN VURGULARI (SOFT BACKGROUNDS) ---
val TrendCardBackground = Color(0xFFFEF2F2) // Çok açık gül kurusu (Red 50)
val ExpenseCardBackground = Color(0xFFFEE2E2)// Yumuşak kırmızı zemin (Red 100)

// --- 🍊 ARA TONLAR ---
val WarningAmber = Color(0xFFEE8B26)        // Yakıt, Ulaşım ve Bekleyen İşlemler

// --- 🏢 ESKİ UI UYUMLULUĞU VE STİLLER ---
val labelStyle = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)

/**
 * 🚀 DİNAMİK KATEGORİ RENK YÖNETİMİ
 * Uygulamanın her yerinde tutarlı bir görsel dil sağlar.
 */
fun getCategoryColor(category: String): Color {
    return when (category.trim().lowercase()) {
        "market", "gıda", "mutfak"   -> SettlementGreen // Güven veren temel ihtiyaç
        "yakıt", "ulaşım", "araç"     -> WarningAmber    // Enerjik ve dikkat çekici
        "eğlence", "sosyal", "gezi"   -> AccentColor      // Canlı ve modern
        "fatura", "kira", "aidat"     -> SteelBlue        // Ciddi ve stabil
        "sağlık", "eczane", "hastane" -> SoftRed          // Pastel ve sakinleştirici
        "giyim", "alışveriş"          -> CyanAccent       // Ferah ve teknolojik
        "maaş", "gelir", "ek iş"      -> PositiveGreen    // Pozitif kazanç
        "diğer", "nakit"              -> SlateSilver      // Nötr ve geri planda
        else -> {
            // Bilinmeyen kategoriler için isimden renk üret ama soft tut
            val hash = category.hashCode()
            val colorLong = 0xFF000000L or (hash.toLong() and 0x00FFFFFFL)
            Color(colorLong).copy(alpha = 0.7f)
        }
    }
}

