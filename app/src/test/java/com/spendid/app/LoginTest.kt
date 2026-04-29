package com.spendid.app

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class LoginTest {

    @Mock
    private lateinit var userRepository: UserRepository

    private val validUsername = "john_doe"
    private val validPassword = "SecurePass123"
    private val invalidPassword = "WrongPass123"

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun testLoginWithValidCredentials_ReturnsUser() {
        val expectedUser = TestDataFactory.createTestUser(
            id = 1,
            username = validUsername,
            tutorialCompleted = true
        )

        `when`(userRepository.login(validUsername, validPassword))
            .thenReturn(expectedUser)

        val result = userRepository.login(validUsername, validPassword)

        assertNotNull(result)
        assertEquals(expectedUser.username, result?.username)
        assertTrue(result?.tutorialCompleted == true)
        verify(userRepository, times(1)).login(validUsername, validPassword)
    }

    @Test
    fun testLoginWithInvalidPassword_ReturnsNull() {
        `when`(userRepository.login(validUsername, invalidPassword))
            .thenReturn(null)

        val result = userRepository.login(validUsername, invalidPassword)

        assertNull(result)
        verify(userRepository, times(1)).login(validUsername, invalidPassword)
    }

    @Test
    fun testLoginWithNonExistentUsername_ReturnsNull() {
        val nonExistentUsername = "ghost_user"
        `when`(userRepository.login(nonExistentUsername, validPassword))
            .thenReturn(null)

        val result = userRepository.login(nonExistentUsername, validPassword)

        assertNull(result)
    }

    @Test
    fun testLoginWithEmptyUsername_ReturnsNull() {
        val result = userRepository.login("", validPassword)
        assertNull(result)
    }

    @Test
    fun testLoginWithEmptyPassword_ReturnsNull() {
        val result = userRepository.login(validUsername, "")
        assertNull(result)
    }
}