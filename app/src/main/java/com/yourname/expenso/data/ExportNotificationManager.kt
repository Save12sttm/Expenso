package com.yourname.expenso.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "export_notifications")

@Singleton
class ExportNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val LAST_EXPORT_KEY = stringPreferencesKey("last_export_info")
    private val SHOWN_EXPORTS_KEY = stringPreferencesKey("shown_exports")

    suspend fun saveExportInfo(filePath: String, timestamp: Long) {
        val exportInfo = "$timestamp|$filePath"
        context.dataStore.edit { preferences ->
            preferences[LAST_EXPORT_KEY] = exportInfo
        }
    }

    suspend fun markExportAsShown(timestamp: Long) {
        context.dataStore.edit { preferences ->
            val shown = preferences[SHOWN_EXPORTS_KEY] ?: ""
            preferences[SHOWN_EXPORTS_KEY] = "$shown,$timestamp"
        }
    }

    fun getUnshownExport(): Flow<ExportInfo?> {
        return context.dataStore.data.map { preferences ->
            val exportInfo = preferences[LAST_EXPORT_KEY] ?: return@map null
            val shownExports = preferences[SHOWN_EXPORTS_KEY] ?: ""
            
            val parts = exportInfo.split("|")
            if (parts.size == 2) {
                val timestamp = parts[0].toLongOrNull() ?: return@map null
                val filePath = parts[1]
                
                if (!shownExports.contains(timestamp.toString())) {
                    ExportInfo(timestamp, filePath)
                } else null
            } else null
        }
    }
}

data class ExportInfo(
    val timestamp: Long,
    val filePath: String
)