package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileNumber: String,
    val subject: String,
    val description: String = "",
    val receivedDate: Long = System.currentTimeMillis(),
    val currentStatus: String = "Received", // Received, Under Process, Forwarded, Pending, Approved, Rejected, Closed
    val currentSection: String = "",
    val currentPerson: String = "",
    val nextAction: String = "",
    val priority: String = "Medium", // Low, Medium, High, Urgent
    val dueDate: Long = System.currentTimeMillis(),
    val reminderDate: Long? = null,
    val notes: String = "" ,
    val createdDate: Long = System.currentTimeMillis(),
    val modifiedDate: Long = System.currentTimeMillis()
)
