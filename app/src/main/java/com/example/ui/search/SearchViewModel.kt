package com.example.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.FileEntity
import com.example.data.local.entity.MeetingEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.repository.FileRepository
import com.example.data.repository.MeetingRepository
import com.example.data.repository.NoteRepository
import com.example.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class GlobalSearchResults(
    val query: String = "",
    val tasks: List<TaskEntity> = emptyList(),
    val files: List<FileEntity> = emptyList(),
    val meetings: List<MeetingEntity> = emptyList(),
    val notes: List<NoteEntity> = emptyList()
) {
    val totalCount: Int get() = tasks.size + files.size + meetings.size + notes.size
}

class SearchViewModel(
    taskRepo: TaskRepository,
    fileRepo: FileRepository,
    meetingRepo: MeetingRepository,
    noteRepo: NoteRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val searchResults: StateFlow<GlobalSearchResults> = combine(
        _query,
        taskRepo.allTasks,
        fileRepo.allFiles,
        meetingRepo.allMeetings,
        noteRepo.allNotes
    ) { q, allTasks, allFiles, allMeetings, allNotes ->
        if (q.isBlank()) {
            GlobalSearchResults()
        } else {
            GlobalSearchResults(
                query = q,
                tasks = allTasks.filter {
                    it.title.contains(q, ignoreCase = true) ||
                            it.description.contains(q, ignoreCase = true) ||
                            it.category.contains(q, ignoreCase = true)
                },
                files = allFiles.filter {
                    it.fileNumber.contains(q, ignoreCase = true) ||
                            it.subject.contains(q, ignoreCase = true) ||
                            it.currentSection.contains(q, ignoreCase = true) ||
                            it.currentPerson.contains(q, ignoreCase = true)
                },
                meetings = allMeetings.filter {
                    it.title.contains(q, ignoreCase = true) ||
                            it.agenda.contains(q, ignoreCase = true) ||
                            it.location.contains(q, ignoreCase = true)
                },
                notes = allNotes.filter {
                    it.title.contains(q, ignoreCase = true) ||
                            it.content.contains(q, ignoreCase = true)
                }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GlobalSearchResults())

    fun setQuery(q: String) {
        _query.value = q
    }
}
