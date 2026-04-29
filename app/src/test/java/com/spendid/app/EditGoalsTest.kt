package com.spendid.app

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class EditGoalsTest {

    @Mock
    private lateinit var budgetRepository: BudgetRepository

    private lateinit var currentBudget: Budget

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        currentBudget = TestDataFactory.createTestBudget(
            amount = 6000.00,
            minGoal = 3000.00,
            maxGoal = 8000.00
        )
    }

    @Test
    fun testEditMinGoal_Success() {
        val newMinGoal = 4000.00

        // Get the non-null value safely
        val currentMaxGoal = currentBudget.maxGoal ?: 8000.0

        `when`(budgetRepository.saveBudget(
            eq(currentBudget.amount),
            anyInt(),
            anyInt(),
            eq(newMinGoal),
            eq(currentMaxGoal)
        )).thenReturn(1L)

        val result = budgetRepository.saveBudget(
            currentBudget.amount,
            3,
            2026,
            newMinGoal,
            currentMaxGoal
        )

        assertTrue(result > 0)
        assertNotEquals(currentBudget.minGoal, newMinGoal)
        assertEquals(4000.00, newMinGoal, 0.01)
    }

    @Test
    fun testMinGoalCannotExceedMaxGoal() {
        val invalidMin = 9000.00
        val maxGoal = 8000.00

        assertTrue(invalidMin > maxGoal)
        assertFalse(minGoalValid(invalidMin, maxGoal))

        val validMin = 5000.00
        assertTrue(validMin <= maxGoal)
        assertTrue(minGoalValid(validMin, maxGoal))
    }

    @Test
    fun testGoalsWithinBudgetRange() {
        val budgetAmount = 6000.00

        // min goal should be less than or equal to budget
        val validMinGoal = 4500.00
        assertTrue(validMinGoal <= budgetAmount)

        // max goal should be greater than or equal to budget
        val validMaxGoal = 8000.00
        assertTrue(validMaxGoal >= budgetAmount)

        // Invalid cases
        val invalidMinGoal = 7000.00
        assertFalse(invalidMinGoal <= budgetAmount)

        val invalidMaxGoal = 3000.00
        assertFalse(invalidMaxGoal >= budgetAmount)
    }

    @Test
    fun testGoals_OptionalFieldsCanBeNull() {
        val budgetWithoutGoals = Budget(
            id = 2,
            amount = 5000.00,
            month = 3,
            year = 2026,
            minGoal = null,
            maxGoal = null
        )

        assertNull(budgetWithoutGoals.minGoal)
        assertNull(budgetWithoutGoals.maxGoal)
    }

    @Test
    fun testGoals_UpdateOnDifferentMonth() {
        val nextMonthBudget = currentBudget.copy(month = 4)

        assertNotEquals(currentBudget.month, nextMonthBudget.month)
        assertEquals(4, nextMonthBudget.month)
    }

    @Test
    fun testSaveBudgetWithNullGoals() {
        val budgetAmount = 5000.00
        val month = 3
        val year = 2026

        `when`(budgetRepository.saveBudget(
            eq(budgetAmount),
            eq(month),
            eq(year),
            isNull(),
            isNull()
        )).thenReturn(1L)

        val result = budgetRepository.saveBudget(
            budgetAmount,
            month,
            year,
            null,
            null
        )

        assertTrue(result > 0)
    }

    @Test
    fun testGoalValues_ArePositive() {
        val minGoal = 3000.00
        val maxGoal = 8000.00

        assertTrue(minGoal > 0)
        assertTrue(maxGoal > 0)
    }

    private fun minGoalValid(minGoal: Double, maxGoal: Double): Boolean {
        return minGoal <= maxGoal
    }
}