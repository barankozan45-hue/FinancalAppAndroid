package com.example.sql_arac

class CategoryRepository(private val dbManager: DatabaseManager) {
    fun getAllCategories(): List<String> {
        val categories = mutableListOf<String>()
        dbManager.getConnection()?.use { conn ->
            val rs = conn.createStatement().executeQuery("SELECT name FROM Categories WHERE type = 'EXPENSE'")
            while (rs.next()) {
                categories.add(rs.getString("name"))
            }
        }
        return categories
    }
}

