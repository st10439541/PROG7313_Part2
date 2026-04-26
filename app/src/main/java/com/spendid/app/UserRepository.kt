package com.spendid.app

import android.content.ContentValues

class UserRepository(private val dbHelper: DatabaseHelper) {

    fun insertUser(username: String, password: String): Long {

        val db = dbHelper.writableDatabase

        return try {

            db.beginTransaction()

            val existsCursor = db.rawQuery(
                "SELECT id FROM users WHERE username = ?",
                arrayOf(username)
            )

            val exists = existsCursor.use {
                it.moveToFirst()
            }

            if (exists) {
                println("USERNAME EXISTS")
                return -1
            }

            val db = dbHelper.writableDatabase

            val salt = PasswordHasher.generateSalt()
            val hash = PasswordHasher.hash(password, salt)

            println("USERNAME = $username")
            println("PASSWORD LENGTH = ${password.length}")
            println("SALT = $salt")
            println("HASH = $hash")
            val values = ContentValues().apply {
                put("username", username)
                put("passwordHash", hash)
                put("salt", salt)
                put("tutorialCompleted", 0)
            }

            val result = db.insert("users", null, values)

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
            "SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_USERS} WHERE ${DatabaseHelper.COL_USERNAME} = ?",
            arrayOf(username)
        )

        return cursor.use {
            if (it.moveToFirst()) it.getInt(0) else 0
        }
    }

    fun login(username: String, password: String): UserEntity? {

        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_USERS} WHERE ${DatabaseHelper.COL_USERNAME} = ?",
            arrayOf(username)
        )

        return cursor.use {

            if (!it.moveToFirst()) return null

            val storedHash = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COL_PASSWORD_HASH))
            val salt = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COL_SALT))

            val valid = PasswordHasher.verify(password, salt, storedHash)

            if (!valid) return null

            UserEntity(
                id = it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.COL_ID)),
                username = username,
                passwordHash = storedHash,
                salt = salt,
                tutorialCompleted = it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.COL_TUTORIAL_COMPLETED)) == 1
            )
        }
    }

    fun getUserById(id: Int): UserEntity? {

        val db = dbHelper.readableDatabase

        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_USERS} WHERE ${DatabaseHelper.COL_ID} = ?",
            arrayOf(id.toString())
        )

        return cursor.use {

            if (!it.moveToFirst()) return null

            UserEntity(
                id = it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.COL_ID)),
                username = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COL_USERNAME)),
                passwordHash = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COL_PASSWORD_HASH)),
                salt = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COL_SALT)),
                tutorialCompleted = it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.COL_TUTORIAL_COMPLETED)) == 1
            )
        }
    }

    fun updateUser(user: UserEntity) {

        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(DatabaseHelper.COL_TUTORIAL_COMPLETED, if (user.tutorialCompleted) 1 else 0)
        }

        db.update(
            DatabaseHelper.TABLE_USERS,
            values,
            "${DatabaseHelper.COL_ID} = ?",
            arrayOf(user.id.toString())
        )
    }
}