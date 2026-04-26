package com.spendid.app

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class AddExpenseActivity : AppCompatActivity() {

    private var imageUri: String? = null

    private val repository by lazy {
        ExpenseRepository(DatabaseHelper(this))
    }

    private lateinit var imageView: ImageView
    private lateinit var descriptionInput: TextInputEditText
    private lateinit var amountDisplay: TextView

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                imageUri = uri.toString()
                imageView.setImageURI(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        imageView = findViewById(R.id.receiptImageView)
        descriptionInput = findViewById(R.id.descriptionInput)
        amountDisplay = findViewById(R.id.amountDisplay)

        // Attach receipt photo
        findViewById<MaterialButton>(R.id.btnAttachReceipt).setOnClickListener {
            imagePicker.launch("image/*")
        }

        // Save expense
        findViewById<MaterialButton>(R.id.btnSave).setOnClickListener {

            val amount = 100.0 // Replace with real amount input later
            val description = descriptionInput.text.toString().trim()
            val category = "Food" // Replace with selected category
            val date = "2026-04-26" // Replace with selected date
            val time = "10:30 AM" // Replace with selected time

            if (description.isEmpty()) {
                Toast.makeText(this, "Please enter description", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val result = repository.insertExpense(
                amount,
                description,
                category,
                date,
                time,
                imageUri
            )

            if (result > 0) {
                Toast.makeText(this, "Expense saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to save expense", Toast.LENGTH_SHORT).show()
            }
        }
    }
}