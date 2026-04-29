package com.spendid.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ReportsFragment : Fragment() {

    private lateinit var categoryBreakdownContainer: LinearLayout
    private lateinit var chartIncomeExpense: BarChart
    private lateinit var btnMonthly: MaterialButton
    private lateinit var btnWeekly: MaterialButton
    private lateinit var btnYearly: MaterialButton

    private val expenseRepository by lazy { ExpenseRepository(DatabaseHelper(requireContext())) }
    private val budgetRepository by lazy { BudgetRepository(DatabaseHelper(requireContext())) }
    private val categoryRepository by lazy { CategoryRepository(DatabaseHelper(requireContext())) }

    private var currentStartDate: String = ""
    private var currentEndDate: String = ""
    private var currentPeriodType: String = "monthly"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_reports, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        setupPeriodSelection(view)
        loadMonthly()
    }

    private fun setupViews(view: View) {
        categoryBreakdownContainer = view.findViewById(R.id.categoryBreakdown)
        chartIncomeExpense = view.findViewById(R.id.chartIncomeExpense)
        btnMonthly = view.findViewById(R.id.btnMonthly)
        btnWeekly = view.findViewById(R.id.btnWeekly)
        btnYearly = view.findViewById(R.id.btnYearly)

        val backButton = view.findViewById<MaterialButton>(R.id.btnBack)
        backButton?.visibility = View.GONE
    }

    private fun setupPeriodSelection(view: View) {
        btnMonthly.setOnClickListener {
            currentPeriodType = "monthly"
            updateButtonSelection(btnMonthly)
            loadMonthly()
        }

        btnWeekly.setOnClickListener {
            currentPeriodType = "weekly"
            updateButtonSelection(btnWeekly)
            loadWeekly()
        }

        btnYearly.setOnClickListener {
            currentPeriodType = "yearly"
            updateButtonSelection(btnYearly)
            loadYearly()
        }
    }

    private fun updateButtonSelection(selectedButton: MaterialButton) {
        val buttons = listOf(btnMonthly, btnWeekly, btnYearly)
        buttons.forEach { button ->
            button.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.white)
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.bark))
            button.strokeColor = ContextCompat.getColorStateList(requireContext(), R.color.linen)
            button.strokeWidth = 2
        }

        selectedButton.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.forest)
        selectedButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        selectedButton.strokeWidth = 0
    }

    private fun loadMonthly() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        currentStartDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        currentEndDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        loadReportData()
    }

    private fun loadWeekly() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        currentStartDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        currentEndDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        loadReportData()
    }

    private fun loadYearly() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_YEAR, 1)
        currentStartDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
        currentEndDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        loadReportData()
    }

    private fun loadReportData() {
        lifecycleScope.launch {
            val spendingByCategory = getSpendingByCategory(currentStartDate, currentEndDate)
            updateCategoryBreakdown(spendingByCategory)
            loadIncomeVsExpenses()
        }
    }

    private suspend fun getSpendingByCategory(startDate: String, endDate: String): List<CategorySpending> {
        val expenses = expenseRepository.getAllExpenses()
        val filteredExpenses = expenses.filter { expense ->
            expense.date >= startDate && expense.date <= endDate
        }

        val totalByCategory = mutableMapOf<Int, Double>()

        for (expense in filteredExpenses) {
            totalByCategory[expense.categoryId] = totalByCategory.getOrDefault(expense.categoryId, 0.0) + expense.amount
        }

        val grandTotal = totalByCategory.values.sum()
        val results = mutableListOf<CategorySpending>()

        for ((categoryId, total) in totalByCategory) {
            val category = categoryRepository.getCategoryById(categoryId)
            val percentage = if (grandTotal > 0) (total / grandTotal) * 100 else 0.0
            results.add(
                CategorySpending(
                    categoryName = category?.name ?: "Unknown",
                    categoryIcon = category?.icon ?: "📦",
                    totalAmount = total,
                    percentage = percentage
                )
            )
        }

        return results.sortedByDescending { it.totalAmount }
    }

    private fun loadIncomeVsExpenses() {
        lifecycleScope.launch {
            val expenses = expenseRepository.getAllExpenses()
            val filteredExpenses = expenses.filter { expense ->
                expense.date >= currentStartDate && expense.date <= currentEndDate
            }

            val totalExpenses = filteredExpenses.sumOf { it.amount }
            val currentBudget = budgetRepository.getCurrentBudget()
            val totalIncome = currentBudget?.amount ?: 0.0

            setupBarChart(totalIncome, totalExpenses)
            addIncomeExpenseSummary(totalIncome, totalExpenses)
        }
    }

    private fun setupBarChart(income: Double, expenses: Double) {
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, income.toFloat()))
        entries.add(BarEntry(1f, expenses.toFloat()))

        val dataSet = BarDataSet(entries, "Amount (R)")

        dataSet.setColors(
            ContextCompat.getColor(requireContext(), R.color.forest),
            ContextCompat.getColor(requireContext(), R.color.terracotta)
        )

        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.bark)
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getBarLabel(barEntry: BarEntry): String {
                return "R ${String.format("%.0f", barEntry.y)}"
            }
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.5f

        chartIncomeExpense.data = barData
        chartIncomeExpense.description.isEnabled = false
        chartIncomeExpense.legend.isEnabled = true
        chartIncomeExpense.setFitBars(true)

        val xAxis = chartIncomeExpense.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(arrayOf("Income", "Expenses"))
        xAxis.textSize = 12f
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.brown_light)

        chartIncomeExpense.axisLeft.setDrawGridLines(true)
        chartIncomeExpense.axisLeft.textSize = 10f
        chartIncomeExpense.axisLeft.textColor = ContextCompat.getColor(requireContext(), R.color.brown_light)
        chartIncomeExpense.axisRight.isEnabled = false

        chartIncomeExpense.animateY(1000)
        chartIncomeExpense.invalidate()
    }

    private fun addIncomeExpenseSummary(income: Double, expenses: Double) {
        val difference = income - expenses
        val differenceColor = if (difference >= 0) R.color.forest else R.color.red
        val differenceText = if (difference >= 0) "Surplus" else "Deficit"

        val summaryView = view?.findViewById<TextView>(R.id.tvIncomeExpenseSummary)
        if (summaryView != null) {
            summaryView.text = "$differenceText: R ${String.format("%.2f", Math.abs(difference))}"
            summaryView.setTextColor(ContextCompat.getColor(requireContext(), differenceColor))
        }
    }

    private fun updateCategoryBreakdown(categories: List<CategorySpending>) {
        categoryBreakdownContainer.removeAllViews()

        if (categories.isEmpty()) {
            val emptyView = TextView(requireContext()).apply {
                text = "No expenses found for this period"
                setTextColor(ContextCompat.getColor(requireContext(), R.color.brown_light))
                textSize = 13f
                setPadding(16, 40, 16, 40)
                gravity = android.view.Gravity.CENTER
            }
            categoryBreakdownContainer.addView(emptyView)
            return
        }

        for (category in categories) {
            val categoryView = createCategoryItemView(category)
            categoryBreakdownContainer.addView(categoryView)
        }
    }

    private fun createCategoryItemView(category: CategorySpending): View {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.item_category_breakdown, categoryBreakdownContainer, false)

        val iconText = view.findViewById<TextView>(R.id.categoryIcon)
        val nameText = view.findViewById<TextView>(R.id.categoryName)
        val amountText = view.findViewById<TextView>(R.id.categoryAmount)
        val percentageText = view.findViewById<TextView>(R.id.categoryPercentage)
        val progressBar = view.findViewById<ProgressBar>(R.id.categoryProgress)

        iconText.text = category.categoryIcon
        nameText.text = category.categoryName
        amountText.text = "R ${String.format("%.2f", category.totalAmount)}"
        percentageText.text = "${String.format("%.0f", category.percentage)}%"
        progressBar.progress = category.percentage.toInt()

        return view
    }

    data class CategorySpending(
        val categoryName: String,
        val categoryIcon: String,
        val totalAmount: Double,
        val percentage: Double
    )
}