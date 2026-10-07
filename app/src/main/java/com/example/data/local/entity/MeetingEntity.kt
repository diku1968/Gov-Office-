package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meetings")
data class MeetingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val date: Long = System.currentTimeMillis(),
    val startTime: String = "10:00",
    val endTime: String = "11:00",
    val location: String = "",
    val participants: String = "",
    val agenda: String = "",
    val discussion: String = "",
    val decisions: String = "",
    val actionPoints: String = "",
    val notes: String = "",
    val reminderDateTime: Long? = null
)
