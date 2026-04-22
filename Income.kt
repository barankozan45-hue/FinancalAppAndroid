package com.example.sql_arac

data class Income(
    val id: Int = 0,
    val amount: Double,
    val accountId: Int,
    val category: String,
    val date: String,
    val recurringId: Int? = null
)

