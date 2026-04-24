package com.spendid.app

data class UserEntity(
    val id: Int = 0,
    val username: String,
    val passwordHash: String,
    val salt: String = "",
    val tutorialCompleted: Boolean = false
)