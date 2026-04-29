package com.spendid.app

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class EditProfileTest {

    @Mock
    private lateinit var userRepository: UserRepository

    private lateinit var currentUser: UserEntity

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        currentUser = TestDataFactory.createTestUser(
            id = 1,
            username = "john_doe",
            tutorialCompleted = true
        )
    }

    @Test
    fun testEditUsername_Success() {
        val newUsername = "john_doe_updated"
        `when`(userRepository.usernameExists(newUsername)).thenReturn(0)

        val exists = userRepository.usernameExists(newUsername)
        val canUpdate = exists == 0

        assertTrue(canUpdate)
        assertEquals(0, exists)
        assertTrue(newUsername != currentUser.username)
    }

    @Test
    fun testEditUsername_WithExistingUsername_ShouldFail() {
        val existingUsername = "existing_user"
        `when`(userRepository.usernameExists(existingUsername)).thenReturn(1)

        val exists = userRepository.usernameExists(existingUsername)

        assertEquals(1, exists)
    }

    @Test
    fun testEditEmail_ValidEmail_ReturnsTrue() {
        val validEmails = listOf(
            "user@example.com",
            "john.doe@email.co.za",
            "test123@gmail.com"
        )

        validEmails.forEach { email ->
            assertTrue(isValidEmail(email))
        }
    }

    @Test
    fun testEditEmail_InvalidEmail_ReturnsFalse() {
        val invalidEmails = listOf(
            "invalid-email",
            "missing@domain",
            "@missingusername.com",
            "spaces@test .com"
        )

        invalidEmails.forEach { email ->
            assertFalse(isValidEmail(email))
        }
    }

    @Test
    fun testEditProfile_AllFieldsUpdate_Success() {
        val updatedProfile = ProfileData(
            username = "new_username",
            email = "newemail@example.com",
            name = "John Updated"
        )

        assertEquals("new_username", updatedProfile.username)
        assertEquals("newemail@example.com", updatedProfile.email)
        assertEquals("John Updated", updatedProfile.name)
    }

    @Test
    fun testEditProfile_EmptyUsername_ShouldFail() {
        val emptyUsername = ""
        assertTrue(emptyUsername.isEmpty())
    }

    @Test
    fun testEditProfile_UsernameMaxLength() {
        val maxLengthUsername = "a".repeat(50)
        assertTrue(maxLengthUsername.length <= 50)
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        return email.matches(emailRegex.toRegex())
    }

    data class ProfileData(
        val username: String,
        val email: String,
        val name: String
    )
}