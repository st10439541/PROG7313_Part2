package com.spendid.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var username: String = ""

    private lateinit var recentExpensesContainer: LinearLayout
    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var budgetRepository: BudgetRepository
    private lateinit var categoryRepository: CategoryRepository

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

        val dbHelper = DatabaseHelper(requireContext())

        expenseRepository = ExpenseRepository(dbHelper)
        budgetRepository = BudgetRepository(dbHelper)
        categoryRepository = CategoryRepository(dbHelper)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.activity_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userNameText = view.findViewById<TextView>(R.id.userName)
        val greetingText = view.findViewById<TextView>(R.id.greetingText)
        val currentDateText = view.findViewById<TextView>(R.id.currentDate)

        recentExpensesContainer = view.findViewById(R.id.recentExpensesContainer)

        userNameText.text = username

        // Greeting based on time
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (hour) {
            in 0..11 -> "Good morning ✦"
            in 12..16 -> "Good afternoon ✦"
            else -> "Good evening ✦"
        }
        greetingText.text = greeting

        // Current month/year
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        currentDateText.text = "📅 ${dateFormat.format(Date())}"

        // View report button
        val btnViewReport = view.findViewById<MaterialButton>(R.id.btnViewReport)
        btnViewReport.setOnClickListener {
            (activity as? MainActivity)?.navigateToReports()
        }

        // Set goals button
        val btnSetGoals = view.findViewById<MaterialButton>(R.id.btnSetGoals)
        btnSetGoals.setOnClickListener {
            val intent = Intent(requireContext(), SetBudgetActivity::class.java)
            startActivity(intent)
        }

        // View all expenses
        val btnViewAll = view.findViewById<TextView>(R.id.btnViewAll)

        btnViewAll.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.contentContainer, ExpensesFragment())
                .addToBackStack(null)
                .commit()
        }

        loadRecentExpenses()
        loadBudgetData()
    }

    override fun onResume() {
        super.onResume()
        loadRecentExpenses()
        loadBudgetData()
    }

    private fun loadRecentExpenses() {
        lifecycleScope.launch {
            val expenses = expenseRepository.getAllExpenses()
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

        expenses.forEach { expense ->
            val expenseView = createExpenseItemView(expense)
            recentExpensesContainer.addView(expenseView)
        }
    }

    private fun createExpenseItemView(expense: Expense): View {
        val inflater = LayoutInflater.from(requireContext())

        val view = inflater.inflate(
            R.layout.item_recent_expense,
            recentExpensesContainer,
            false
        )

        // Updated IDs from your XML
        val imgExpense = view.findViewById<ImageView>(R.id.imgExpense)
        val title = view.findViewById<TextView>(R.id.expenseTitle)
        val meta = view.findViewById<TextView>(R.id.expenseMeta)
        val amount = view.findViewById<TextView>(R.id.expenseAmount)
        val dateTime = view.findViewById<TextView>(R.id.tvDateTime)
        val deleteBtn = view.findViewById<ImageView>(R.id.deleteButton)

        val category = categoryRepository.getCategoryById(expense.categoryId)
        val categoryName = category?.name ?: "Unknown"

        title.text = expense.description
        meta.text = "$categoryName • ${formatDate(expense.date)}"
        amount.text = "R ${String.format("%.2f", expense.amount)}"
        dateTime.text = "${expense.date} | ${expense.time}"

        // Load receipt image if available
        if (!expense.imageUri.isNullOrEmpty()) {
            try {
                imgExpense.setImageURI(Uri.parse(expense.imageUri))
            } catch (e: Exception) {
                imgExpense.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } else {
            imgExpense.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // Delete expense
        deleteBtn.setOnClickListener {
            expenseRepository.deleteExpense(expense.id)

            loadRecentExpenses()
            loadBudgetData()
        }

        return view
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            )

            val outputFormat = SimpleDateFormat(
                "MMM dd",
                Locale.getDefault()
            )

            val date = inputFormat.parse(dateString)

            if (date != null) {
                outputFormat.format(date)
            } else {
                dateString
            }

        } catch (e: Exception) {
            dateString
        }
    }

    private fun showEmptyState() {
        recentExpensesContainer.removeAllViews()

        val emptyView = TextView(requireContext()).apply {
            text = "No expenses yet.\nTap + to add your first expense!"
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(32, 48, 32, 48)

            setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.brown_light
                )
            )
        }

        recentExpensesContainer.addView(emptyView)
    }

    private fun loadBudgetData() {
        lifecycleScope.launch {

            val expenses = expenseRepository.getAllExpenses()
            val currentBudget = budgetRepository.getCurrentBudget()

            val budgetAmount = currentBudget?.amount ?: 6000.0

            val calendar = Calendar.getInstance()
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentYear = calendar.get(Calendar.YEAR)

            val monthlyTotal = expenses.filter { expense ->
                try {
                    val date = SimpleDateFormat(
                        "yyyy-MM-dd",
                        Locale.getDefault()
                    ).parse(expense.date)

                    if (date != null) {
                        val cal = Calendar.getInstance()
                        cal.time = date

                        cal.get(Calendar.MONTH) == currentMonth &&
                                cal.get(Calendar.YEAR) == currentYear
                    } else {
                        false
                    }

                } catch (e: Exception) {
                    false
                }
            }.sumOf { it.amount }

            val spentAmount = view?.findViewById<TextView>(R.id.spentAmount)
            val progressBar = view?.findViewById<ProgressBar>(R.id.budgetProgress)
            val percentageText = view?.findViewById<TextView>(R.id.maxGoalPercentage)
            val minGoalText = view?.findViewById<TextView>(R.id.minGoal)
            val maxGoalText = view?.findViewById<TextView>(R.id.maxGoal)

            spentAmount?.text = "R ${String.format("%.2f", monthlyTotal)}"

            val percentage =
                if (budgetAmount > 0)
                    (monthlyTotal / budgetAmount) * 100
                else
                    0.0

            progressBar?.progress = percentage.toInt()
            percentageText?.text = "${percentage.toInt()}%"

            val minGoal = currentBudget?.minGoal
            val maxGoal = currentBudget?.maxGoal

            minGoalText?.text =
                if (minGoal != null && minGoal > 0) {
                    "Min R${String.format("%.0f", minGoal)}"
                } else {
                    "Set a goal"
                }

            maxGoalText?.text =
                if (maxGoal != null && maxGoal > 0) {
                    "Max R${String.format("%.0f", maxGoal)}"
                } else {
                    "Max R${String.format("%.0f", budgetAmount)}"
                }

            when {
                percentage >= 100 -> {
                    spentAmount?.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.red
                        )
                    )
                }

                percentage >= 80 -> {
                    spentAmount?.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.terracotta
                        )
                    )
                }

                else -> {
                    spentAmount?.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.bark
                        )
                    )
                }
            }
        }
    }
}