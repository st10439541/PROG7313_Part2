package com.spendid.app

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import java.util.Calendar

class AddExpenseFragment : Fragment() {

    private lateinit var etTitle: EditText
    private lateinit var etAmount: EditText
    private lateinit var etDescription: EditText
    private lateinit var etDate: EditText
    private lateinit var etStartTime: EditText
    private lateinit var etEndTime: EditText
    private lateinit var etImagePath: EditText
    private lateinit var spCategory: Spinner   // ← now a Spinner
    private lateinit var db: DatabaseHelper

    // ---------- Image picker ----------
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            etImagePath.setText(it.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_add_expense, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind views
        etTitle = view.findViewById(R.id.etTitle)
        etAmount = view.findViewById(R.id.etAmount)
        etDescription = view.findViewById(R.id.etDescription)
        etDate = view.findViewById(R.id.etDate)
        etStartTime = view.findViewById(R.id.etStartTime)
        etEndTime = view.findViewById(R.id.etEndTime)
        etImagePath = view.findViewById(R.id.etImagePath)
        spCategory = view.findViewById(R.id.spCategory)

        // Back button
        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Database
        db = DatabaseHelper(requireContext())

        // ---------- Category dropdown (Spinner – always visible) ----------
        val categories = listOf(
            "Technology", "Business", "Sports", "Entertainment",
            "Health", "Education", "Lifestyle"
        )
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategory.adapter = adapter

        // ---------- Date picker ----------
        etDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    etDate.setText(date)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // ---------- Start time picker ----------
        etStartTime.setOnClickListener {
            showTimePicker { time -> etStartTime.setText(time) }
        }

        // ---------- End time picker ----------
        etEndTime.setOnClickListener {
            showTimePicker { time -> etEndTime.setText(time) }
        }

        // ---------- Image picker ----------
        etImagePath.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // ---------- Save button ----------
        view.findViewById<View>(R.id.btnSaveExpense).setOnClickListener {
            saveExpense()
        }
    }

    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val time = String.format("%02d:%02d", hourOfDay, minute)
                onTimeSelected(time)
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun saveExpense() {
        val title = etTitle.text.toString().trim()
        val amountText = etAmount.text.toString().trim()
        val amount = amountText.toDoubleOrNull() ?: 0.0
        val category = spCategory.selectedItem.toString()   // Spinner reading
        val description = etDescription.text.toString().trim()
        val imagePath = etImagePath.text.toString().trim()
        val date = etDate.text.toString().trim()
        val startTime = etStartTime.text.toString().trim().padStart(5, '0')
        val endTime = etEndTime.text.toString().trim().padStart(5, '0')

        if (title.isEmpty() || amountText.isEmpty() || date.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val expense = Expense(
            id = 0,
            title = title,
            amount = amount,
            category = category,
            description = description,
            date = date,
            startTime = startTime,
            endTime = endTime,
            imagePath = imagePath
        )

        val result = db.insertExpense(expense)

        if (result == -1L) {
            Toast.makeText(requireContext(), "Failed to save", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Saved successfully", Toast.LENGTH_SHORT).show()
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
}