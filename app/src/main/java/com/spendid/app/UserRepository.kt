package com.spendid.app

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

class UserRepository(private val dbHelper: DatabaseHelper) {

    /**
     * Insert a new user. Returns the new row ID, or -1 if username already exists.
     */
    fun insertUser(username: String, password: String): Long {
        val db = dbHelper.writableDatabase
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash(password, salt)
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_USERNAME, username)
            put(DatabaseHelper.COL_PASSWORD_HASH, hash)
            put(DatabaseHelper.COL_SALT, salt)
            put(DatabaseHelper.COL_TUTORIAL_COMPLETED, 0)
        }
        return db.insertWithOnConflict(
            DatabaseHelper.TABLE_USERS,
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE
        )
    }

    /**
     * Check if username exists (returns count).
     */
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

    /**
     * Login: returns UserEntity if username+passwordHash match, else null.
     */
    fun login(username: String, password: String): UserEntity? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_USERS} WHERE ${DatabaseHelper.COL_USERNAME} = ?",
            arrayOf(username)
        )
        return cursor.use {
            if (it.moveToFirst()) {
                val storedHash = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COL_PASSWORD_HASH))
                val salt = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COL_SALT))
                if (PasswordHasher.verify(password, salt, storedHash)) {
                    UserEntity(
                        id = it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.COL_ID)),
                        username = username,
                        passwordHash = storedHash,
                        salt = salt,
                        tutorialCompleted = it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.COL_TUTORIAL_COMPLETED)) == 1
                    )
                } else null
            } else null
        }
    }

    /**
     * Get user by ID (used by tutorial activity).
     */
    fun getUserById(id: Int): UserEntity? {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${DatabaseHelper.TABLE_USERS} WHERE ${DatabaseHelper.COL_ID} = ?",
            arrayOf(id.toString())
        )
        return cursor.use {
            if (it.moveToFirst()) {
                UserEntity(
                    id = it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.COL_ID)),
                    username = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COL_USERNAME)),
                    passwordHash = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COL_PASSWORD_HASH)),
                    salt = it.getString(it.getColumnIndexOrThrow(DatabaseHelper.COL_SALT)),
                    tutorialCompleted = it.getInt(it.getColumnIndexOrThrow(DatabaseHelper.COL_TUTORIAL_COMPLETED)) == 1
                )
            } else null
        }
    }

    /**
     * Update user (e.g., mark tutorial completed).
     */
    fun updateUser(user: UserEntity) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_TUTORIAL_COMPLETED, if (user.tutorialCompleted) 1 else 0)
            // In case you ever update other fields, add them here.
        }
        db.update(
            DatabaseHelper.TABLE_USERS,
            values,
            "${DatabaseHelper.COL_ID} = ?",
            arrayOf(user.id.toString())
        )
    }
}