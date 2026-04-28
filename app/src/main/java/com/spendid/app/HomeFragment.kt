package com.spendid.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment private constructor() : Fragment() {

    private var username: String = ""
    private lateinit var recentExpensesContainer: LinearLayout
    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var budgetRepository: BudgetRepository

    companion object {
        fun newInstance(username: String): HomeFragment {
            val fragment = HomeFragment()
            val args = Bundle()
            args.putString("USERNAME", username)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        username = arguments?.getString("USERNAME") ?: "User"
        expenseRepository = ExpenseRepository(DatabaseHelper(requireContext()))
        budgetRepository = BudgetRepository(DatabaseHelper(requireContext()))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set username in greeting
        val userNameText = view.findViewById<TextView>(R.id.userName)
        userNameText?.text = username

        // Get references to views
        recentExpensesContainer = view.findViewById(R.id.recentExpensesContainer)

        // Set greeting based on time of day
        val greetingText = view.findViewById<TextView>(R.id.greetingText)
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 0..11 -> "Good morning ✦"
            in 12..16 -> "Good afternoon ✦"
            else -> "Good evening ✦"
        }
        greetingText?.text = greeting

        // Set current month/year
        val currentDateText = view.findViewById<TextView>(R.id.currentDate)
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        currentDateText?.text = "📅 ${dateFormat.format(Date())}"

        // Handle View Report button click
        val btnViewReport = view.findViewById<MaterialButton>(R.id.btnViewReport)
        btnViewReport?.setOnClickListener {
            (activity as? MainActivity)?.navigateToReports()
        }

        // Handle Set Goals button click
        val btnSetGoals = view.findViewById<MaterialButton>(R.id.btnSetGoals)
        btnSetGoals?.setOnClickListener {
            val intent = Intent(requireContext(), SetBudgetActivity::class.java)
            startActivity(intent)
        }

        // Handle View All click
        val btnViewAll = view.findViewById<TextView>(R.id.btnViewAll)
        btnViewAll?.setOnClickListener {
            // Navigate to full expense list
            (activity as? MainActivity)?.let {
                // You can add navigation to ExpenseListActivity here
            }
        }

        // Handle notification bell click
        val btnNotifications = view.findViewById<TextView>(R.id.btnNotifications)
        btnNotifications?.setOnClickListener {
            // Show notifications - you can implement this
        }

        // Handle badges click
        val btnBadges = view.findViewById<TextView>(R.id.btnBadges)
        btnBadges?.setOnClickListener {
            // Navigate to badges screen - you can implement this
        }

        // Load recent expenses
        loadRecentExpenses()

        // Load budget data
        loadBudgetData()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to the fragment
        loadRecentExpenses()
        loadBudgetData()
    }

    private fun loadRecentExpenses() {
        lifecycleScope.launch {
            val expenses = expenseRepository.getAllExpenses()

            // Take only the first 5 most recent expenses
            val recentExpenses = expenses.take(5)

            if (recentExpenses.isEmpty()) {
                showEmptyState()
            } else {
                displayExpenses(recentExpenses)
            }
        }
    }

    private fun displayExpenses(expenses: List<Expense>) {
        recentExpensesContainer.removeAllViews()

        for (expense in expenses) {
            val expenseView = createExpenseItemView(expense)
            recentExpensesContainer.addView(expenseView)
        }
    }

    private fun createExpenseItemView(expense: Expense): View {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_recent_expense, recentExpensesContainer, false)

        val icon = view.findViewById<TextView>(R.id.expenseIcon)
        val title = view.findViewById<TextView>(R.id.expenseTitle)
        val meta = view.findViewById<TextView>(R.id.expenseMeta)
        val amount = view.findViewById<TextView>(R.id.expenseAmount)

        // Set icon based on category
        icon.text = getCategoryIcon(expense.category)

        // Set title (description)
        title.text = expense.description

        // Set meta (category and formatted date)
        val formattedDate = formatDate(expense.date)
        meta.text = "${expense.category} • $formattedDate"

        // Set amount
        amount.text = "R ${String.format("%.2f", expense.amount)}"

        // Add click listener to view expense details (optional)
        view.setOnClickListener {
            // You can add navigation to expense detail here
        }

        return view
    }

    private fun getCategoryIcon(category: String): String {
        return when (category.lowercase()) {
            "food" -> "🍔"
            "transport" -> "🚗"
            "health" -> "💊"
            "shopping" -> "🛒"
            "entertainment" -> "🎬"
            else -> "📦"
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            if (date != null) outputFormat.format(date) else dateString
        } catch (e: Exception) {
            dateString
        }
    }

    private fun showEmptyState() {
        recentExpensesContainer.removeAllViews()

        val emptyView = TextView(requireContext()).apply {
            text = "No expenses yet.\nTap + to add your first expense!"
            textSize = 14f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.brown_light))
            gravity = android.view.Gravity.CENTER
            setPadding(32, 48, 32, 48)
        }

        recentExpensesContainer.addView(emptyView)
    }

    private fun loadBudgetData() {
        lifecycleScope.launch {
            val expenses = expenseRepository.getAllExpenses()

            // Get current budget from database
            val currentBudget = budgetRepository.getCurrentBudget()
            val budgetAmount = currentBudget?.amount ?: 6000.0 // Default to 6000 if no budget set
            val minGoal = currentBudget?.minGoal
            val maxGoal = currentBudget?.maxGoal

            // Calculate total spent this month
            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)

            val monthlyTotal = expenses.filter { expense ->
                try {
                    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(expense.date)
                    if (date != null) {
                        val cal = Calendar.getInstance()
                        cal.time = date
                        cal.get(Calendar.MONTH) == currentMonth &&
                                cal.get(Calendar.YEAR) == currentYear
                    } else false
                } catch (e: Exception) {
                    false
                }
            }.sumOf { it.amount }

            // Update UI with budget data
            val spentAmount = view?.findViewById<TextView>(R.id.spentAmount)
            spentAmount?.text = "R ${String.format("%.2f", monthlyTotal)}"

            // Calculate percentage of budget
            val percentage = if (budgetAmount > 0) (monthlyTotal / budgetAmount) * 100 else 0.0
            val progressBar = view?.findViewById<android.widget.ProgressBar>(R.id.budgetProgress)
            progressBar?.progress = percentage.toInt()

            val percentageText = view?.findViewById<TextView>(R.id.maxGoalPercentage)
            percentageText?.text = "${percentage.toInt()}%"

            // Update min and max goal text
            val minGoalText = view?.findViewById<TextView>(R.id.minGoal)
            if (minGoal != null && minGoal > 0) {
                minGoalText?.text = "Min R${String.format("%.0f", minGoal)}"
            } else {
                minGoalText?.text = "Set a goal"
            }

            val maxGoalText = view?.findViewById<TextView>(R.id.maxGoal)
            if (maxGoal != null && maxGoal > 0) {
                maxGoalText?.text = "Max R${String.format("%.0f", maxGoal)}"
            } else {
                maxGoalText?.text = "Max R${String.format("%.0f", budgetAmount)}"
            }

            // Update spent amount text color based on budget usage
            when {
                percentage >= 100 -> spentAmount?.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                percentage >= 80 -> spentAmount?.setTextColor(ContextCompat.getColor(requireContext(), R.color.terracotta))
                else -> spentAmount?.setTextColor(ContextCompat.getColor(requireContext(), R.color.bark))
            }
        }
    }
}