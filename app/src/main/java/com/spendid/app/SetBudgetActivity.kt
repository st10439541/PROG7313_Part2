package com.spendid.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.*

class SetBudgetActivity : AppCompatActivity() {

    private lateinit var etBudgetAmount: TextInputEditText
    private lateinit var etMinGoal: TextInputEditText
    private lateinit var etMaxGoal: TextInputEditText
    private lateinit var btnSaveBudget: MaterialButton
    private lateinit var btnBack: MaterialButton

    private lateinit var budgetRepository: BudgetRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_budget)

        etBudgetAmount = findViewById(R.id.etBudgetAmount)
        etMinGoal = findViewById(R.id.etMinGoal)
        etMaxGoal = findViewById(R.id.etMaxGoal)
        btnSaveBudget = findViewById(R.id.btnSaveBudget)
        btnBack = findViewById(R.id.btnBack)

        budgetRepository = BudgetRepository(DatabaseHelper(this))

        // Load existing budget if any
        loadExistingBudget()

        btnSaveBudget.setOnClickListener {
            saveBudget()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadExistingBudget() {
        val currentBudget = budgetRepository.getCurrentBudget()
        if (currentBudget != null) {
            etBudgetAmount.setText(currentBudget.amount.toString())
            currentBudget.minGoal?.let { etMinGoal.setText(it.toString()) }
            currentBudget.maxGoal?.let { etMaxGoal.setText(it.toString()) }
        }
    }

    private fun saveBudget() {
        val budgetText = etBudgetAmount.text.toString().trim()

        if (budgetText.isEmpty()) {
            Toast.makeText(this, "Please enter a budget amount", Toast.LENGTH_SHORT).show()
            return
        }

        val budgetAmount = budgetText.toDoubleOrNull()
        if (budgetAmount == null || budgetAmount <= 0) {
            Toast.makeText(this, "Please enter a valid budget amount", Toast.LENGTH_SHORT).show()
            return
        }

        val minGoalText = etMinGoal.text.toString().trim()
        val maxGoalText = etMaxGoal.text.toString().trim()

        val minGoal = if (minGoalText.isNotEmpty()) minGoalText.toDoubleOrNull() else null
        val maxGoal = if (maxGoalText.isNotEmpty()) maxGoalText.toDoubleOrNull() else null

        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)

        val result = budgetRepository.saveBudget(budgetAmount, month, year, minGoal, maxGoal)

        if (result != -1L) {
            Toast.makeText(this, "Budget saved successfully", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Failed to save budget", Toast.LENGTH_SHORT).show()
        }
    }
}