package com.spendid.app

data class Expense(
    val id: Int,
    val amount: Double,
    val description: String,
    val categoryId: Int,  // Changed from category: String to categoryId: Int
    val date: String,
    val time: String,
    val imageUri: String?
)