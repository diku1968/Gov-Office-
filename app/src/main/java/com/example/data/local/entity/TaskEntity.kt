package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "General",
    val priority: String = "Medium", // Low, Medium, High, Urgent
    val status: String = "Pending",   // Pending, In Progress, Completed, Cancelled
    val createdDate: Long = System.currentTimeMillis(),
    val dueDate: Long = System.currentTimeMillis(),
    val reminderDateTime: Long? = null,
    val completedDate: Long? = null,
    val notes: String = ""
)
