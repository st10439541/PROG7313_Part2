package com.spendid.app

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var etAmount: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var spinnerCategory: android.widget.Spinner
    private lateinit var btnDate: MaterialButton
    private lateinit var btnTime: MaterialButton
    private lateinit var btnSave: MaterialButton
    private lateinit var btnBack: MaterialButton
    private lateinit var btnAddCategory: MaterialButton
    private lateinit var btnAttachReceipt: MaterialButton
    private lateinit var ivReceiptPreview: ImageView

    private var selectedCategoryId: Int = 1
    private var selectedDate: String = ""
    private var selectedTime: String = ""
    private var selectedImageUri: Uri? = null
    private var cameraImageUri: Uri? = null  // URI for the photo taken by camera

    private val repository by lazy { ExpenseRepository(DatabaseHelper(this)) }
    private val categoryRepository by lazy { CategoryRepository(DatabaseHelper(this)) }
    private var categories: List<Category> = listOf()

    // ── Image Pickers ──────────────────────────────────────────────────────────

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                ivReceiptPreview.setImageURI(it)
                ivReceiptPreview.visibility = View.VISIBLE
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                cameraImageUri?.let {
                    selectedImageUri = it
                    ivReceiptPreview.setImageURI(it)
                    ivReceiptPreview.visibility = View.VISIBLE
                }
            }
        }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) launchCamera()
            else Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }

    // ──────────────────────────────────────────────────────────────────────────

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
        btnAttachReceipt = findViewById(R.id.btnAttachReceipt)
        ivReceiptPreview = findViewById(R.id.ivReceiptPreview)

        loadCategories()
        setDefaultDateTime()
        setupDatePicker()
        setupTimePicker()
        setupSaveButton()
        setupBackButton()
        setupAddCategoryButton()
        setupReceiptButton()
    }

    // ── Receipt / Camera ──────────────────────────────────────────────────────

    private fun setupReceiptButton() {
        btnAttachReceipt.setOnClickListener {
            showImageSourceDialog()
        }
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Remove Photo")
        AlertDialog.Builder(this)
            .setTitle("Attach Receipt")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> checkCameraPermissionAndLaunch()
                    1 -> galleryLauncher.launch("image/*")
                    2 -> {
                        selectedImageUri = null
                        ivReceiptPreview.setImageURI(null)
                        ivReceiptPreview.visibility = View.GONE
                    }
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndLaunch() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED -> launchCamera()
            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val photoFile = File.createTempFile(
            "receipt_${System.currentTimeMillis()}",
            ".jpg",
            cacheDir
        )
        val uri = FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            photoFile
        )
        cameraImageUri = uri
        cameraLauncher.launch(uri)  // ← non-null Uri
    }

    // ──────────────────────────────────────────────────────────────────────────

    private fun loadCategories() {
        categories = categoryRepository.getAllCategories()

        val adapter = object : ArrayAdapter<Category>(
            this,
            android.R.layout.simple_spinner_item,
            categories
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: layoutInflater.inflate(
                    android.R.layout.simple_spinner_item, parent, false
                )
                val tv = view as android.widget.TextView
                tv.text = "${getItem(position)?.icon} ${getItem(position)?.name}"
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = convertView ?: layoutInflater.inflate(
                    android.R.layout.simple_spinner_dropdown_item, parent, false
                )
                val tv = view as android.widget.TextView
                tv.text = "${getItem(position)?.icon} ${getItem(position)?.name}"
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
        btnAddCategory.setOnClickListener { showAddCategoryDialog() }
    }

    private fun showAddCategoryDialog() {
        val input = android.widget.EditText(this).apply { hint = "Enter category name" }
        AlertDialog.Builder(this)
            .setTitle("Add New Category")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    val result = categoryRepository.insertCategory(name)
                    if (result != -1L) {
                        Toast.makeText(this, "Category added", Toast.LENGTH_SHORT).show()
                        loadCategories()
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
        btnDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(calendar.time)
        btnTime.text = selectedTime
    }

    private fun setupDatePicker() {
        btnDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                selectedDate = String.format(Locale.US, "%d-%02d-%02d", year, month + 1, day)
                val cal = Calendar.getInstance().apply { set(year, month, day) }
                btnDate.text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(cal.time)
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun setupTimePicker() {
        btnTime.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(this, { _, hour, minute ->
                val amPm = if (hour < 12) "AM" else "PM"
                val h = if (hour % 12 == 0) 12 else hour % 12
                selectedTime = String.format(Locale.US, "%02d:%02d %s", h, minute, amPm)
                btnTime.text = selectedTime
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show()
        }
    }

    private fun setupSaveButton() {
        btnSave.setOnClickListener {
            val amount = etAmount.text.toString().trim().toDoubleOrNull()
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
                selectedImageUri?.toString()  //passes the URI
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
        btnBack.setOnClickListener { finish() }
    }
}