package com.example.data.repository

import com.example.data.local.dao.UserProfileDao
import com.example.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

class UserProfileRepository(private val dao: UserProfileDao) {
    val profileFlow: Flow<UserProfileEntity?> = dao.getUserProfile()

    suspend fun getProfileOnce(): UserProfileEntity? = dao.getUserProfileOnce()

    suspend fun saveProfile(profile: UserProfileEntity) {
        dao.insertOrUpdateProfile(profile)
    }

    suspend fun updatePin(pin: String?, enabled: Boolean) {
        val current = dao.getUserProfileOnce() ?: UserProfileEntity()
        dao.insertOrUpdateProfile(
            current.copy(
                pinCode = pin,
                isAppLockEnabled = enabled
            )
        )
    }

    suspend fun updateLanguage(lang: String) {
        val current = dao.getUserProfileOnce() ?: UserProfileEntity()
        dao.insertOrUpdateProfile(current.copy(languageCode = lang))
    }

    suspend fun updateTheme(theme: String) {
        val current = dao.getUserProfileOnce() ?: UserProfileEntity()
        dao.insertOrUpdateProfile(current.copy(themeMode = theme))
    }

    suspend fun updateNotifications(enabled: Boolean) {
        val current = dao.getUserProfileOnce() ?: UserProfileEntity()
        dao.insertOrUpdateProfile(current.copy(notificationsEnabled = enabled))
    }
}
