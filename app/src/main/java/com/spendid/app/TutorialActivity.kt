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

class TutorialActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnBack: MaterialButton
    private lateinit var btnNext: MaterialButton
    private lateinit var btnSkip: TextView             // ← CHANGED from MaterialButton
    private lateinit var stepIndicator: TextView
    private lateinit var title: TextView
    private lateinit var dot1: View
    private lateinit var dot2: View
    private lateinit var dot3: View
    private lateinit var dot4: View

    private var userId: Int = -1

    private val repository by lazy { UserRepository(DatabaseHelper(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        userId = intent.getIntExtra("USER_ID", -1)

        viewPager = findViewById(R.id.viewPager)
        btnBack = findViewById(R.id.btnBack)
        btnNext = findViewById(R.id.btnNext)
        btnSkip = findViewById(R.id.btnSkip)       // now a TextView
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
            val user = repository.getUserById(userId)
            if (user != null) {
                repository.updateUser(user.copy(tutorialCompleted = true))
            }
            startActivity(Intent(this@TutorialActivity, MainActivity::class.java).apply {
                putExtra("USERNAME", user?.username ?: "")
            })
            finish()
        }
    }
}