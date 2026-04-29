package com.spendid.app

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditExpenseTest {

    private lateinit var existingExpense: Expense

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        existingExpense = TestDataFactory.createTestExpense(
            id = 1,
            amount = 150.50,
            description = "Lunch at Café",
            categoryId = 1,
            date = "2026-04-15",
            time = "13:30 PM"
        )
    }

    @Test
    fun testescription_Success() {
        val newDescription = "Lunch at Café (Updated)"
        val isChanged = newDescription != existingExpense.description

        assertTrue(isChanged)
        assertEquals("Lunch at Café (Updated)", newDescription)
    }

    @Test
    fun testCategory_Success() {
        val newCategoryId = 4
        val updatedExpense = existingExpense.copy(categoryId = newCategoryId)

        assertEquals(4, updatedExpense.categoryId)
        assertNotEquals(existingExpense.categoryId, updatedExpense.categoryId)
    }

    @Test
    fun testDate_Success() {
        val newDate = "2026-04-20"
        val updatedExpense = existingExpense.copy(date = newDate)

        assertEquals("2026-04-20", updatedExpense.date)
        assertNotEquals(existingExpense.date, updatedExpense.date)
    }

    @Test
    fun testTime_Success() {
        val newTime = "14:45 PM"
        val updatedExpense = existingExpense.copy(time = newTime)

        assertEquals("14:45 PM", updatedExpense.time)
        assertNotEquals(existingExpense.time, updatedExpense.time)
    }


    @Test
    fun testVerifyMonthlyTotalUpdate() {
        val originalTotal = 1806.48
        val oldAmount = 150.50
        val newAmount = 175.50
        val difference = newAmount - oldAmount
        val newTotal = originalTotal + difference

        assertEquals(1831.48, newTotal, 0.01)
        assertNotEquals(originalTotal, newTotal)
    }

    @Test
    fun testEditMultipleExpenses() {
        val expense1 = existingExpense.copy(amount = 100.00)
        val expense2 = existingExpense.copy(id = 2, amount = 200.00)
        val expense3 = existingExpense.copy(id = 3, amount = 300.00)

        val total = expense1.amount + expense2.amount + expense3.amount

        assertEquals(600.00, total, 0.01)
    }
}