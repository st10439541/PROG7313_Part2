package com.spendid.app

data class UserEntity(
    val id: Int = 0,
    val username: String,
    val passwordHash: String,
    val salt: String = "",
    val tutorialCompleted: Boolean = false,
    val financialGoal: String = "",
    val spendingHabit: String = "",
    val budgetAlerts: Boolean = true,
    val dailyReminder: Boolean = true,
    val badgeNotifications: Boolean = false,
    val darkMode: Boolean = false
)