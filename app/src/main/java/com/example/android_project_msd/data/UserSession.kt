package com.example.android_project_msd.data

object UserSession {
    var currentUserId: String? = null
    var currentUserEmail: String? = null

    fun login(userId: String, email: String) {
        currentUserId = userId
        currentUserEmail = email
    }

    fun logout() {
        currentUserId = null
        currentUserEmail = null
    }

    fun isLoggedIn(): Boolean = currentUserId != null
}

