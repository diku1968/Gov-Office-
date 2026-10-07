package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.VoiceNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceNoteDao {
    @Query("SELECT * FROM voice_notes ORDER BY createdDate DESC")
    fun getAllVoiceNotes(): Flow<List<VoiceNoteEntity>>

    @Query("SELECT * FROM voice_notes WHERE id = :id LIMIT 1")
    suspend fun getVoiceNoteById(id: Long): VoiceNoteEntity?

    @Query("SELECT * FROM voice_notes WHERE title LIKE '%' || :query || '%' OR transcription LIKE '%' || :query || '%' OR notes LIKE '%' || :query || '%'")
    fun searchVoiceNotes(query: String): Flow<List<VoiceNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceNote(voiceNote: VoiceNoteEntity): Long

    @Update
    suspend fun updateVoiceNote(voiceNote: VoiceNoteEntity)

    @Delete
    suspend fun deleteVoiceNote(voiceNote: VoiceNoteEntity)

    @Query("DELETE FROM voice_notes WHERE id = :id")
    suspend fun deleteVoiceNoteById(id: Long)
}
