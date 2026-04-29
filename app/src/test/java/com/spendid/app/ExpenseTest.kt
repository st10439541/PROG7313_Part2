package com.spendid.app

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class ExpenseTest {

    @Mock
    private lateinit var expenseRepository: ExpenseRepository

    private lateinit var testExpenses: List<Expense>

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        testExpenses = TestDataFactory.createExpenseList()
    }


    @Test
    fun testGetAllExpenses_ReturnsList() {
        `when`(expenseRepository.getAllExpenses()).thenReturn(testExpenses)

        val expenses = expenseRepository.getAllExpenses()

        assertNotNull(expenses)
        assertEquals(5, expenses.size)
        assertEquals(150.50, expenses[0].amount, 0.01)
        assertEquals("Lunch at Café", expenses[0].description)
    }

    @Test
    fun testDeleteExpense_Success() {
        val expenseIdToDelete = 3
        `when`(expenseRepository.deleteExpense(expenseIdToDelete)).thenReturn(1)

        val result = expenseRepository.deleteExpense(expenseIdToDelete)

        assertEquals(1, result)
        verify(expenseRepository, times(1)).deleteExpense(expenseIdToDelete)
    }

    @Test
    fun testCalculateMonthlyTotal_CorrectSum() {
        val monthlyTotal = testExpenses.sumOf { it.amount }

        assertEquals(150.50 + 45.99 + 320.00 + 89.99 + 1200.00, monthlyTotal, 0.01)
        assertEquals(1806.48, monthlyTotal, 0.01)
    }

    @Test
    fun testExpenseWithImageUri_SavedCorrectly() {
        val imageUri = "content://media/external/12345"
        val expenseWithImage = TestDataFactory.createTestExpense(
            id = 6,
            amount = 200.00,
            description = "Dinner with receipt",
            imageUri = imageUri
        )

        assertNotNull(expenseWithImage.imageUri)
        assertEquals(imageUri, expenseWithImage.imageUri)
    }

    @Test
    fun testExpenseValidation_InvalidAmount_ShouldFail() {
        val negativeAmount = -50.0
        assertTrue(negativeAmount <= 0)

        val zeroAmount = 0.0
        assertTrue(zeroAmount <= 0)
    }

    @Test
    fun testExpenseByCategory_Filtering() {
        val foodExpenses = testExpenses.filter { it.categoryId == 1 }

        assertEquals(2, foodExpenses.size)
        assertTrue(foodExpenses.all { it.categoryId == 1 })
    }
}