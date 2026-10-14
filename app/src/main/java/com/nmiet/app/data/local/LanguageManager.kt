package com.nmiet.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nmiet.app.domain.model.AppLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LanguageManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val languageKey = stringPreferencesKey("app_language_mode")

    val currentLanguage: StateFlow<AppLanguage> = context.relaynetDataStore.data
        .map { preferences ->
            AppLanguage.fromCode(preferences[languageKey])
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = AppLanguage.BILINGUAL_EN_OR
        )

    fun setLanguage(language: AppLanguage) {
        scope.launch {
            context.relaynetDataStore.edit { preferences ->
                preferences[languageKey] = language.code
            }
        }
    }

    companion object {
        @Volatile
        private var instance: LanguageManager? = null

        fun getInstance(context: Context): LanguageManager {
            return instance ?: synchronized(this) {
                instance ?: LanguageManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
