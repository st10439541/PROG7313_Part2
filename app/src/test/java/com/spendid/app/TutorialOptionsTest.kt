package com.spendid.app
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class TutorialOptionsTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }


    @Test
    fun testTutorialState_PersistsAfterCompletion() {
        val userId = 1
        val completedUser = TestDataFactory.createTestUser(
            id = userId,
            tutorialCompleted = true,
            financialGoal = "Save more money",
            spendingHabit = "Mostly in control"
        )

        `when`(userRepository.getUserById(userId)).thenReturn(completedUser)

        val user = userRepository.getUserById(userId)

        assertNotNull(user)
        assertTrue(user?.tutorialCompleted == true)
        assertEquals("Save more money", user?.financialGoal)
        assertEquals("Mostly in control", user?.spendingHabit)
    }

    @Test
    fun testTutorialOptions_AllPreferencesSaved() {
        val testOptions = TutorialPreferences(
            financialGoal = "Reach a financial goal",
            spendingHabit = "Needs improvement",
            budgetAlerts = false,
            dailyReminder = true,
            badgeNotifications = true,
            darkMode = true
        )

        assertEquals("Reach a financial goal", testOptions.financialGoal)
        assertEquals("Needs improvement", testOptions.spendingHabit)
        assertFalse(testOptions.budgetAlerts)
        assertTrue(testOptions.dailyReminder)
        assertTrue(testOptions.badgeNotifications)
        assertTrue(testOptions.darkMode)
    }

    @Test
    fun testTutorialPreferences_DefaultValues() {
        val defaultOptions = TutorialPreferences()

        assertEquals("", defaultOptions.financialGoal)
        assertEquals("", defaultOptions.spendingHabit)
        assertTrue(defaultOptions.budgetAlerts)
        assertTrue(defaultOptions.dailyReminder)
        assertFalse(defaultOptions.badgeNotifications)
        assertFalse(defaultOptions.darkMode)
    }

    data class TutorialPreferences(
        var financialGoal: String = "",
        var spendingHabit: String = "",
        var budgetAlerts: Boolean = true,
        var dailyReminder: Boolean = true,
        var badgeNotifications: Boolean = false,
        var darkMode: Boolean = false
    )
}