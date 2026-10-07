package com.example.data.repository

import com.example.data.local.dao.VoiceNoteDao
import com.example.data.local.entity.VoiceNoteEntity
import kotlinx.coroutines.flow.Flow
import java.io.File

class VoiceNoteRepository(private val dao: VoiceNoteDao) {
    val allVoiceNotes: Flow<List<VoiceNoteEntity>> = dao.getAllVoiceNotes()

    fun searchVoiceNotes(query: String): Flow<List<VoiceNoteEntity>> = dao.searchVoiceNotes(query)

    suspend fun getVoiceNoteById(id: Long): VoiceNoteEntity? = dao.getVoiceNoteById(id)

    suspend fun insertVoiceNote(voiceNote: VoiceNoteEntity): Long = dao.insertVoiceNote(voiceNote)

    suspend fun updateVoiceNote(voiceNote: VoiceNoteEntity) = dao.updateVoiceNote(voiceNote)

    suspend fun deleteVoiceNote(voiceNote: VoiceNoteEntity) {
        // Also remove local file
        try {
            val file = File(voiceNote.filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {}
        dao.deleteVoiceNote(voiceNote)
    }
}
