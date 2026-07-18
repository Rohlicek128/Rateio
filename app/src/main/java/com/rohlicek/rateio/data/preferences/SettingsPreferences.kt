package com.rohlicek.rateio.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


val Context.dataStore by preferencesDataStore(name = "settings")

class SyncPreferences(private val context: Context) {
    companion object {
        private val LAST_SYNC_KEY = longPreferencesKey("last_imdb_sync")
    }

    val lastSyncTime: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[LAST_SYNC_KEY]
    }

    suspend fun saveLastSyncTime(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SYNC_KEY] = timestamp
        }
    }
}