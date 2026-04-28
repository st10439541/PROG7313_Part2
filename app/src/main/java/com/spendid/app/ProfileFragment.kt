package com.spendid.app

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class ProfileFragment private constructor() : Fragment() {

    private var username: String = ""
    private lateinit var tvSalary: TextView
    private lateinit var budgetRepository: BudgetRepository
    private var currentBudget: Budget? = null  // ← FIXED: removed lateinit, made nullable

    companion object {
        fun newInstance(username: String): ProfileFragment {
            val fragment = ProfileFragment()
            val args = Bundle()
            args.putString("USERNAME", username)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        username = arguments?.getString("USERNAME") ?: "User"
        budgetRepository = BudgetRepository(DatabaseHelper(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set username in profile header
        val profileName = view.findViewById<TextView>(R.id.profileName)
        profileName?.text = username

        // Generate a default email based on username
        val profileEmail = view.findViewById<TextView>(R.id.profileEmail)
        val email = "${username.lowercase().replace(" ", "")}@email.com"
        profileEmail?.text = email

        // Get salary TextView
        tvSalary = view.findViewById(R.id.tvSalary)

        // Load and display current budget/salary
        loadSalary()

        // Set click listener for salary row
        val salaryRow = view.findViewById<LinearLayout>(R.id.salaryRow)
        salaryRow?.setOnClickListener {
            showEditSalaryDialog()
        }

        // Handle Edit Profile click
        val btnEditProfile = view.findViewById<LinearLayout>(R.id.btnEditProfile)
        btnEditProfile?.setOnClickListener {
            showEditProfileDialog()
        }

        // Handle Theme click
        val btnTheme = view.findViewById<LinearLayout>(R.id.btnTheme)
        btnTheme?.setOnClickListener {
            showThemeDialog()
        }

        // Handle Logout button click
        val btnLogout = view.findViewById<MaterialButton>(R.id.btnLogout)
        btnLogout?.setOnClickListener {
            performLogout()
        }
    }

    private fun loadSalary() {
        lifecycleScope.launch {
            currentBudget = budgetRepository.getCurrentBudget()
            val salaryAmount = currentBudget?.amount ?: 0.0

            val formatter = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
            formatter.currency = Currency.getInstance("ZAR")
            tvSalary.text = formatter.format(salaryAmount)
        }
    }

    private fun showEditSalaryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_salary, null)
        val etSalary = dialogView.findViewById<EditText>(R.id.etSalary)

        // Pre-fill with current salary
        val currentAmount = currentBudget?.amount ?: 0.0
        etSalary.setText(currentAmount.toInt().toString())

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Monthly Salary")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val salaryText = etSalary.text.toString().trim()
                if (salaryText.isNotEmpty()) {
                    val newSalary = salaryText.toDoubleOrNull()
                    if (newSalary != null && newSalary > 0) {
                        saveSalary(newSalary)
                    } else {
                        Toast.makeText(requireContext(), "Please enter a valid salary amount", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveSalary(newSalary: Double) {
        lifecycleScope.launch {
            val calendar = Calendar.getInstance()
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)

            // Keep existing min/max goals if they exist
            val minGoal = currentBudget?.minGoal
            val maxGoal = currentBudget?.maxGoal

            val result = budgetRepository.saveBudget(newSalary, month, year, minGoal, maxGoal)

            if (result != -1L) {
                currentBudget = budgetRepository.getCurrentBudget()
                val formatter = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
                formatter.currency = Currency.getInstance("ZAR")
                tvSalary.text = formatter.format(newSalary)
                Toast.makeText(requireContext(), "Salary updated successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to update salary", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEditProfileDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)

        // Pre-fill with current values
        etName.setText(username)
        val profileEmail = view?.findViewById<TextView>(R.id.profileEmail)?.text.toString()
        etEmail.setText(profileEmail)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = etName.text.toString().trim()
                val newEmail = etEmail.text.toString().trim()

                if (newName.isNotEmpty()) {
                    // Update username in UI
                    val profileName = view?.findViewById<TextView>(R.id.profileName)
                    profileName?.text = newName
                    username = newName

                    // Update email
                    val profileEmailView = view?.findViewById<TextView>(R.id.profileEmail)
                    profileEmailView?.text = newEmail

                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showThemeDialog() {
        val themes = arrayOf("Forest Green (Default)", "Terracotta", "Sage", "Bark")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Theme")
            .setItems(themes) { _, which ->
                when (which) {
                    0 -> applyTheme(R.color.forest)
                    1 -> applyTheme(R.color.terracotta)
                    2 -> applyTheme(R.color.sage)
                    3 -> applyTheme(R.color.bark)
                }
            }
            .show()
    }

    private fun applyTheme(colorResId: Int) {
        // Store theme preference
        val prefs = requireContext().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putInt("theme_color", colorResId).apply()

        Toast.makeText(requireContext(), "Theme will apply on next restart", Toast.LENGTH_SHORT).show()
    }

    private fun performLogout() {
        val intent = Intent(activity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        Toast.makeText(activity, "Logged out successfully", Toast.LENGTH_SHORT).show()
    }
}