package com.spendid.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Uses your existing dashboard screen
        setContentView(R.layout.activity_dashboard)
    }
}