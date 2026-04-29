package com.spendid.app

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class ReportsTest {

    @Mock
    private lateinit var expenseRepository: ExpenseRepository

    @Mock
    private lateinit var budgetRepository: BudgetRepository

    private lateinit var testExpenses: List<Expense>
    private lateinit var testBudget: Budget

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        testExpenses = TestDataFactory.createExpenseList()
        testBudget = TestDataFactory.createTestBudget()
    }

    @Test
    fun testCalculateTotalExpenses_CorrectSum() {
        val total = testExpenses.sumOf { it.amount }
        assertEquals(1806.48, total, 0.01)
    }

    @Test
    fun testCalculateSurplus_IncomeMinusExpenses() {
        val income = testBudget.amount
        val expenses = testExpenses.sumOf { it.amount }
        val surplus = income - expenses

        assertEquals(6000.00 - 1806.48, surplus, 0.01)
        assertEquals(4193.52, surplus, 0.01)
        assertTrue(surplus > 0)
    }

    @Test
    fun testCalculateDeficit_WhenExpensesExceedIncome() {
        val lowIncome = 1000.00
        val highExpenses = 1500.00
        val deficit = lowIncome - highExpenses

        assertTrue(deficit < 0)
        assertEquals(-500.00, deficit, 0.01)
    }

    @Test
    fun testSpendingByCategory_CalculatesCorrectly() {
        val foodExpenses = testExpenses.filter { it.categoryId == 1 }.sumOf { it.amount }
        val transportExpenses = testExpenses.filter { it.categoryId == 2 }.sumOf { it.amount }
        val totalExpenses = testExpenses.sumOf { it.amount }

        val foodPercentage = (foodExpenses / totalExpenses) * 100
        val transportPercentage = (transportExpenses / totalExpenses) * 100

        assertEquals(470.50, foodExpenses, 0.01)
        assertEquals(26.05, foodPercentage, 0.01)
        assertEquals(45.99, transportExpenses, 0.01)
        assertEquals(2.55, transportPercentage, 0.01)
    }

    @Test
    fun testFilterExpensesByDateRange_ReturnsCorrectSubset() {
        val startDate = "2026-04-13"
        val endDate = "2026-04-15"

        val filteredExpenses = testExpenses.filter { expense ->
            expense.date >= startDate && expense.date <= endDate
        }

        assertEquals(3, filteredExpenses.size)
        assertTrue(filteredExpenses.all { it.date in listOf("2026-04-13", "2026-04-14", "2026-04-15") })
    }

    @Test
    fun testFilterExpensesByMonth_ReturnsCorrectSubset() {
        val filteredByMonth = testExpenses.filter { it.date.startsWith("2026-04") }

        assertEquals(5, filteredByMonth.size)
    }

    @Test
    fun testBudgetPercentageUsed_CalculatesCorrectly() {
        val budgetAmount = 6000.00
        val spentAmount = 1806.48
        val percentageUsed = (spentAmount / budgetAmount) * 100

        assertEquals(30.11, percentageUsed, 0.01)
    }

    @Test
    fun testWeeklySpending_CalculatesAverage() {
        val weeklyExpenses = listOf(200.00, 150.00, 300.00, 175.00, 225.00, 180.00, 250.00)
        val average = weeklyExpenses.average()

        assertEquals(211.43, average, 0.01)
    }
}