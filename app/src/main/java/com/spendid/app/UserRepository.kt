package com.spendid.app

import android.content.ContentValues

class UserRepository(private val dbHelper: DatabaseHelper) {

    fun insertUser(username: String, password: String): Long {

        val db = dbHelper.writableDatabase

        return try {
            db.beginTransaction()

            val existsCursor = db.rawQuery(
                "SELECT id FROM ${DatabaseHelper.TABLE_USERS} WHERE ${DatabaseHelper.COL_USERNAME} = ?",
                arrayOf(username)
            )

            val exists = existsCursor.use {
                it.moveToFirst()
            }

            if (exists) {
                println("USERNAME EXISTS")
                return -1
            }

            // FIX: removed duplicate writableDatabase call
            val salt = PasswordHasher.generateSalt()
            val hash = PasswordHasher.hash(password, salt)

            println("USERNAME = $username")
            println("PASSWORD LENGTH = ${password.length}")
            println("SALT = $salt")
            println("HASH = $hash")

            val values = ContentValues().apply {
                put(DatabaseHelper.COL_USERNAME, username)
                put(DatabaseHelper.COL_PASSWORD_HASH, hash)
                put(DatabaseHelper.COL_SALT, salt)
                put(DatabaseHelper.COL_TUTORIAL_COMPLETED, 0)
            }

            val result = db.insert(
                DatabaseHelper.TABLE_USERS,
                null,
                values
            )

            db.setTransactionSuccessful()

            println("INSERT RESULT = $result")

            result

        } catch (e: Exception) {
            println("DB ERROR: ${e.message}")
            e.printStackTrace()
            -1
        } finally {
            db.endTransaction()
        }
    }

    fun usernameExists(username: String): Int {
        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT COUNT(*) 
            FROM ${DatabaseHelper.TABLE_USERS} 
            WHERE ${DatabaseHelper.COL_USERNAME} = ?
            """.trimIndent(),
            arrayOf(username)
        )

        return cursor.use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }
    }

    fun login(username: String, password: String): UserEntity? {

        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT * 
            FROM ${DatabaseHelper.TABLE_USERS}
            WHERE ${DatabaseHelper.COL_USERNAME} = ?
            """.trimIndent(),
            arrayOf(username)
        )

        return cursor.use {

            if (!it.moveToFirst()) return null

            val storedHash = it.getString(
                it.getColumnIndexOrThrow(DatabaseHelper.COL_PASSWORD_HASH)
            )

            val salt = it.getString(
                it.getColumnIndexOrThrow(DatabaseHelper.COL_SALT)
            )

            val valid = PasswordHasher.verify(password, salt, storedHash)

            if (!valid) return null

            UserEntity(
                // FIX: changed COL_ID → COL_USER_ID
                id = it.getInt(
                    it.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)
                ),
                username = username,
                passwordHash = storedHash,
                salt = salt,
                tutorialCompleted = it.getInt(
                    it.getColumnIndexOrThrow(DatabaseHelper.COL_TUTORIAL_COMPLETED)
                ) == 1
            )
        }
    }

    fun getUserById(id: Int): UserEntity? {

        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT * 
            FROM ${DatabaseHelper.TABLE_USERS}
            WHERE ${DatabaseHelper.COL_USER_ID} = ?
            """.trimIndent(),
            arrayOf(id.toString())
        )

        return cursor.use {

            if (!it.moveToFirst()) return null

            UserEntity(
                // FIX: changed COL_ID → COL_USER_ID
                id = it.getInt(
                    it.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)
                ),
                username = it.getString(
                    it.getColumnIndexOrThrow(DatabaseHelper.COL_USERNAME)
                ),
                passwordHash = it.getString(
                    it.getColumnIndexOrThrow(DatabaseHelper.COL_PASSWORD_HASH)
                ),
                salt = it.getString(
                    it.getColumnIndexOrThrow(DatabaseHelper.COL_SALT)
                ),
                tutorialCompleted = it.getInt(
                    it.getColumnIndexOrThrow(DatabaseHelper.COL_TUTORIAL_COMPLETED)
                ) == 1
            )
        }
    }

    fun updateUser(user: UserEntity) {

        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(
                DatabaseHelper.COL_TUTORIAL_COMPLETED,
                if (user.tutorialCompleted) 1 else 0
            )
        }

        db.update(
            DatabaseHelper.TABLE_USERS,
            values,

            // FIX: changed COL_ID → COL_USER_ID
            "${DatabaseHelper.COL_USER_ID} = ?",
            arrayOf(user.id.toString())
        )
    }

    fun updateTutorialOptions(
        userId: Int,
        financialGoal: String,
        spendingHabit: String,
        budgetAlerts: Boolean,
        dailyReminder: Boolean,
        badgeNotifications: Boolean,
        darkMode: Boolean
    ) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_FINANCIAL_GOAL, financialGoal)
            put(DatabaseHelper.COL_SPENDING_HABIT, spendingHabit)
            put(DatabaseHelper.COL_BUDGET_ALERTS, if (budgetAlerts) 1 else 0)
            put(DatabaseHelper.COL_DAILY_REMINDER, if (dailyReminder) 1 else 0)
            put(DatabaseHelper.COL_BADGE_NOTIFICATIONS, if (badgeNotifications) 1 else 0)
            put(DatabaseHelper.COL_DARK_MODE, if (darkMode) 1 else 0)
            put(DatabaseHelper.COL_TUTORIAL_COMPLETED, 1)
        }

        db.update(
            DatabaseHelper.TABLE_USERS,
            values,
            "${DatabaseHelper.COL_ID} = ?",
            arrayOf(userId.toString())
        )
    }
}