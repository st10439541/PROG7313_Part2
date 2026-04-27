package com.spendid.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameLayout: TextInputLayout
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText

    private val repository by lazy { UserRepository(DatabaseHelper(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameLayout = findViewById(R.id.usernameLayout)
        passwordLayout = findViewById(R.id.passwordLayout)
        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)

        findViewById<MaterialButton>(R.id.btnGoToRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        findViewById<MaterialButton>(R.id.btnLogin).setOnClickListener {
            attemptLogin()
        }
    }

    private fun attemptLogin() {

        usernameLayout.error = null
        passwordLayout.error = null

        val username = usernameInput.text.toString().trim()
        val password = passwordInput.text.toString()

        if (username.isEmpty()) {
            usernameLayout.error = "Please enter your username"
            return
        }

        if (password.isEmpty()) {
            passwordLayout.error = "Please enter your password"
            return
        }

        lifecycleScope.launch {

            val user = repository.login(username, password)

            if (user != null) {

                println("LOGIN SUCCESS: ${user.username}")

                val intent = if (user.tutorialCompleted) {
                    Intent(this@LoginActivity, MainActivity::class.java)
                } else {
                    Intent(this@LoginActivity, TutorialActivity::class.java)
                }

                intent.putExtra("USER_ID", user.id)
                intent.putExtra("USERNAME", user.username)

                startActivity(intent)
                finish()

            } else {
                runOnUiThread {
                    passwordLayout.error = "Incorrect username or password"
                }
            }
        }
    }
}