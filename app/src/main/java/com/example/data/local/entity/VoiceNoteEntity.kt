package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_notes")
data class VoiceNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val filePath: String,
    val durationMillis: Long = 0,
    val transcription: String = "",
    val notes: String = "",
    val createdDate: Long = System.currentTimeMillis()
)
