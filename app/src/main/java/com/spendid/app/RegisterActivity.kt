package com.spendid.app

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var usernameLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText

    // Strength meter views
    private lateinit var strengthMeter: View
    private lateinit var strengthBar1: View
    private lateinit var strengthBar2: View
    private lateinit var strengthBar3: View
    private lateinit var strengthBar4: View
    private lateinit var strengthLabel: android.widget.TextView

    // Repository – uses plain SQLite
    private val repository by lazy { UserRepository(DatabaseHelper(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        usernameLayout        = findViewById(R.id.usernameLayout)
        passwordLayout        = findViewById(R.id.passwordLayout)
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout)
        usernameInput         = findViewById(R.id.usernameInput)
        passwordInput         = findViewById(R.id.passwordInput)
        confirmPasswordInput  = findViewById(R.id.confirmPasswordInput)
        strengthMeter         = findViewById(R.id.strengthMeter)
        strengthBar1          = findViewById(R.id.strengthBar1)
        strengthBar2          = findViewById(R.id.strengthBar2)
        strengthBar3          = findViewById(R.id.strengthBar3)
        strengthBar4          = findViewById(R.id.strengthBar4)
        strengthLabel         = findViewById(R.id.strengthLabel)

        // Live password strength feedback
        passwordInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { updateStrengthMeter(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        findViewById<MaterialButton>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<MaterialButton>(R.id.btnGoToLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        findViewById<MaterialButton>(R.id.btnCreateAccount).setOnClickListener {
            attemptRegister()
        }
    }

    private fun attemptRegister() {
        usernameLayout.error        = null
        passwordLayout.error        = null
        confirmPasswordLayout.error = null

        val username        = usernameInput.text.toString().trim()
        val password        = passwordInput.text.toString()
        val confirmPassword = confirmPasswordInput.text.toString()

        // ── Validate username ─────────────────────────────
        if (username.isEmpty()) {
            usernameLayout.error = "Please choose a username"
            return
        }
        if (username.length < 4) {
            usernameLayout.error = "Username must be at least 4 characters"
            return
        }
        if (username.length > 30) {
            usernameLayout.error = "Username must be under 30 characters"
            return
        }
        if (!username.matches(Regex("^[a-zA-Z0-9]+$"))) {
            usernameLayout.error = "Only letters and numbers allowed"
            return
        }

        // ── Validate password ─────────────────────────────
        if (password.length < 8) {
            passwordLayout.error = "Password must be at least 8 characters"
            return
        }
        if (!password.any { it.isUpperCase() }) {
            passwordLayout.error = "Add at least one uppercase letter"
            return
        }
        if (!password.any { it.isLowerCase() }) {
            passwordLayout.error = "Add at least one lowercase letter"
            return
        }
        if (!password.any { it.isDigit() }) {
            passwordLayout.error = "Add at least one digit"
            return
        }
        if (!password.any { "!@#\$%^&*()_+-=[]{}|;':\",./<>?`~".contains(it) }) {
            passwordLayout.error = "Add at least one special character"
            return
        }
        if (password != confirmPassword) {
            confirmPasswordLayout.error = "Passwords do not match"
            return
        }

        lifecycleScope.launch {
            // Check if username already exists
            if (repository.usernameExists(username) > 0) {
                runOnUiThread { usernameLayout.error = "Username already taken" }
                return@launch
            }

            // Insert new user – the repository now handles salting + hashing
            repository.insertUser(username, password)   // ⚠️ plain password, not hashed

            runOnUiThread {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                finish()
            }
        }
    }

    // ── Password strength meter logic ──────────────────
    private fun updateStrengthMeter(password: String) {
        if (password.isEmpty()) {
            strengthMeter.visibility = View.GONE
            return
        }
        strengthMeter.visibility = View.VISIBLE

        val score = calculateStrength(password)

        val active   = getColor(R.color.forest)
        val medium   = getColor(R.color.terracotta)
        val weak     = getColor(R.color.terracotta)
        val inactive = getColor(R.color.linen)

        when (score) {
            1 -> {
                strengthBar1.setBackgroundColor(weak);    strengthBar2.setBackgroundColor(inactive)
                strengthBar3.setBackgroundColor(inactive); strengthBar4.setBackgroundColor(inactive)
                strengthLabel.text = "Weak"; strengthLabel.setTextColor(weak)
            }
            2 -> {
                strengthBar1.setBackgroundColor(medium);  strengthBar2.setBackgroundColor(medium)
                strengthBar3.setBackgroundColor(inactive); strengthBar4.setBackgroundColor(inactive)
                strengthLabel.text = "Fair"; strengthLabel.setTextColor(medium)
            }
            3 -> {
                strengthBar1.setBackgroundColor(active);  strengthBar2.setBackgroundColor(active)
                strengthBar3.setBackgroundColor(active);  strengthBar4.setBackgroundColor(inactive)
                strengthLabel.text = "Good"; strengthLabel.setTextColor(active)
            }
            4 -> {
                strengthBar1.setBackgroundColor(active);  strengthBar2.setBackgroundColor(active)
                strengthBar3.setBackgroundColor(active);  strengthBar4.setBackgroundColor(active)
                strengthLabel.text = "Strong"; strengthLabel.setTextColor(active)
            }
        }
    }

    private fun calculateStrength(password: String): Int {
        var score = 0
        if (password.length >= 6)  score++
        if (password.length >= 10) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        return score.coerceAtLeast(1)
    }
}