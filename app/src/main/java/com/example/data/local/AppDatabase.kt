package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AttachmentDao
import com.example.data.local.dao.FileDao
import com.example.data.local.dao.FileHistoryDao
import com.example.data.local.dao.MeetingDao
import com.example.data.local.dao.NoteDao
import com.example.data.local.dao.TaskDao
import com.example.data.local.dao.UserProfileDao
import com.example.data.local.dao.VoiceNoteDao
import com.example.data.local.entity.AttachmentEntity
import com.example.data.local.entity.FileEntity
import com.example.data.local.entity.FileHistoryEntity
import com.example.data.local.entity.MeetingEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.local.entity.VoiceNoteEntity

@Database(
    entities = [
        UserProfileEntity::class,
        TaskEntity::class,
        FileEntity::class,
        FileHistoryEntity::class,
        AttachmentEntity::class,
        MeetingEntity::class,
        NoteEntity::class,
        VoiceNoteEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun taskDao(): TaskDao
    abstract fun fileDao(): FileDao
    abstract fun fileHistoryDao(): FileHistoryDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun meetingDao(): MeetingDao
    abstract fun noteDao(): NoteDao
    abstract fun voiceNoteDao(): VoiceNoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "govwork_assistant_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
