package com.spendid.app

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*
import android.view.View
import android.view.ViewGroup

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var etAmount: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var btnDate: MaterialButton
    private lateinit var btnTime: MaterialButton
    private lateinit var btnSave: MaterialButton
    private lateinit var btnBack: MaterialButton
    private lateinit var btnAddCategory: MaterialButton

    private var selectedCategoryId: Int = 1
    private var selectedDate: String = ""
    private var selectedTime: String = ""

    private val repository by lazy { ExpenseRepository(DatabaseHelper(this)) }
    private val categoryRepository by lazy { CategoryRepository(DatabaseHelper(this)) }
    private var categories: List<Category> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        etAmount = findViewById(R.id.etAmount)
        etDescription = findViewById(R.id.etDescription)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        btnDate = findViewById(R.id.btnDate)
        btnTime = findViewById(R.id.btnTime)
        btnSave = findViewById(R.id.btnSave)
        btnBack = findViewById(R.id.btnBack)
        btnAddCategory = findViewById(R.id.btnAddCategory)

        loadCategories()
        setDefaultDateTime()
        setupDatePicker()
        setupTimePicker()
        setupSaveButton()
        setupBackButton()
        setupAddCategoryButton()
    }

    private fun loadCategories() {
        categories = categoryRepository.getAllCategories()

        // Create a custom adapter that shows icon + name
        val adapter = object : ArrayAdapter<Category>(
            this,
            android.R.layout.simple_spinner_item,
            categories
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: layoutInflater.inflate(
                    android.R.layout.simple_spinner_item, parent, false
                )
                val textView = view as android.widget.TextView
                val category = getItem(position)
                textView.text = "${category?.icon} ${category?.name}"
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: layoutInflater.inflate(
                    android.R.layout.simple_spinner_dropdown_item, parent, false
                )
                val textView = view as android.widget.TextView
                val category = getItem(position)
                textView.text = "${category?.icon} ${category?.name}"
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedCategoryId = categories[position].categoryId
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupAddCategoryButton() {
        btnAddCategory.setOnClickListener {
            showAddCategoryDialog()
        }
    }

    private fun showAddCategoryDialog() {
        val input = android.widget.EditText(this)
        input.hint = "Enter category name"

        AlertDialog.Builder(this)
            .setTitle("Add New Category")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    val result = categoryRepository.insertCategory(name)
                    if (result != -1L) {
                        Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show()
                        loadCategories() // Refresh spinner
                    } else {
                        Toast.makeText(this, "Category already exists", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setDefaultDateTime() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        selectedDate = dateFormat.format(calendar.time)
        selectedTime = timeFormat.format(calendar.time)
        val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        btnDate.text = displayDateFormat.format(calendar.time)
        btnTime.text = selectedTime
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
                selectedCategoryId,
                selectedDate,
                selectedTime,
                null
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