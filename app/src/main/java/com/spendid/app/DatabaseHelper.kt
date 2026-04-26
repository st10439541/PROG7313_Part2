package com.spendid.app

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "spendid_db.db"
        const val DATABASE_VERSION = 4

        // Users table
        const val TABLE_USERS = "users"
        const val COL_ID = "id"
        const val COL_USERNAME = "username"
        const val COL_PASSWORD_HASH = "passwordHash"
        const val COL_SALT = "salt"
        const val COL_TUTORIAL_COMPLETED = "tutorialCompleted"

        // Goals table
        const val TABLE_GOALS = "goals"
        const val COL_USER_ID = "userId"
        const val COL_MIN_GOAL = "minGoal"
        const val COL_MAX_GOAL = "maxGoal"
        const val COL_CREATED_DATE = "createdDate"

        // Expenses table
        const val TABLE_EXPENSES = "expenses"
        const val EXP_COL_ID = "id"
        const val EXP_COL_USER_ID = "userId"
        const val EXP_COL_DATE = "date"
        const val EXP_COL_TIME_START = "timeStart"
        const val EXP_COL_TIME_END = "timeEnd"
        const val EXP_COL_DESCRIPTION = "description"
        const val EXP_COL_AMOUNT = "amount"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Users table (existing)
        db.execSQL(
            """
            CREATE TABLE $TABLE_USERS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USERNAME TEXT NOT NULL UNIQUE,
                $COL_PASSWORD_HASH TEXT NOT NULL,
                $COL_SALT TEXT NOT NULL,
                $COL_TUTORIAL_COMPLETED INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )

        // Goals table
        db.execSQL(
            """
            CREATE TABLE $TABLE_GOALS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_ID INTEGER NOT NULL,
                $COL_MIN_GOAL REAL NOT NULL,
                $COL_MAX_GOAL REAL NOT NULL,
                $COL_CREATED_DATE TEXT NOT NULL,
                FOREIGN KEY($COL_USER_ID) REFERENCES $TABLE_USERS($COL_ID),
                PRIMARY KEY($COL_USER_ID)
            )
            """.trimIndent()
        )

        // Expenses table
        db.execSQL(
            """
            CREATE TABLE $TABLE_EXPENSES (
                $EXP_COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $EXP_COL_USER_ID INTEGER NOT NULL,
                $EXP_COL_DATE TEXT NOT NULL,
                $EXP_COL_TIME_START TEXT NOT NULL,
                $EXP_COL_TIME_END TEXT NOT NULL,
                $EXP_COL_DESCRIPTION TEXT,
                $EXP_COL_AMOUNT REAL,
                FOREIGN KEY($EXP_COL_USER_ID) REFERENCES $TABLE_USERS($COL_ID)
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 4) {
            // Add goals table
            db.execSQL(
                """
                CREATE TABLE $TABLE_GOALS (
                    $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COL_USER_ID INTEGER NOT NULL,
                    $COL_MIN_GOAL REAL NOT NULL,
                    $COL_MAX_GOAL REAL NOT NULL,
                    $COL_CREATED_DATE TEXT NOT NULL,
                    FOREIGN KEY($COL_USER_ID) REFERENCES $TABLE_USERS($COL_ID),
                    PRIMARY KEY($COL_USER_ID)
                )
                """.trimIndent()
            )

            // Add expenses table
            db.execSQL(
                """
                CREATE TABLE $TABLE_EXPENSES (
                    $EXP_COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $EXP_COL_USER_ID INTEGER NOT NULL,
                    $EXP_COL_DATE TEXT NOT NULL,
                    $EXP_COL_TIME_START TEXT NOT NULL,
                    $EXP_COL_TIME_END TEXT NOT NULL,
                    $EXP_COL_DESCRIPTION TEXT,
                    $EXP_COL_AMOUNT REAL,
                    FOREIGN KEY($EXP_COL_USER_ID) REFERENCES $TABLE_USERS($COL_ID)
                )
                """.trimIndent()
            )
        }
    }

    // Goals methods
    fun saveGoals(userId: Int, minGoal: Double, maxGoal: Double): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_ID, userId)
            put(COL_MIN_GOAL, minGoal)
            put(COL_MAX_GOAL, maxGoal)
            put(COL_CREATED_DATE, getCurrentDateTime())
        }
        return db.replace(TABLE_GOALS, null, values) > 0
    }

    fun getGoals(userId: Int): Goals? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_GOALS, arrayOf(COL_MIN_GOAL, COL_MAX_GOAL),
            "$COL_USER_ID = ?", arrayOf(userId.toString()),
            null, null, null
        )
        return if (cursor.moveToFirst()) {
            Goals(
                minGoal = cursor.getDouble(0),
                maxGoal = cursor.getDouble(1)
            )
        } else null.also { cursor.close() }
    }

    // Expenses methods
    fun insertExpense(userId: Int, date: String, timeStart: String, timeEnd: String,
                      description: String?, amount: Double?): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(EXP_COL_USER_ID, userId)
            put(EXP_COL_DATE, date)
            put(EXP_COL_TIME_START, timeStart)
            put(EXP_COL_TIME_END, timeEnd)
            put(EXP_COL_DESCRIPTION, description)
            put(EXP_COL_AMOUNT, amount)
        }
        return db.insert(TABLE_EXPENSES, null, values)
    }

    fun getExpenses(userId: Int, startDate: String? = null, endDate: String? = null): List<Expense> {
        val db = readableDatabase
        val expenses = mutableListOf<Expense>()
        val selection = if (startDate != null && endDate != null) {
            "$EXP_COL_USER_ID = ? AND $EXP_COL_DATE BETWEEN ? AND ?"
        } else {
            "$EXP_COL_USER_ID = ?"
        }
        val selectionArgs = if (startDate != null && endDate != null) {
            arrayOf(userId.toString(), startDate, endDate)
        } else {
            arrayOf(userId.toString())
        }

        val cursor = db.query(
            TABLE_EXPENSES, null, selection, selectionArgs,
            null, null, "$EXP_COL_DATE DESC, $EXP_COL_TIME_START DESC"
        )

        with(cursor) {
            while (moveToNext()) {
                expenses.add(
                    Expense(
                        id = getLong(getColumnIndexOrThrow(EXP_COL_ID)),
                        date = getString(getColumnIndexOrThrow(EXP_COL_DATE)),
                        timeStart = getString(getColumnIndexOrThrow(EXP_COL_TIME_START)),
                        timeEnd = getString(getColumnIndexOrThrow(EXP_COL_TIME_END)),
                        description = getString(getColumnIndexOrThrow(EXP_COL_DESCRIPTION)),
                        amount = getDoubleOrNull(getColumnIndexOrThrow(EXP_COL_AMOUNT))
                    )
                )
            }
            close()
        }
        return expenses
    }

    private fun getCurrentDateTime(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }

    private fun Cursor.getDoubleOrNull(columnIndex: Int): Double? {
        return if (isNull(columnIndex)) null else getDouble(columnIndex)
    }
}

data class Goals(val minGoal: Double, val maxGoal: Double)
data class Expense(
    val id: Long,
    val date: String,
    val timeStart: String,
    val timeEnd: String,
    val description: String?,
    val amount: Double?
)