package com.spendid.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "spendid.db"
        const val DATABASE_VERSION = 5   // Incremented to rebuild tables

        // ================= USERS =================
        const val TABLE_USERS = "users"
        const val COL_USER_ID = "id"
        const val COL_USERNAME = "username"
        const val COL_PASSWORD_HASH = "passwordHash"
        const val COL_SALT = "salt"
        const val COL_TUTORIAL_COMPLETED = "tutorialCompleted"

        // ================= EXPENSES =================
        const val TABLE_EXPENSES = "expenses"
        const val COL_EXPENSE_ID = "id"
        const val COL_TITLE = "title"               // from second helper
        const val COL_AMOUNT = "amount"
        const val COL_CATEGORY = "category"
        const val COL_DESCRIPTION = "description"
        const val COL_DATE = "date"
        const val COL_START_TIME = "startTime"
        const val COL_END_TIME = "endTime"
        const val COL_IMAGE_PATH = "imagePath"      // from second helper (was imageUri in first)
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Users table
        db.execSQL(
            """
            CREATE TABLE $TABLE_USERS (
                $COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USERNAME TEXT NOT NULL UNIQUE,
                $COL_PASSWORD_HASH TEXT NOT NULL,
                $COL_SALT TEXT NOT NULL DEFAULT '',
                $COL_TUTORIAL_COMPLETED INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        // Expenses table (merged schema: includes title, all time fields, image path)
        db.execSQL(
            """
            CREATE TABLE $TABLE_EXPENSES (
                $COL_EXPENSE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TITLE TEXT NOT NULL,
                $COL_AMOUNT REAL NOT NULL,
                $COL_CATEGORY TEXT NOT NULL,
                $COL_DESCRIPTION TEXT NOT NULL,
                $COL_DATE TEXT NOT NULL,
                $COL_START_TIME TEXT NOT NULL,
                $COL_END_TIME TEXT NOT NULL,
                $COL_IMAGE_PATH TEXT
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // For development: drop and recreate both tables
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSES")
        onCreate(db)
    }

    // ================= EXPENSE CRUD =================
    fun insertExpense(expense: Expense): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TITLE, expense.title)
            put(COL_AMOUNT, expense.amount)
            put(COL_CATEGORY, expense.category)
            put(COL_DESCRIPTION, expense.description)
            put(COL_DATE, expense.date)
            put(COL_START_TIME, expense.startTime)
            put(COL_END_TIME, expense.endTime)
            put(COL_IMAGE_PATH, expense.imagePath)
        }
        val result = db.insert(TABLE_EXPENSES, null, values)
        db.close()
        return result
    }

    fun getAllExpenses(): List<Expense> {
        val expenseList = mutableListOf<Expense>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_EXPENSES"
        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_EXPENSE_ID))
            val title = cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE))
            val amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_AMOUNT))
            val category = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY))
            val description = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION))
            val date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE))
            val startTime = cursor.getString(cursor.getColumnIndexOrThrow(COL_START_TIME))
            val endTime = cursor.getString(cursor.getColumnIndexOrThrow(COL_END_TIME))
            val imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE_PATH))

            val expense = Expense(id, title, amount, category, description, date, startTime, endTime, imagePath)
            expenseList.add(expense)
        }
        cursor.close()
        db.close()
        return expenseList
    }

    fun deleteExpense(expenseId: Int) {
        val db = writableDatabase
        val whereClause = "$COL_EXPENSE_ID = ?"
        val whereArgs = arrayOf(expenseId.toString())
        db.delete(TABLE_EXPENSES, whereClause, whereArgs)
        db.close()
    }
}