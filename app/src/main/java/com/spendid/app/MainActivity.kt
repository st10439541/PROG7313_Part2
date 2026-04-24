package com.spendid.app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)   // ← now the real dashboard

        val username = intent.getStringExtra("USERNAME") ?: "there"
        findViewById<TextView>(R.id.userName)?.text = username
    }
}