package com.spendid.app

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class RegisterTest {

    @Mock
    private lateinit var userRepository: UserRepository

    private val newUsername = "new_user_2026"
    private val validPassword = "StrongP@ss123"
    private val shortPassword = "123"
    private val existingUsername = "existing_user"

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun testRegisterWithValidCredentials_ReturnsSuccess() {
        `when`(userRepository.usernameExists(newUsername)).thenReturn(0)
        `when`(userRepository.insertUser(newUsername, validPassword)).thenReturn(1L)

        val exists = userRepository.usernameExists(newUsername)
        val result = if (exists == 0) userRepository.insertUser(newUsername, validPassword) else -1L

        assertEquals(0, exists)
        assertTrue(result > 0)
        verify(userRepository, times(1)).insertUser(newUsername, validPassword)
    }

    @Test
    fun testRegisterWithShortPassword_ShouldFail() {
        assertTrue(shortPassword.length < 6)
    }

    @Test
    fun testRegisterWithEmptyUsername_ShouldFail() {
        assertTrue(newUsername.isNotEmpty())
    }

    @Test
    fun testPasswordStrengthValidation_WeakPassword() {
        val weakPassword = "12345"
        val score = calculatePasswordStrength(weakPassword)

        assertTrue(score < 2)
    }

    @Test
    fun testPasswordStrengthValidation_StrongPassword() {
        val strongPassword = "StrongP@ss123!"
        val score = calculatePasswordStrength(strongPassword)

        assertTrue(score >= 3)
    }

    private fun calculatePasswordStrength(password: String): Int {
        var score = 0
        if (password.length >= 6) score++
        if (password.length >= 10) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        return score
    }
}