package com.spendid.app

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    private lateinit var navHome: LinearLayout
    private lateinit var navExpenses: LinearLayout
    private lateinit var navAdd: TextView          // still a TextView
    private lateinit var navReports: LinearLayout
    private lateinit var navProfile: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        navHome = findViewById(R.id.navHome)
        navExpenses = findViewById(R.id.navExpenses)
        navAdd = findViewById(R.id.navAdd)         // TextView
        navReports = findViewById(R.id.navReports)
        navProfile = findViewById(R.id.navProfile)

        setupClickListeners()

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
            highlightNavItem(navHome)
        } else {
            highlightCurrentMenuItem()
        }
    }

    private fun setupClickListeners() {
        navHome.setOnClickListener {
            loadFragment(HomeFragment())
            highlightNavItem(navHome)
        }

        navExpenses.setOnClickListener {
            loadFragment(ExpenseListFragment())
            highlightNavItem(navExpenses)
        }

        navAdd.setOnClickListener {
            loadFragment(AddExpenseFragment())
            highlightNavItem(navAdd)   // now works because we accept View
        }

//        navReports.setOnClickListener {
//            loadFragment(ReportsFragment())
//            highlightNavItem(navReports)
//        }

        navProfile.setOnClickListener {
            loadFragment(ProfileFragment())
            highlightNavItem(navProfile)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.contentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    // Now accepts any View, handles both TextView and parent containers
    private fun highlightNavItem(selectedNav: View) {
        // Build a list of all the clickable navigation items
        val allNavItems = listOf<View>(navHome, navExpenses, navAdd, navReports, navProfile)

        for (nav in allNavItems) {
            val colorRes = if (nav == selectedNav) R.color.forest else R.color.hint_text
            val colorInt = getColor(colorRes)

            when (nav) {
                is TextView -> {
                    // The add button is already a TextView
                    nav.setTextColor(colorInt)
                }
                is LinearLayout -> {
                    // Nav items that contain an icon + text (child at index 1 is the label)
                    val textView = nav.getChildAt(1) as? TextView
                    textView?.setTextColor(colorInt)
                }
            }
        }
    }

    private fun highlightCurrentMenuItem() {
        val selected = intent.getStringExtra("selected_nav_item") ?: "home"
        when (selected) {
            "home" -> highlightNavItem(navHome)
            "expenses" -> highlightNavItem(navExpenses)
            "reports" -> highlightNavItem(navReports)
            "profile" -> highlightNavItem(navProfile)
            else -> highlightNavItem(navHome)
        }
    }
}