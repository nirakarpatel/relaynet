package com.nmiet.app.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nmiet.app.data.local.relaynetDataStore
import com.nmiet.app.domain.model.AppLanguage
import com.nmiet.app.domain.model.LearnerProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserPreferencesRepository(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        val KEY_STUDENT_NAME = stringPreferencesKey("student_name")
        val KEY_EDUCATIONAL_BOARD = stringPreferencesKey("educational_board")
        val KEY_CLASS_GRADE = stringPreferencesKey("class_grade")
        val KEY_LANGUAGE_MODE = stringPreferencesKey("app_language_mode")

        @Volatile
        private var INSTANCE: UserPreferencesRepository? = null

        fun getInstance(context: Context): UserPreferencesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferencesRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    val learnerProfile: StateFlow<LearnerProfile> = context.relaynetDataStore.data
        .map { preferences ->
            val name = preferences[KEY_STUDENT_NAME] ?: "Soumya Ranjan"
            val board = preferences[KEY_EDUCATIONAL_BOARD] ?: "ODISHA_STATE"
            val grade = preferences[KEY_CLASS_GRADE] ?: "Class 9"
            val langCode = preferences[KEY_LANGUAGE_MODE]
            val language = AppLanguage.fromCode(langCode)

            LearnerProfile(
                studentName = name,
                educationalBoard = board,
                classGrade = grade,
                languageMode = language
            )
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = LearnerProfile(
                studentName = "Soumya Ranjan",
                educationalBoard = "ODISHA_STATE",
                classGrade = "Class 9",
                languageMode = AppLanguage.BILINGUAL_EN_HI
            )
        )

    fun updateEducationalBoard(board: String) {
        scope.launch {
            context.relaynetDataStore.edit { preferences ->
                preferences[KEY_EDUCATIONAL_BOARD] = board.trim()
            }
        }
    }

    fun updateClassGrade(grade: String) {
        val normalizedGrade = if (grade.startsWith("Class ")) grade else "Class $grade"
        scope.launch {
            context.relaynetDataStore.edit { preferences ->
                preferences[KEY_CLASS_GRADE] = normalizedGrade
            }
        }
    }

    fun updateStudentName(name: String) {
        scope.launch {
            context.relaynetDataStore.edit { preferences ->
                preferences[KEY_STUDENT_NAME] = name.trim()
            }
        }
    }

    fun updateLanguageMode(language: AppLanguage) {
        scope.launch {
            context.relaynetDataStore.edit { preferences ->
                preferences[KEY_LANGUAGE_MODE] = language.code
            }
        }
    }

    fun updateBoard(board: com.nmiet.app.domain.model.Board) {
        updateEducationalBoard(board.name)
    }

    fun updateGrade(grade: Int) {
        updateClassGrade("Class $grade")
    }

    fun resetProfile() {
        scope.launch {
            context.relaynetDataStore.edit { preferences ->
                preferences[KEY_STUDENT_NAME] = "Soumya Ranjan"
                preferences[KEY_EDUCATIONAL_BOARD] = "ODISHA_STATE"
                preferences[KEY_CLASS_GRADE] = "Class 9"
                preferences[KEY_LANGUAGE_MODE] = AppLanguage.BILINGUAL_EN_HI.code
            }
        }
    }
}
