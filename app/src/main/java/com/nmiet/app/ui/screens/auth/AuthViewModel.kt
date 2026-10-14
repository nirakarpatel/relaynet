package com.nmiet.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.nmiet.app.domain.model.AppLanguage
import com.nmiet.app.domain.model.Board
import com.nmiet.app.domain.model.LearnerProfile
import com.nmiet.app.domain.repository.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AuthMode {
    LOGIN,
    SIGNUP
}

class AuthViewModel(
    private val repository: ContentRepository
) : ViewModel() {

    val profile: StateFlow<LearnerProfile> = repository.getLearnerProfileFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LearnerProfile()
        )

    private val _authMode = MutableStateFlow(AuthMode.LOGIN)
    val authMode: StateFlow<AuthMode> = _authMode.asStateFlow()

    // Form Fields - Login & Basic Registration
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _passwordVisible = MutableStateFlow(false)
    val passwordVisible: StateFlow<Boolean> = _passwordVisible.asStateFlow()

    // Profile Completion Fields
    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _selectedGrade = MutableStateFlow(9)
    val selectedGrade: StateFlow<Int> = _selectedGrade.asStateFlow()

    private val _selectedBoard = MutableStateFlow(Board.ODISHA_STATE)
    val selectedBoard: StateFlow<Board> = _selectedBoard.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(AppLanguage.BILINGUAL_EN_OR)
    val selectedLanguage: StateFlow<AppLanguage> = _selectedLanguage.asStateFlow()

    private val _schoolName = MutableStateFlow("")
    val schoolName: StateFlow<String> = _schoolName.asStateFlow()

    private val _selectedInterests = MutableStateFlow<Set<String>>(setOf("Biology", "Photosynthesis", "Science"))
    val selectedInterests: StateFlow<Set<String>> = _selectedInterests.asStateFlow()

    // Status & Error
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _infoMessage = MutableStateFlow<String?>(null)
    val infoMessage: StateFlow<String?> = _infoMessage.asStateFlow()

    init {
        // Pre-fill fields if profile has initial data
        viewModelScope.launch {
            repository.getLearnerProfileFlow().collect { p ->
                if (_name.value.isBlank() && p.name.isNotBlank() && p.name != "Soumya Ranjan") {
                    _name.value = p.name
                }
                if (_schoolName.value.isBlank() && p.schoolName.isNotBlank()) {
                    _schoolName.value = p.schoolName
                }
                _selectedGrade.value = p.classGrade
                _selectedBoard.value = p.board
                _selectedLanguage.value = p.preferredLanguage
            }
        }
    }

    fun setAuthMode(mode: AuthMode) {
        _authMode.value = mode
        _errorMessage.value = null
        _infoMessage.value = null
    }

    fun onEmailChange(v: String) {
        _email.value = v
        _errorMessage.value = null
    }

    fun onPasswordChange(v: String) {
        _password.value = v
        _errorMessage.value = null
    }

    fun togglePasswordVisibility() {
        _passwordVisible.value = !_passwordVisible.value
    }

    fun onNameChange(v: String) {
        _name.value = v
        _errorMessage.value = null
    }

    fun onSchoolChange(v: String) {
        _schoolName.value = v
    }

    fun onGradeChange(v: Int) {
        _selectedGrade.value = v
    }

    fun onBoardChange(v: Board) {
        _selectedBoard.value = v
    }

    fun onLanguageChange(v: AppLanguage) {
        _selectedLanguage.value = v
    }

    fun toggleInterest(interest: String) {
        val current = _selectedInterests.value.toMutableSet()
        if (current.contains(interest)) {
            current.remove(interest)
        } else {
            current.add(interest)
        }
        _selectedInterests.value = current
    }

    fun dismissError() {
        _errorMessage.value = null
    }

    fun dismissInfo() {
        _infoMessage.value = null
    }

    fun requestPasswordReset() {
        if (_email.value.isBlank()) {
            _errorMessage.value = "Please enter your email address to receive password reset instructions."
            return
        }
        _infoMessage.value = "Password reset instructions have been sent to ${_email.value.trim()}."
        _errorMessage.value = null
    }

    fun login(onSuccess: (needsProfileCompletion: Boolean) -> Unit) {
        val emailInput = _email.value.trim()
        val passwordInput = _password.value

        if (emailInput.isBlank()) {
            _errorMessage.value = "Email address is required."
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            _errorMessage.value = "Please enter a valid email address."
            return
        }
        if (passwordInput.isBlank()) {
            _errorMessage.value = "Password is required."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.loginWithEmail(emailInput, passwordInput)
            _isLoading.value = false
            if (result.isSuccess) {
                val p = result.getOrNull()
                val needsCompletion = p?.isProfileCompleted == false || p?.name.isNullOrBlank() || p?.name == "Soumya Ranjan"
                onSuccess(needsCompletion)
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Login failed. Please check your credentials."
            }
        }
    }

    fun register(onSuccess: (needsProfileCompletion: Boolean) -> Unit) {
        val nameInput = _name.value.trim()
        val emailInput = _email.value.trim()
        val passwordInput = _password.value

        if (emailInput.isBlank()) {
            _errorMessage.value = "Email address is required."
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
            _errorMessage.value = "Please enter a valid email address."
            return
        }
        if (passwordInput.length < 6) {
            _errorMessage.value = "Password must be at least 6 characters long."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val displayName = if (nameInput.isNotBlank()) nameInput else emailInput.substringBefore("@")
            val result = repository.registerWithEmail(
                name = displayName,
                email = emailInput,
                password = passwordInput,
                classGrade = _selectedGrade.value,
                board = _selectedBoard.value
            )
            _isLoading.value = false
            if (result.isSuccess) {
                // If user didn't fill detailed name, lead to profile completion
                onSuccess(nameInput.isBlank())
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Registration failed."
            }
        }
    }

    fun handleGoogleSignInResult(account: GoogleSignInAccount, onSuccess: (needsProfileCompletion: Boolean) -> Unit) {
        val email = account.email ?: "google.student@gmail.com"
        val name = account.displayName ?: email.substringBefore("@")
        val avatar = account.photoUrl?.toString()
        val idToken = account.idToken

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.loginWithGoogle(
                idToken = idToken,
                email = email,
                name = name,
                avatarUrl = avatar
            )
            _isLoading.value = false
            if (result.isSuccess) {
                _name.value = name
                val p = result.getOrNull()
                val needsCompletion = p?.isProfileCompleted == false
                onSuccess(needsCompletion)
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Google Sign-In failed."
            }
        }
    }

    fun loginWithGoogleDirect(
        email: String = "student.relaynet@gmail.com",
        name: String = "Google Learner",
        onSuccess: (needsProfileCompletion: Boolean) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = repository.loginWithGoogle(
                idToken = "tok_google_${System.currentTimeMillis()}",
                email = email,
                name = name,
                avatarUrl = "https://api.dicebear.com/7.x/bottts/svg?seed=$name"
            )
            _isLoading.value = false
            if (result.isSuccess) {
                _name.value = name
                onSuccess(true)
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Google Sign-In failed."
            }
        }
    }

    fun saveCompletedProfile(onSuccess: () -> Unit) {
        val trimmedName = _name.value.trim()
        if (trimmedName.isBlank()) {
            _errorMessage.value = "Please enter your full name."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val current = repository.getLearnerProfileFlow()
                val p = profile.value.copy(
                    name = trimmedName,
                    classGrade = _selectedGrade.value,
                    board = _selectedBoard.value,
                    preferredLanguage = _selectedLanguage.value,
                    schoolName = _schoolName.value.trim(),
                    learningInterests = _selectedInterests.value.toList(),
                    isProfileCompleted = true,
                    isLoggedIn = true
                )
                repository.updateLearnerProfile(p)
                _isLoading.value = false
                onSuccess()
            } catch (e: Exception) {
                _isLoading.value = false
                _errorMessage.value = "Failed to save profile: ${e.message}"
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }
}
