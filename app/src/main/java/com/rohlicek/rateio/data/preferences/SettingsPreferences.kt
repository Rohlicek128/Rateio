package com.rohlicek.rateio.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.dataStore by preferencesDataStore(name = "settings")

class SyncPreferences(private val context: Context) {
    companion object {
        private val TMDB_API_TOKEN_KEY = stringPreferencesKey("tmdb_api_token")
        private val IS_FIRST_LAUNCH_KEY = booleanPreferencesKey("is_first_launch")
        private val LAST_SYNC_KEY = longPreferencesKey("last_imdb_sync")
    }

    val tmdbApiToken: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[TMDB_API_TOKEN_KEY] ?: ""
    }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_FIRST_LAUNCH_KEY] ?: true
    }

    val lastSyncTime: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[LAST_SYNC_KEY]
    }

    suspend fun saveTmdbApiToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TMDB_API_TOKEN_KEY] = token
        }
    }

    suspend fun setFirstLaunchCompleted() {
        context.dataStore.edit { preferences ->
            preferences[IS_FIRST_LAUNCH_KEY] = false
        }
    }

    suspend fun saveLastSyncTime(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SYNC_KEY] = timestamp
        }
    }
}