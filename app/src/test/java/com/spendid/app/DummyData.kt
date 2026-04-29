package com.spendid.app

object TestDataFactory {

    // User test data
    fun createTestUser(
        id: Int = 1,
        username: String = "testuser",
        passwordHash: String = "dummy_hash",
        salt: String = "dummy_salt",
        tutorialCompleted: Boolean = false,
        financialGoal: String = "Save more money",
        spendingHabit: String = "Mostly in control",
        budgetAlerts: Boolean = true,
        dailyReminder: Boolean = true,
        badgeNotifications: Boolean = false,
        darkMode: Boolean = false

    ): UserEntity {

        return UserEntity(
            id = id,
            username = username,
            passwordHash = passwordHash,
            salt = salt,
            tutorialCompleted = tutorialCompleted,
            financialGoal = financialGoal,
            spendingHabit = spendingHabit,
            budgetAlerts = budgetAlerts,
            dailyReminder = dailyReminder,
            badgeNotifications = badgeNotifications,
            darkMode = darkMode
        )
    }

    // Expense test data
    fun createTestExpense(
        id: Int = 1,
        amount: Double = 150.50,
        description: String = "Lunch at Café",
        categoryId: Int = 1,
        date: String = "2026-04-15",
        time: String = "13:30 PM",
        imageUri: String? = null

    ): Expense {

        return Expense(
            id = id,
            amount = amount,
            description = description,
            categoryId = categoryId,
            date = date,
            time = time,
            imageUri = imageUri
        )
    }

    fun createExpenseList(): List<Expense> {
        return listOf(
            createTestExpense(1, 150.50, "Lunch at Café", 1, "2026-04-15", "13:30 PM"),
            createTestExpense(2, 45.99, "Uber Ride", 2, "2026-04-14", "08:15 AM"),
            createTestExpense(3, 320.00, "Groceries", 1, "2026-04-13", "18:45 PM"),
            createTestExpense(4, 89.99, "Movie Tickets", 5, "2026-04-12", "20:00 PM"),
            createTestExpense(5, 1200.00, "New Shoes", 4, "2026-04-10", "15:20 PM")
        )
    }

    // Budget test data
    fun createTestBudget(
        id: Int = 1,
        amount: Double = 6000.00,
        month: Int = 3,
        year: Int = 2026,
        minGoal: Double? = 3000.00,
        maxGoal: Double? = 8000.00
    ): Budget {
        return Budget(
            id = id,
            amount = amount,
            month = month,
            year = year,
            minGoal = minGoal,
            maxGoal = maxGoal
        )
    }

    // Category test data
    fun createTestCategories(): List<Category> {
        return listOf(
            Category(1, "Food", "🍔"),
            Category(2, "Transport", "🚗"),
            Category(3, "Health", "💊"),
            Category(4, "Shopping", "🛒"),
            Category(5, "Entertainment", "🎬"),
            Category(6, "Other", "📦")
        )
    }

    fun createTestCategorySpending(): List<ReportsFragment.CategorySpending> {
        return listOf(
            ReportsFragment.CategorySpending("Food", "🍔", 470.49, 47.0),
            ReportsFragment.CategorySpending("Transport", "🚗", 245.99, 24.5),
            ReportsFragment.CategorySpending("Entertainment", "🎬", 189.99, 19.0),
            ReportsFragment.CategorySpending("Shopping", "🛒", 95.50, 9.5)
        )
    }
}