package com.example.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.NoteEntity
import com.example.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(
    private val noteRepo: NoteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val notes: StateFlow<List<NoteEntity>> = combine(
        noteRepo.allNotes,
        _searchQuery
    ) { all, query ->
        if (query.isBlank()) {
            all
        } else {
            all.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.content.contains(query, ignoreCase = true) ||
                        it.category.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun saveNote(
        id: Long = 0,
        title: String,
        content: String,
        category: String,
        isPinned: Boolean = false,
        relatedType: String? = null,
        relatedId: Long? = null
    ) {
        viewModelScope.launch {
            val note = NoteEntity(
                id = id,
                title = title,
                content = content,
                category = category,
                createdDate = System.currentTimeMillis(),
                modifiedDate = System.currentTimeMillis(),
                isPinned = isPinned,
                relatedType = relatedType,
                relatedId = relatedId
            )
            if (id == 0L) {
                noteRepo.insertNote(note)
            } else {
                noteRepo.updateNote(note)
            }
        }
    }

    fun togglePin(note: NoteEntity) {
        viewModelScope.launch {
            noteRepo.togglePin(note)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            noteRepo.deleteNote(note)
        }
    }
}
