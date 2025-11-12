package com.example.android_project_msd.utils

import android.content.Context
import androidx.core.content.edit

class UserPrefs(context: Context) {
    private val sp = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun isLoggedIn(): Boolean = sp.getBoolean("logged_in", false)

    fun setLoggedIn(value: Boolean) = sp.edit {
        putBoolean("logged_in", value)
    }
}
