package com.spendid.app

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class GoalSettingActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var etMinGoal: TextInputEditText
    private lateinit var etMaxGoal: TextInputEditText
    private var currentUserId = 1 // TODO: Get from login session

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal_setting) // Create this layout

        dbHelper = DatabaseHelper(this)
        etMinGoal = findViewById(R.id.etMinGoal)
        etMaxGoal = findViewById(R.id.etMaxGoal)
        val btnSave = findViewById<Button>(R.id.btnSaveGoals)

        loadCurrentGoals()
        btnSave.setOnClickListener {
            saveGoals()
        }
    }

    private fun loadCurrentGoals() {
        val goals = dbHelper.getGoals(currentUserId)
        goals?.let {
            etMinGoal.setText(it.minGoal.toString())
            etMaxGoal.setText(it.maxGoal.toString())
        }
    }

    private fun saveGoals() {
        val minGoal = etMinGoal.text.toString().toDoubleOrNull()
        val maxGoal = etMaxGoal.text.toString().toDoubleOrNull()

        if (minGoal == null || maxGoal == null || minGoal >= maxGoal) {
            Toast.makeText(this, "Please enter valid goals (min < max)", Toast.LENGTH_SHORT).show()
            return
        }

        if (dbHelper.saveGoals(currentUserId, minGoal, maxGoal)) {
            Toast.makeText(this, "Goals saved successfully!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to save goals", Toast.LENGTH_SHORT).show()
        }
    }
}