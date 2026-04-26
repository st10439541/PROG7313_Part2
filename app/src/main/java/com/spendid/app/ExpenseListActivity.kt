package com.spendid.app

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ExpenseListActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var expenseRecycler: RecyclerView
    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView
    private lateinit var adapter: ExpenseAdapter
    private var currentUserId = 1 // TODO: Get from login session
    private var startDateFilter: String? = null
    private var endDateFilter: String? = null

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_list)

        dbHelper = DatabaseHelper(this)
        initViews()
        loadExpenses()
        setupDateFilters()
    }

    private fun initViews() {
        expenseRecycler = findViewById(R.id.expenseRecycler)
        tvStartDate = findViewById(R.id.tvStartDate)
        tvEndDate = findViewById(R.id.tvEndDate)

        expenseRecycler.layoutManager = LinearLayoutManager(this)
    }

    private fun loadExpenses() {
        val expenses = dbHelper.getExpenses(currentUserId, startDateFilter, endDateFilter)
        adapter = ExpenseAdapter(expenses)
        expenseRecycler.adapter = adapter
    }

    private fun setupDateFilters() {
        tvStartDate.setOnClickListener { showDatePicker { date ->
            startDateFilter = dateFormat.format(date)
            tvStartDate.text = startDateFilter
            loadExpenses()
        }}

        tvEndDate.setOnClickListener { showDatePicker { date ->
            endDateFilter = dateFormat.format(date)
            tvEndDate.text = endDateFilter
            loadExpenses()
        }}
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}