package com.spendid.app

data class Expense(
    val id: Int,
    val title: String,
    val amount: Double,
    val category: String,
    val description: String,
    val date: String,        // format: YYYY-MM-DD
    val startTime: String,   // format: HH:mm
    val endTime: String,
    val imagePath: String    // URI string or empty
)