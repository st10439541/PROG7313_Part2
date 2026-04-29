package com.spendid.app

import android.database.sqlite.SQLiteDatabase
import java.text.SimpleDateFormat
import java.util.*

class ReportsRepository(private val dbHelper: DatabaseHelper) {

    data class CategorySpending(
        val category: String,
        val totalAmount: Double,
        val percentage: Double,
        val icon: String
    )

    // Get category icons mapping
    private fun getCategoryIcon(category: String): String = when (category.lowercase()) {
        "food" -> "🍔"
        "transport" -> "🚗"
        "health" -> "💊"
        "shopping" -> "🛒"
        "entertainment" -> "🎬"
        else -> "📦"
    }

    // Get spending by category for a date range
    fun getSpendingByCategory(startDate: String, endDate: String): List<CategorySpending> {
        val db = dbHelper.readableDatabase
        val results = mutableListOf<CategorySpending>()

        // Updated query to join with categories table
        val query = """
            SELECT 
                ${DatabaseHelper.TABLE_CATEGORIES}.${DatabaseHelper.COL_CATEGORY_NAME} as category_name,
                ${DatabaseHelper.TABLE_CATEGORIES}.${DatabaseHelper.COL_CATEGORY_ICON} as category_icon,
                SUM(${DatabaseHelper.TABLE_EXPENSES}.${DatabaseHelper.COL_AMOUNT}) as total_spent
            FROM ${DatabaseHelper.TABLE_EXPENSES}
            JOIN ${DatabaseHelper.TABLE_CATEGORIES} 
                ON ${DatabaseHelper.TABLE_EXPENSES}.${DatabaseHelper.COL_EXPENSE_CATEGORY_ID} = ${DatabaseHelper.TABLE_CATEGORIES}.${DatabaseHelper.COL_CATEGORY_ID}
            WHERE ${DatabaseHelper.TABLE_EXPENSES}.${DatabaseHelper.COL_DATE} BETWEEN ? AND ?
            GROUP BY ${DatabaseHelper.TABLE_CATEGORIES}.${DatabaseHelper.COL_CATEGORY_NAME}
            ORDER BY total_spent DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(startDate, endDate))

        var grandTotal = 0.0
        val tempList = mutableListOf<Triple<String, String, Double>>()

        while (cursor.moveToNext()) {
            val categoryName = cursor.getString(cursor.getColumnIndexOrThrow("category_name"))
            val categoryIcon = cursor.getString(cursor.getColumnIndexOrThrow("category_icon"))
            val total = cursor.getDouble(cursor.getColumnIndexOrThrow("total_spent"))
            tempList.add(Triple(categoryName, categoryIcon, total))
            grandTotal += total
        }
        cursor.close()

        // Calculate percentages and create result objects
        tempList.forEach { (categoryName, categoryIcon, total) ->
            val percentage = if (grandTotal > 0) (total / grandTotal) * 100 else 0.0
            results.add(
                CategorySpending(
                    category = categoryName,
                    totalAmount = total,
                    percentage = percentage,
                    icon = categoryIcon
                )
            )
        }

        return results
    }

    // Get total spending for a date range
    fun getTotalSpending(startDate: String, endDate: String): Double {
        val db = dbHelper.readableDatabase

        val query = """
            SELECT SUM(${DatabaseHelper.COL_AMOUNT}) as total
            FROM ${DatabaseHelper.TABLE_EXPENSES}
            WHERE ${DatabaseHelper.COL_DATE} BETWEEN ? AND ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(startDate, endDate))

        var total = 0.0
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"))
        }
        cursor.close()

        return total
    }

    // Get expense count for a date range
    fun getExpenseCount(startDate: String, endDate: String): Int {
        val db = dbHelper.readableDatabase

        val query = """
            SELECT COUNT(*) as count
            FROM ${DatabaseHelper.TABLE_EXPENSES}
            WHERE ${DatabaseHelper.COL_DATE} BETWEEN ? AND ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(startDate, endDate))

        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndexOrThrow("count"))
        }
        cursor.close()

        return count
    }

    // Get total spending per day average
    fun getAverageDailySpending(startDate: String, endDate: String): Double {
        val total = getTotalSpending(startDate, endDate)

        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val start = format.parse(startDate)
        val end = format.parse(endDate)

        val days = if (start != null && end != null) {
            val diff = end.time - start.time
            (diff / (24 * 60 * 60 * 1000)).toInt() + 1
        } else {
            30
        }

        return if (days > 0) total / days else 0.0
    }

    // Get top categories (limited)
    fun getTopCategories(startDate: String, endDate: String, limit: Int = 3): List<CategorySpending> {
        return getSpendingByCategory(startDate, endDate).take(limit)
    }
}