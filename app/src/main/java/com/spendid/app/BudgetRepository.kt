package com.spendid.app

import android.content.ContentValues
import java.util.*

class BudgetRepository(private val dbHelper: DatabaseHelper) {

    fun saveBudget(amount: Double, month: Int, year: Int, minGoal: Double? = null, maxGoal: Double? = null): Long {
        val db = dbHelper.writableDatabase

        // First, check if budget exists for this month/year
        val existingBudget = getBudget(month, year)

        val values = ContentValues().apply {
            put(DatabaseHelper.COL_BUDGET_AMOUNT, amount)
            put(DatabaseHelper.COL_BUDGET_MONTH, month)
            put(DatabaseHelper.COL_BUDGET_YEAR, year)
            if (minGoal != null) put(DatabaseHelper.COL_BUDGET_MIN_GOAL, minGoal)
            if (maxGoal != null) put(DatabaseHelper.COL_BUDGET_MAX_GOAL, maxGoal)
        }

        return if (existingBudget != null) {
            // Update existing budget
            db.update(
                DatabaseHelper.TABLE_BUDGET,
                values,
                "${DatabaseHelper.COL_BUDGET_MONTH} = ? AND ${DatabaseHelper.COL_BUDGET_YEAR} = ?",
                arrayOf(month.toString(), year.toString())
            ).toLong()
        } else {
            // Insert new budget
            db.insert(DatabaseHelper.TABLE_BUDGET, null, values)
        }
    }

    fun getBudget(month: Int, year: Int): Budget? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_BUDGET} WHERE ${DatabaseHelper.COL_BUDGET_MONTH} = ? AND ${DatabaseHelper.COL_BUDGET_YEAR} = ?",
            arrayOf(month.toString(), year.toString())
        )

        return cursor.use {
            if (it.moveToFirst()) {
                Budget(
                    id = it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.COL_BUDGET_ID)),
                    amount = it.getDouble(it.getColumnIndexOrThrow(DatabaseHelper.COL_BUDGET_AMOUNT)),
                    month = it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.COL_BUDGET_MONTH)),
                    year = it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.COL_BUDGET_YEAR)),
                    minGoal = if (it.getColumnIndex(DatabaseHelper.COL_BUDGET_MIN_GOAL) >= 0)
                        it.getDouble(it.getColumnIndexOrThrow(DatabaseHelper.COL_BUDGET_MIN_GOAL)) else null,
                    maxGoal = if (it.getColumnIndex(DatabaseHelper.COL_BUDGET_MAX_GOAL) >= 0)
                        it.getDouble(it.getColumnIndexOrThrow(DatabaseHelper.COL_BUDGET_MAX_GOAL)) else null
                )
            } else null
        }
    }

    fun getCurrentBudget(): Budget? {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        return getBudget(currentMonth, currentYear)
    }

    fun getAllBudgets(): List<Budget> {
        val budgets = mutableListOf<Budget>()
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_BUDGET} ORDER BY ${DatabaseHelper.COL_BUDGET_YEAR} DESC, ${DatabaseHelper.COL_BUDGET_MONTH} DESC",
            null
        )

        cursor.use {
            while (it.moveToNext()) {
                val budget = Budget(
                    id = it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.COL_BUDGET_ID)),
                    amount = it.getDouble(it.getColumnIndexOrThrow(DatabaseHelper.COL_BUDGET_AMOUNT)),
                    month = it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.COL_BUDGET_MONTH)),
                    year = it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.COL_BUDGET_YEAR)),
                    minGoal = if (it.getColumnIndex(DatabaseHelper.COL_BUDGET_MIN_GOAL) >= 0)
                        it.getDouble(it.getColumnIndexOrThrow(DatabaseHelper.COL_BUDGET_MIN_GOAL)) else null,
                    maxGoal = if (it.getColumnIndex(DatabaseHelper.COL_BUDGET_MAX_GOAL) >= 0)
                        it.getDouble(it.getColumnIndexOrThrow(DatabaseHelper.COL_BUDGET_MAX_GOAL)) else null
                )
                budgets.add(budget)
            }
        }

        return budgets
    }

    fun deleteBudget(month: Int, year: Int): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            DatabaseHelper.TABLE_BUDGET,
            "${DatabaseHelper.COL_BUDGET_MONTH} = ? AND ${DatabaseHelper.COL_BUDGET_YEAR} = ?",
            arrayOf(month.toString(), year.toString())
        )
    }
}