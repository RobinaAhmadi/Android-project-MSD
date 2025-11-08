package com.example.android_project_msd.model.auth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TokenStore private constructor(
    private val dataStore: DataStore<Preferences>
) {
    val token: Flow<String?> = dataStore.data.map { prefs ->
        prefs[KEY_TOKEN]
    }

    suspend fun save(token: String) {
        dataStore.edit { prefs -> prefs[KEY_TOKEN] = token }
    }

    suspend fun clear() {
        dataStore.edit { prefs -> prefs.remove(KEY_TOKEN) }
    }

    companion object {
        private val Context.authDataStore: DataStore<Preferences> by preferencesDataStore("auth_prefs")
        private val KEY_TOKEN = stringPreferencesKey("auth_token")

        fun getInstance(context: Context): TokenStore =
            TokenStore(context.applicationContext.authDataStore)
    }
}
