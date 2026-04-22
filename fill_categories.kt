package com.example.sql_arac

fun fillCategories(dbManager: DatabaseManager) {
    val expenseCategories = listOf(
        Triple("Market", "🛒", "EXPENSE"),
        Triple("Online Yemek", "🛵", "EXPENSE"),
        Triple("Caffe / Restorant", "☕", "EXPENSE"),
        Triple("Ulaşım", "🚌", "EXPENSE"),
        Triple("Abonelikler / Aidatlar", "💳", "EXPENSE"),
        Triple("Hobi / Oyun", "🎮", "EXPENSE"),
        Triple("Faturalar", "⚡", "EXPENSE"),
        Triple("Kira", "🏠", "EXPENSE"),
        Triple("Giyim", "👕", "EXPENSE"),
        Triple("Eğitim", "📚", "EXPENSE"),
        Triple("Sağlık", "🏥", "EXPENSE"),
        Triple("Spor", "🏋️", "EXPENSE"),
        Triple("Kozmetik", "💄", "EXPENSE"),
        Triple("Sosyal Etkinlik", "🎭", "EXPENSE"),
        Triple("Kitap / Dergi", "📖", "EXPENSE"),
        Triple("Araç", "🚗", "EXPENSE"),
        Triple("Borç / Taksit", "💸", "EXPENSE"),
        Triple("Elektronik", "💻", "EXPENSE"),
        Triple("Beyaz Eşya", "🧊", "EXPENSE"),
        Triple("Mobilya", "🛋️", "EXPENSE"),
        Triple("Diğer", "📦", "EXPENSE")
    )

    val incomeCategories = listOf(
        Triple("Maaş", "🏦", "INCOME"),
        Triple("Satış Geliri", "🏷️", "INCOME"),
        Triple("Gayrimenkul / Kira", "🏠", "INCOME"),
        Triple("İkramiye / Prim", "🎁", "INCOME"),
        Triple("Finans / Faiz", "📈", "INCOME"),
        Triple("Destek", "🤝", "INCOME")
    )

    dbManager.executeTransaction { conn ->
        // INSERT OR IGNORE: skips silently if name already exists (UNIQUE constraint)
        // No DELETE, no sequence reset — existing foreign key references are never touched
        val sql = "INSERT OR IGNORE INTO Categories (name, icon, type) VALUES (?, ?, ?)"
        conn.prepareStatement(sql).use { pstmt ->
            (expenseCategories + incomeCategories).forEach { (name, icon, type) ->
                pstmt.setString(1, name)
                pstmt.setString(2, icon)
                pstmt.setString(3, type)
                pstmt.addBatch()
            }
            pstmt.executeBatch()
        }
    }
    println("✅ Kategoriler başarıyla yüklendi (mevcut kayıtlar korundu).")
}