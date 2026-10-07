package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val employeeName: String = "",
    val department: String = "",
    val designation: String = "",
    val office: String = "",
    val employeeId: String = "",
    val isProfileConfigured: Boolean = false,
    val pinCode: String? = null,
    val isAppLockEnabled: Boolean = false,
    val languageCode: String = "en",
    val themeMode: String = "system",
    val notificationsEnabled: Boolean = true
)
