package com.example.android_project_msd.data

object UserSession {
    var currentUserId: Int? = null
    var currentUserEmail: String? = null

    fun login(userId: Int, email: String) {
        currentUserId = userId
        currentUserEmail = email
    }

    fun logout() {
        currentUserId = null
        currentUserEmail = null
    }

    fun isLoggedIn(): Boolean = currentUserId != null
}

