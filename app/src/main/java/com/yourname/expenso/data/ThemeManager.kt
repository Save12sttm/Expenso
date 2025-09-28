package com.yourname.expenso.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class AppTheme {
    LIGHT, DARK, SYSTEM
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

@Singleton
class AppPreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Theme preferences
    private val themeKey = stringPreferencesKey("app_theme")
    private val notificationsKey = booleanPreferencesKey("notifications_enabled")
    
    // Profile preferences
    private val profileNameKey = stringPreferencesKey("profile_name")
    private val profileEmailKey = stringPreferencesKey("profile_email")
    private val profileAvatarKey = stringPreferencesKey("profile_avatar")

    val theme: Flow<AppTheme> = context.dataStore.data.map { preferences ->
        when (preferences[themeKey]) {
            "LIGHT" -> AppTheme.LIGHT
            "DARK" -> AppTheme.DARK
            else -> AppTheme.SYSTEM
        }
    }

    suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[themeKey] = theme.name
        }
    }
    
    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[notificationsKey] ?: true
    }
    
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[notificationsKey] = enabled
        }
    }
    
    // Profile methods
    val profileName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[profileNameKey] ?: "John Doe"
    }
    
    val profileEmail: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[profileEmailKey] ?: "john.doe@example.com"
    }
    
    val profileAvatar: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[profileAvatarKey] ?: "Person"
    }
    
    suspend fun setProfileName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[profileNameKey] = name
        }
    }
    
    suspend fun setProfileEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[profileEmailKey] = email
        }
    }
    
    suspend fun setProfileAvatar(avatar: String) {
        context.dataStore.edit { preferences ->
            preferences[profileAvatarKey] = avatar
        }
    }
}