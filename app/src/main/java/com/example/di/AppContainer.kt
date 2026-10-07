package com.example.di

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.repository.FileRepository
import com.example.data.repository.MeetingRepository
import com.example.data.repository.NoteRepository
import com.example.data.repository.TaskRepository
import com.example.data.repository.UserProfileRepository
import com.example.data.repository.VoiceNoteRepository

interface AppContainer {
    val userProfileRepository: UserProfileRepository
    val taskRepository: TaskRepository
    val fileRepository: FileRepository
    val meetingRepository: MeetingRepository
    val noteRepository: NoteRepository
    val voiceNoteRepository: VoiceNoteRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {
    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    override val userProfileRepository: UserProfileRepository by lazy {
        UserProfileRepository(database.userProfileDao())
    }

    override val taskRepository: TaskRepository by lazy {
        TaskRepository(database.taskDao())
    }

    override val fileRepository: FileRepository by lazy {
        FileRepository(database.fileDao(), database.fileHistoryDao(), database.attachmentDao())
    }

    override val meetingRepository: MeetingRepository by lazy {
        MeetingRepository(database.meetingDao())
    }

    override val noteRepository: NoteRepository by lazy {
        NoteRepository(database.noteDao())
    }

    override val voiceNoteRepository: VoiceNoteRepository by lazy {
        VoiceNoteRepository(database.voiceNoteDao())
    }
}
