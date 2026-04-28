package com.spendid.app

data class Budget(
    val id: Int = 0,
    val amount: Double,
    val month: Int,
    val year: Int,
    val minGoal: Double? = null,
    val maxGoal: Double? = null
)