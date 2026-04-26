package com.spendid.app

data class Expense(
    val id: Int,
    val amount: Double,
    val description: String,
    val category: String,
    val date: String,
    val time: String,
    val imageUri: String?
)