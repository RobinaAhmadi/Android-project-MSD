package com.example.android_project_msd.data

import android.content.Context
import androidx.core.content.edit

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveSession(
        token: String,
        sessionId: String,
        userId: String,
        email: String
    ) {
        prefs.edit {
            putString(KEY_TOKEN, token)
            putString(KEY_SESSION_ID, sessionId)
            putString(KEY_USER_ID, userId)
            putString(KEY_EMAIL, email)
        }
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)
    fun getSessionId(): String? = prefs.getString(KEY_SESSION_ID, null)
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)

    fun clear() {
        prefs.edit { clear() }
    }

    companion object {
        private const val PREFS_NAME = "remote_session_prefs"
        private const val KEY_TOKEN = "token"
        private const val KEY_SESSION_ID = "session_id"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
    }
}
