package com.example.android_project_msd.auth

data class User(val email: String, val password: String)

object MockAuthRepository {
    // Seed with a simple test user
    private val users = mutableListOf(
        User(email = "huhu.b2000@hotmail.com", password = "123456")

    )

    fun authenticate(email: String, password: String): Boolean {
        val e = email.trim().lowercase()
        return users.any { it.email.lowercase() == e && it.password == password }
    }

    fun register(email: String, password: String): Boolean {
        val e = email.trim().lowercase()
        if (users.any { it.email.lowercase() == e }) return false
        users += User(email = e, password = password)
        return true
    }
}

