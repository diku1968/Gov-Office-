package com.example.data.repository

import com.example.data.local.dao.NoteDao
import com.example.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()

    fun getNotesForRelated(type: String, relatedId: Long): Flow<List<NoteEntity>> =
        noteDao.getNotesForRelated(type, relatedId)

    fun searchNotes(query: String): Flow<List<NoteEntity>> =
        noteDao.searchNotes(query)

    suspend fun getNoteById(id: Long): NoteEntity? = noteDao.getNoteById(id)

    fun getNoteFlow(id: Long): Flow<NoteEntity?> = noteDao.getNoteFlow(id)

    suspend fun insertNote(note: NoteEntity): Long = noteDao.insertNote(note)

    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)

    suspend fun togglePin(note: NoteEntity) =
        noteDao.updateNote(note.copy(isPinned = !note.isPinned, modifiedDate = System.currentTimeMillis()))

    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)

    suspend fun deleteNoteById(id: Long) = noteDao.deleteNoteById(id)

    suspend fun getAllNotesSnapshot(): List<NoteEntity> = noteDao.getAllNotesSnapshot()

    suspend fun insertAll(notes: List<NoteEntity>) = noteDao.insertAll(notes)
}
