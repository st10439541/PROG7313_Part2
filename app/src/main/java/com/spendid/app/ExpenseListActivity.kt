package com.spendid.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ExpenseListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    private val repository by lazy {
        ExpenseRepository(DatabaseHelper(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_list)

        recyclerView = findViewById(R.id.expenseRecycler)
        recyclerView.layoutManager = LinearLayoutManager(this)

        setupBottomNav()
        loadExpenses()
    }

    private fun loadExpenses() {
        val list = repository.getAllExpenses()

        Log.d("ExpenseList", "Loaded: ${list.size}")

        recyclerView.adapter = ExpenseAdapter(list)
    }

    private fun setupBottomNav() {

        val home = findViewById<android.view.View>(R.id.navHome)
        val add = findViewById<android.view.View>(R.id.navAdd)
        val reports = findViewById<android.view.View>(R.id.navReports)
        val profile = findViewById<android.view.View>(R.id.navProfile)

        home.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        add.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        reports.setOnClickListener {
            startActivity(Intent(this, ReportsActivity::class.java))
        }

        profile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}