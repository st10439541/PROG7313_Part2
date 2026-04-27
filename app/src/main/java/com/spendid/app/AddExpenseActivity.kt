package com.spendid.app

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddExpenseActivity : AppCompatActivity() {

    private var imageUri: String? = null

    private val repository by lazy {
        ExpenseRepository(DatabaseHelper(this))
    }

    // Views
    private lateinit var etAmount: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var chipGroupCategory: ChipGroup
    private lateinit var btnDate: MaterialButton
    private lateinit var btnTime: MaterialButton
    private lateinit var ivReceiptPreview: ImageView
    private lateinit var btnAttachReceipt: MaterialButton
    private lateinit var btnSave: MaterialButton
    private lateinit var btnBack: MaterialButton

    // Data holders
    private var selectedCategory: String = "Food"
    private var selectedDate: String = ""
    private var selectedTime: String = ""

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri.toString()
            ivReceiptPreview.setImageURI(uri)
            ivReceiptPreview.visibility = ImageView.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        // Bind views
        etAmount = findViewById(R.id.etAmount)
        etDescription = findViewById(R.id.etDescription)
        chipGroupCategory = findViewById(R.id.chipGroupCategory)
        btnDate = findViewById(R.id.btnDate)
        btnTime = findViewById(R.id.btnTime)
        ivReceiptPreview = findViewById(R.id.ivReceiptPreview)
        btnAttachReceipt = findViewById(R.id.btnAttachReceipt)
        btnSave = findViewById(R.id.btnSave)
        btnBack = findViewById(R.id.btnBack)

        // Set default date & time
        setDefaultDateTime()

        // Setup listeners
        setupCategorySelection()
        setupDatePicker()
        setupTimePicker()
        setupImagePicker()
        setupSaveButton()
        setupBackButton()
    }

    private fun setDefaultDateTime() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        selectedDate = dateFormat.format(calendar.time)
        selectedTime = timeFormat.format(calendar.time)
        // Display format for buttons
        val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        btnDate.text = displayDateFormat.format(calendar.time)
        btnTime.text = selectedTime
    }

    private fun setupCategorySelection() {
        chipGroupCategory.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds[0])
                // Extract category name after emoji: e.g., "🍔 Food" -> "Food"
                selectedCategory = chip.text.toString().substringAfter(' ').trim()
            }
        }
        // Pre-select first chip (index 0) as default
        if (chipGroupCategory.childCount > 0) {
            (chipGroupCategory.getChildAt(0) as? Chip)?.isChecked = true
        }
    }

    private fun setupDatePicker() {
        btnDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    selectedDate = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, dayOfMonth)
                    val displayCalendar = Calendar.getInstance()
                    displayCalendar.set(year, month, dayOfMonth)
                    val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    btnDate.text = displayFormat.format(displayCalendar.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupTimePicker() {
        btnTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    val amPm = if (hourOfDay < 12) "AM" else "PM"
                    val hour = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                    selectedTime = String.format(Locale.US, "%02d:%02d %s", hour, minute, amPm)
                    btnTime.text = selectedTime
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        }
    }

    private fun setupImagePicker() {
        btnAttachReceipt.setOnClickListener {
            imagePicker.launch("image/*")
        }
    }

    private fun setupSaveButton() {
        btnSave.setOnClickListener {
            val amountText = etAmount.text.toString().trim()
            val amount = amountText.toDoubleOrNull()
            val description = etDescription.text.toString().trim()

            if (amount == null || amount <= 0.0) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (description.isEmpty()) {
                Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val result = repository.insertExpense(
                amount,
                description,
                selectedCategory,
                selectedDate,
                selectedTime,
                imageUri   // matches your repository parameter name
            )

            if (result > 0) {
                Toast.makeText(this, "Expense saved successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Failed to save expense", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBackButton() {
        btnBack.setOnClickListener {
            finish()
        }
    }
}