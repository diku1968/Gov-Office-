package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.UserProfileEntity
import com.example.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val profileRepo: UserProfileRepository
) : ViewModel() {

    val profile: StateFlow<UserProfileEntity?> = profileRepo.profileFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    fun unlockApp() {
        _isUnlocked.value = true
    }

    fun lockApp() {
        _isUnlocked.value = false
    }

    fun saveProfile(
        name: String,
        department: String,
        designation: String,
        office: String,
        empId: String
    ) {
        viewModelScope.launch {
            val current = profile.value ?: UserProfileEntity()
            profileRepo.saveProfile(
                current.copy(
                    employeeName = name,
                    department = department,
                    designation = designation,
                    office = office,
                    employeeId = empId,
                    isProfileConfigured = true
                )
            )
        }
    }

    fun setAppLock(pin: String?, enabled: Boolean) {
        viewModelScope.launch {
            profileRepo.updatePin(pin, enabled)
            if (!enabled) {
                _isUnlocked.value = true
            }
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            profileRepo.updateLanguage(lang)
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            profileRepo.updateTheme(theme)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            profileRepo.updateNotifications(enabled)
        }
    }
}
