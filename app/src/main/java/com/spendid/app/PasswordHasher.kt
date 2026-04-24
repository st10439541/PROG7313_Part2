package com.spendid.app

import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {

    private const val ITERATIONS = 100_000
    private const val KEY_LENGTH = 256
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"

    /** Generate a 16‑byte random salt (hex‑encoded) */
    fun generateSalt(): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }

    /** Hash password with given salt */
    fun hash(password: String, salt: String): String {
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt.toByteArray(), ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val hash = factory.generateSecret(spec).encoded
        return hash.joinToString("") { "%02x".format(it) }
    }

    /** Verify password against stored hash + salt */
    fun verify(password: String, salt: String, storedHash: String): Boolean {
        val newHash = hash(password, salt)
        return newHash == storedHash
    }
}