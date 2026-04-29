package com.spendid.app

import android.content.ContentValues

class ExpenseRepository(private val dbHelper: DatabaseHelper) {

    fun insertExpense(
        amount: Double,
        description: String,
        categoryId: Int,
        date: String,
        time: String,
        imageUri: String?
    ): Long {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DatabaseHelper.COL_AMOUNT, amount)
            put(DatabaseHelper.COL_DESCRIPTION, description)
            put(DatabaseHelper.COL_EXPENSE_CATEGORY_ID, categoryId) // fixed constant name
            put(DatabaseHelper.COL_DATE, date)
            put(DatabaseHelper.COL_START_TIME, time)
            put(DatabaseHelper.COL_END_TIME, time)
            put(DatabaseHelper.COL_IMAGE_URI, imageUri)
        }

        return db.insert(DatabaseHelper.TABLE_EXPENSES, null, values)
    }

    fun getAllExpenses(): List<Expense> {
        val list = mutableListOf<Expense>()
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_EXPENSES} ORDER BY ${DatabaseHelper.COL_EXPENSE_ID} DESC",
            null
        )

        while (cursor.moveToNext()) {
            val expense = Expense(
                id = cursor.getInt(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EXPENSE_ID)
                ),
                amount = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COL_AMOUNT)
                ),
                description = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DESCRIPTION)
                ),
                categoryId = cursor.getInt(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EXPENSE_CATEGORY_ID)
                ),
                date = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DATE)
                ),
                time = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COL_START_TIME)
                ),
                imageUri = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COL_IMAGE_URI)
                )
            )

            list.add(expense)
        }

        cursor.close()
        return list
    }

    // FIX: Added delete functionality for removing expenses
    fun deleteExpense(id: Int): Int {
        val db = dbHelper.writableDatabase

        return db.delete(
            DatabaseHelper.TABLE_EXPENSES,
            "${DatabaseHelper.COL_EXPENSE_ID} = ?",
            arrayOf(id.toString())
        )
    }
}