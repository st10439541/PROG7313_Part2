package com.spendid.app

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {

        const val DATABASE_NAME = "spendid.db"
        const val DATABASE_VERSION = 5  // Updated to version 5

        // ================= USERS =================
        const val TABLE_USERS = "users"
        const val COL_ID = "id"
        const val COL_USERNAME = "username"
        const val COL_PASSWORD_HASH = "passwordHash"
        const val COL_SALT = "salt"
        const val COL_TUTORIAL_COMPLETED = "tutorialCompleted"

        // ================= TUTORIAL OPTIONS =================
        const val COL_FINANCIAL_GOAL = "financialGoal"
        const val COL_SPENDING_HABIT = "spendingHabit"
        const val COL_BUDGET_ALERTS = "budgetAlerts"
        const val COL_DAILY_REMINDER = "dailyReminder"
        const val COL_BADGE_NOTIFICATIONS = "badgeNotifications"
        const val COL_DARK_MODE = "darkMode"

        // ================= EXPENSES =================
        const val TABLE_EXPENSES = "expenses"
        const val COL_EXPENSE_ID = "id"
        const val COL_AMOUNT = "amount"
        const val COL_DESCRIPTION = "description"
        const val COL_CATEGORY = "category"
        const val COL_DATE = "date"
        const val COL_START_TIME = "startTime"
        const val COL_END_TIME = "endTime"
        const val COL_IMAGE_URI = "imageUri"
    }

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL(
            """
            CREATE TABLE $TABLE_USERS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USERNAME TEXT NOT NULL UNIQUE,
                $COL_PASSWORD_HASH TEXT NOT NULL,
                $COL_SALT TEXT NOT NULL DEFAULT '',
                $COL_TUTORIAL_COMPLETED INTEGER NOT NULL DEFAULT 0,
                $COL_FINANCIAL_GOAL TEXT DEFAULT '',
                $COL_SPENDING_HABIT TEXT DEFAULT '',
                $COL_BUDGET_ALERTS INTEGER DEFAULT 1,
                $COL_DAILY_REMINDER INTEGER DEFAULT 1,
                $COL_BADGE_NOTIFICATIONS INTEGER DEFAULT 0,
                $COL_DARK_MODE INTEGER DEFAULT 0
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE $TABLE_EXPENSES (
                $COL_EXPENSE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_AMOUNT REAL NOT NULL,
                $COL_DESCRIPTION TEXT NOT NULL,
                $COL_CATEGORY TEXT NOT NULL,
                $COL_DATE TEXT NOT NULL,
                $COL_START_TIME TEXT NOT NULL,
                $COL_END_TIME TEXT NOT NULL,
                $COL_IMAGE_URI TEXT
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        // Handle incremental upgrades instead of full reset
        if (oldVersion < 5) {
            try {
                // Add new columns to users table if they don't exist
                db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COL_FINANCIAL_GOAL TEXT DEFAULT ''")
                db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COL_SPENDING_HABIT TEXT DEFAULT ''")
                db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COL_BUDGET_ALERTS INTEGER DEFAULT 1")
                db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COL_DAILY_REMINDER INTEGER DEFAULT 1")
                db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COL_BADGE_NOTIFICATIONS INTEGER DEFAULT 0")
                db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COL_DARK_MODE INTEGER DEFAULT 0")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}