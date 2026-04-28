package com.spendid.app

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    private lateinit var navHome: LinearLayout
    private lateinit var navExpenses: LinearLayout
    private lateinit var navAdd: TextView
    private lateinit var navReports: LinearLayout
    private lateinit var navProfile: LinearLayout

    private var currentUsername: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Get username from intent
        currentUsername = intent.getStringExtra("USERNAME") ?: "User"

        navHome = findViewById(R.id.navHome)
        navExpenses = findViewById(R.id.navExpenses)
        navAdd = findViewById(R.id.navAdd)
        navReports = findViewById(R.id.navReports)
        navProfile = findViewById(R.id.navProfile)

        setupClickListeners()

        if (savedInstanceState == null) {
            loadFragment(HomeFragment.newInstance(currentUsername))
            highlightNavItem(navHome)
        } else {
            highlightCurrentMenuItem()
        }
    }

    private fun setupClickListeners() {
        navHome.setOnClickListener {
            loadFragment(HomeFragment.newInstance(currentUsername))
            highlightNavItem(navHome)
        }

        navExpenses.setOnClickListener {
            loadFragment(ExpensesFragment())
            highlightNavItem(navExpenses)
        }

        navAdd.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        navReports.setOnClickListener {
            loadFragment(ReportsFragment())
            highlightNavItem(navReports)
        }

        navProfile.setOnClickListener {
            loadFragment(ProfileFragment.newInstance(currentUsername))
            highlightNavItem(navProfile)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.contentContainer, fragment)
            .commit()
    }

    private fun highlightNavItem(selectedNav: LinearLayout) {
        val allNav = listOf(navHome, navExpenses, navReports, navProfile)
        allNav.forEach { nav ->
            val textView = nav.getChildAt(1) as? TextView
            val color = if (nav == selectedNav) R.color.forest else R.color.hint_text
            textView?.setTextColor(getColor(color))
        }
    }

    private fun highlightCurrentMenuItem() {
        val selected = intent.getStringExtra("selected_nav_item") ?: "home"
        when (selected) {
            "home" -> highlightNavItem(navHome)
            "expenses" -> highlightNavItem(navExpenses)
            "profile" -> highlightNavItem(navProfile)
            else -> highlightNavItem(navHome)
        }
    }

    fun navigateToReports() {
        loadFragment(ReportsFragment())
        highlightNavItem(navReports)
    }
}