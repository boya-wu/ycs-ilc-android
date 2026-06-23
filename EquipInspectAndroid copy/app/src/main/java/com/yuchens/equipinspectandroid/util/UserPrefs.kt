package com.yuchens.equipinspectandroid.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Context 擴充屬性
val Context.userPrefs by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_USERNAME = stringPreferencesKey("user_name")
        private val KEY_INTERVAL_LIMIT = booleanPreferencesKey("interval_limit")
    }

    val username: Flow<String?> = context.userPrefs.data
        .map { prefs -> prefs[KEY_USERNAME] }

    val intervalLimit: Flow<Boolean> = context.userPrefs.data
        .map { prefs -> prefs[KEY_INTERVAL_LIMIT] ?: false }

    suspend fun setUsername(username: String) {
        context.userPrefs.edit { prefs -> prefs[KEY_USERNAME] = username }
    }

    suspend fun setIntervalLimit(value: Boolean) {
        context.userPrefs.edit { prefs -> prefs[KEY_INTERVAL_LIMIT] = value }
    }

    suspend fun clear() {
        context.userPrefs.edit { it.clear() }
    }
}
