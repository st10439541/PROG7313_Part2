package com.spendid.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

object BottomNavHelper {

    fun setup(activity: AppCompatActivity, selected: Int) {

        val navHome = activity.findViewById<android.view.View>(R.id.navHome)
        val navExpenses = activity.findViewById<android.view.View>(R.id.navExpenses)
        val navAdd = activity.findViewById<android.view.View>(R.id.navAdd)
        val navReports = activity.findViewById<android.view.View>(R.id.navReports)
        val navProfile = activity.findViewById<android.view.View>(R.id.navProfile)

        navHome.setOnClickListener {
            activity.startActivity(Intent(activity, HomeActivity::class.java))
        }

        navExpenses.setOnClickListener {
            activity.startActivity(Intent(activity, ExpenseListActivity::class.java))
        }

        navAdd.setOnClickListener {
            activity.startActivity(Intent(activity, AddExpenseActivity::class.java))
        }

        navReports.setOnClickListener {
            activity.startActivity(Intent(activity, ReportsActivity::class.java)) // ✅ FIXED
        }

        navProfile.setOnClickListener {
            activity.startActivity(Intent(activity, ProfileActivity::class.java))
        }
    }
}