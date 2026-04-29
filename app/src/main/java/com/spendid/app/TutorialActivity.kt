package com.spendid.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.util.Calendar

class TutorialActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnBack: MaterialButton
    private lateinit var btnNext: MaterialButton
    private lateinit var btnSkip: TextView
    private lateinit var stepIndicator: TextView
    private lateinit var title: TextView
    private lateinit var dot1: View
    private lateinit var dot2: View
    private lateinit var dot3: View
    private lateinit var dot4: View

    private var userId: Int = -1
    private var username: String = ""

    private val userRepository by lazy { UserRepository(DatabaseHelper(this)) }
    private val budgetRepository by lazy { BudgetRepository(DatabaseHelper(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        userId = intent.getIntExtra("USER_ID", -1)
        username = intent.getStringExtra("USERNAME") ?: ""

        viewPager = findViewById(R.id.viewPager)
        btnBack = findViewById(R.id.btnBack)
        btnNext = findViewById(R.id.btnNext)
        btnSkip = findViewById(R.id.btnSkip)
        stepIndicator = findViewById(R.id.stepIndicator)
        title = findViewById(R.id.title)
        dot1 = findViewById(R.id.dot1)
        dot2 = findViewById(R.id.dot2)
        dot3 = findViewById(R.id.dot3)
        dot4 = findViewById(R.id.dot4)

        val adapter = TutorialPagerAdapter(this)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateUIForStep(position)
            }
        })

        btnBack.setOnClickListener {
            if (viewPager.currentItem > 0) {
                viewPager.currentItem = viewPager.currentItem - 1
            }
        }

        btnNext.setOnClickListener {
            if (viewPager.currentItem < adapter.itemCount - 1) {
                viewPager.currentItem = viewPager.currentItem + 1
            } else {
                completeTutorial()
            }
        }

        btnSkip.setOnClickListener {
            completeTutorial()
        }
    }

    private fun updateUIForStep(position: Int) {
        val isFirst = position == 0
        val isLast = position == 3
        btnBack.isGone = isFirst
        btnNext.text = if (isLast) "GET STARTED →" else "NEXT →"
        stepIndicator.text = "STEP ${position + 1} OF 4"
        title.text = when (position) {
            0 -> "Welcome to\nSPENDID"
            1 -> "Your financial goal"
            2 -> "Spending habits"
            3 -> "Preferences"
            else -> ""
        }
        dot1.setBackgroundResource(if (position == 0) R.drawable.bg_dot_selected else R.drawable.bg_dot_unselected)
        dot2.setBackgroundResource(if (position == 1) R.drawable.bg_dot_selected else R.drawable.bg_dot_unselected)
        dot3.setBackgroundResource(if (position == 2) R.drawable.bg_dot_selected else R.drawable.bg_dot_unselected)
        dot4.setBackgroundResource(if (position == 3) R.drawable.bg_dot_selected else R.drawable.bg_dot_unselected)
    }

    private fun completeTutorial() {
        lifecycleScope.launch {
            // Get data from fragments
            val step1Fragment = supportFragmentManager.findFragmentByTag("f0") as? Step1Fragment
            val step2Fragment = supportFragmentManager.findFragmentByTag("f1") as? Step2Fragment
            val step3Fragment = supportFragmentManager.findFragmentByTag("f2") as? Step3Fragment
            val step4Fragment = supportFragmentManager.findFragmentByTag("f3") as? Step4Fragment

            val userName = step1Fragment?.getUserName() ?: username
            val salary = step1Fragment?.getSalary() ?: 0.0
            val financialGoal = step2Fragment?.getSelectedGoal() ?: ""
            val spendingHabit = step3Fragment?.getSelectedHabit() ?: ""
            val budgetAlerts = step4Fragment?.getBudgetAlerts() ?: true
            val dailyReminder = step4Fragment?.getDailyReminder() ?: true
            val badgeNotifications = step4Fragment?.getBadgeNotifications() ?: false
            val darkMode = step4Fragment?.getDarkMode() ?: false

            // Update user in database
            val user = userRepository.getUserById(userId)
            if (user != null) {
                userRepository.updateUserWithTutorialOptions(
                    userId = userId,
                    tutorialCompleted = true,
                    financialGoal = financialGoal,
                    spendingHabit = spendingHabit,
                    budgetAlerts = budgetAlerts,
                    dailyReminder = dailyReminder,
                    badgeNotifications = badgeNotifications,
                    darkMode = darkMode
                )

                // Save the monthly salary to budget table
                if (salary > 0) {
                    val calendar = Calendar.getInstance()
                    val month = calendar.get(Calendar.MONTH)
                    val year = calendar.get(Calendar.YEAR)
                    budgetRepository.saveBudget(salary, month, year)
                }
            }

            // Navigate to MainActivity
            startActivity(Intent(this@TutorialActivity, MainActivity::class.java).apply {
                putExtra("USERNAME", if (userName.isNotEmpty()) userName else username)
            })
            finish()
        }
    }
}