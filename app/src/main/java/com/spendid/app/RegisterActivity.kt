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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var usernameLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var confirmPasswordLayout: TextInputLayout
    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText

    private lateinit var strengthMeter: View
    private lateinit var strengthBar1: View
    private lateinit var strengthBar2: View
    private lateinit var strengthBar3: View
    private lateinit var strengthBar4: View
    private lateinit var strengthLabel: android.widget.TextView

    private val repository by lazy { UserRepository(DatabaseHelper(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        usernameLayout = findViewById(R.id.usernameLayout)
        passwordLayout = findViewById(R.id.passwordLayout)
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout)

        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)

        strengthMeter = findViewById(R.id.strengthMeter)
        strengthBar1 = findViewById(R.id.strengthBar1)
        strengthBar2 = findViewById(R.id.strengthBar2)
        strengthBar3 = findViewById(R.id.strengthBar3)
        strengthBar4 = findViewById(R.id.strengthBar4)
        strengthLabel = findViewById(R.id.strengthLabel)

        passwordInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateStrengthMeter(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        findViewById<MaterialButton>(R.id.btnBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<MaterialButton>(R.id.btnGoToLogin).setOnClickListener {
            goToLogin()
        }

        findViewById<MaterialButton>(R.id.btnCreateAccount).setOnClickListener {
            attemptRegister()
        }
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun attemptRegister() {

        usernameLayout.error = null
        passwordLayout.error = null
        confirmPasswordLayout.error = null

        val username = usernameInput.text.toString().trim()
        val password = passwordInput.text.toString()
        val confirmPassword = confirmPasswordInput.text.toString()

        if (username.isEmpty()) {
            usernameLayout.error = "Please choose a username"
            return
        }

        if (password.length < 6) {
            passwordLayout.error = "Password too short"
            return
        }

        if (password != confirmPassword) {
            confirmPasswordLayout.error = "Passwords do not match"
            return
        }

        lifecycleScope.launch {

            val success = withContext(Dispatchers.IO) {
                try {
                    val exists = repository.usernameExists(username)
                    if (exists > 0) return@withContext false

                    val result = repository.insertUser(username, password)

                    // 🔥 IMPORTANT DEBUG
                    println("INSERT RESULT = $result")

                    result != -1L
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }

            if (success) {

                println("REGISTER SUCCESS: $username")

                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)

                finish() // 👈 THIS IS ENOUGH
                finish()
            }else {
                usernameLayout.error = "Registration failed (check logs)"
            }
        }
    }

    private fun updateStrengthMeter(password: String) {
        if (password.isEmpty()) {
            strengthMeter.visibility = View.GONE
            return
        }

        strengthMeter.visibility = View.VISIBLE

        val score = calculateStrength(password)

        val red = getColor(R.color.red)
        val orange = getColor(R.color.terracotta)
        val green = getColor(R.color.forest)
        val gray = getColor(R.color.linen)

        val bars = listOf(
            strengthBar1,
            strengthBar2,
            strengthBar3,
            strengthBar4
        )

        // reset all
        bars.forEach { it.setBackgroundColor(gray) }

        when (score) {

            1 -> {
                bars[0].setBackgroundColor(red)
                strengthLabel.text = "Weak"
                strengthLabel.setTextColor(red)
            }

            2 -> {
                bars[0].setBackgroundColor(red)
                bars[1].setBackgroundColor(orange)
                strengthLabel.text = "Fair"
                strengthLabel.setTextColor(orange)
            }

            3 -> {
                bars[0].setBackgroundColor(red)
                bars[1].setBackgroundColor(orange)
                bars[2].setBackgroundColor(green)
                strengthLabel.text = "Good"
                strengthLabel.setTextColor(green)
            }

            4 -> {
                bars[0].setBackgroundColor(red)
                bars[1].setBackgroundColor(orange)
                bars[2].setBackgroundColor(green)
                bars[3].setBackgroundColor(green)
                strengthLabel.text = "Strong"
                strengthLabel.setTextColor(green)
            }
        }
    }
    private fun calculateStrength(password: String): Int {
        var score = 0

        if (password.length >= 1) score++        // weak baseline (DON'T skip this)
        if (password.length >= 6) score++
        if (password.length >= 10) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        return score.coerceAtMost(4)
    }
}