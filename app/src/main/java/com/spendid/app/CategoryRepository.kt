package com.spendid.app

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

class CategoryRepository(private val dbHelper: DatabaseHelper) {

    fun getAllCategories(): List<Category> {
        val categories = mutableListOf<Category>()
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_CATEGORIES} ORDER BY ${DatabaseHelper.COL_CATEGORY_NAME}",
            null
        )

        while (cursor.moveToNext()) {
            val category = Category(
                categoryId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_NAME)),
                icon = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_ICON))
            )
            categories.add(category)
        }
        cursor.close()
        return categories
    }

    fun insertCategory(name: String, icon: String = "📦"): Long {
        val db = dbHelper.writableDatabase

        // Check if category already exists
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_CATEGORIES} WHERE ${DatabaseHelper.COL_CATEGORY_NAME} = ?",
            arrayOf(name)
        )

        if (cursor.moveToFirst()) {
            cursor.close()
            return -1 // Category already exists
        }
        cursor.close()

        val values = ContentValues().apply {
            put(DatabaseHelper.COL_CATEGORY_NAME, name)
            put(DatabaseHelper.COL_CATEGORY_ICON, icon)
        }

        return db.insert(DatabaseHelper.TABLE_CATEGORIES, null, values)
    }

    fun deleteCategory(categoryId: Int): Int {
        val db = dbHelper.writableDatabase

        // Check if category is used in expenses
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_EXPENSES} WHERE ${DatabaseHelper.COL_CATEGORY_ID} = ?",
            arrayOf(categoryId.toString())
        )

        val isUsed = cursor.moveToFirst()
        cursor.close()

        if (isUsed) {
            return -2 // Category is in use, cannot delete
        }

        return db.delete(
            DatabaseHelper.TABLE_CATEGORIES,
            "${DatabaseHelper.COL_CATEGORY_ID} = ?",
            arrayOf(categoryId.toString())
        )
    }

    fun getCategoryById(categoryId: Int): Category? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_CATEGORIES} WHERE ${DatabaseHelper.COL_CATEGORY_ID} = ?",
            arrayOf(categoryId.toString())
        )

        return cursor.use {
            if (it.moveToFirst()) {
                Category(
                    categoryId = it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_ID)),
                    name = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_NAME)),
                    icon = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_ICON))
                )
            } else null
        }
    }

    fun getCategoryNameById(categoryId: Int): String {
        return getCategoryById(categoryId)?.name ?: "Unknown"
    }
}