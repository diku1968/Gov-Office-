package com.example.ui.files

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.AttachmentEntity
import com.example.data.local.entity.FileEntity
import com.example.data.local.entity.FileHistoryEntity
import com.example.data.repository.FileRepository
import com.example.util.DateUtils
import com.example.util.ReminderWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FileViewModel(
    private val fileRepo: FileRepository,
    private val appContext: Context
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _statusFilter = MutableStateFlow("ALL")
    val statusFilter: StateFlow<String> = _statusFilter

    val files: StateFlow<List<FileEntity>> = combine(
        fileRepo.allFiles,
        _searchQuery,
        _statusFilter
    ) { allFiles, query, filter ->
        allFiles.filter { file ->
            val matchesQuery = query.isBlank() ||
                    file.fileNumber.contains(query, ignoreCase = true) ||
                    file.subject.contains(query, ignoreCase = true) ||
                    file.currentSection.contains(query, ignoreCase = true) ||
                    file.currentPerson.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                "ALL" -> true
                else -> file.currentStatus.equals(filter, ignoreCase = true)
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedFileId = MutableStateFlow<Long?>(null)

    val currentFile: StateFlow<FileEntity?> = _selectedFileId.flatMapLatest { id ->
        if (id != null) fileRepo.getFileFlow(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentFileHistory: StateFlow<List<FileHistoryEntity>> = _selectedFileId.flatMapLatest { id ->
        if (id != null) fileRepo.getHistoryForFile(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentFileAttachments: StateFlow<List<AttachmentEntity>> = _selectedFileId.flatMapLatest { id ->
        if (id != null) fileRepo.getAttachmentsForFile(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedFileId(id: Long?) {
        _selectedFileId.value = id
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onStatusFilterChanged(status: String) {
        _statusFilter.value = status
    }

    fun createFile(
        fileNumber: String,
        subject: String,
        description: String,
        receivedDate: Long,
        currentStatus: String,
        currentSection: String,
        currentPerson: String,
        nextAction: String,
        priority: String,
        dueDate: Long,
        reminderDate: Long?,
        notes: String
    ) {
        viewModelScope.launch {
            val file = FileEntity(
                fileNumber = fileNumber,
                subject = subject,
                description = description,
                receivedDate = receivedDate,
                currentStatus = currentStatus,
                currentSection = currentSection,
                currentPerson = currentPerson,
                nextAction = nextAction,
                priority = priority,
                dueDate = dueDate,
                reminderDate = reminderDate,
                notes = notes,
                createdDate = System.currentTimeMillis(),
                modifiedDate = System.currentTimeMillis()
            )
            val fileId = fileRepo.createFile(
                file = file,
                initialSection = currentSection,
                initialPerson = currentPerson,
                remarks = "Registered in $currentSection by $currentPerson"
            )

            reminderDate?.let { remTime ->
                if (remTime > System.currentTimeMillis()) {
                    ReminderWorker.scheduleReminder(
                        context = appContext,
                        tag = "file_$fileId",
                        notificationId = (fileId + 10000).toInt(),
                        title = "File Action Reminder: #$fileNumber",
                        message = "Subject: $subject | Next Action: $nextAction",
                        type = "File",
                        targetTimeMillis = remTime
                    )
                }
            }
        }
    }

    fun updateFileMovement(
        fileId: Long,
        newStatus: String,
        newSection: String,
        newPerson: String,
        nextAction: String,
        remarks: String
    ) {
        viewModelScope.launch {
            fileRepo.updateFileMovement(
                fileId = fileId,
                newStatus = newStatus,
                newSection = newSection,
                newPerson = newPerson,
                nextAction = nextAction,
                remarks = remarks
            )
        }
    }

    fun addAttachment(fileId: Long, fileName: String, uri: String, mimeType: String, fileSize: Long) {
        viewModelScope.launch {
            fileRepo.addAttachment(
                AttachmentEntity(
                    fileId = fileId,
                    fileName = fileName,
                    uri = uri,
                    mimeType = mimeType,
                    fileSize = fileSize
                )
            )
        }
    }

    fun deleteAttachment(attachment: AttachmentEntity) {
        viewModelScope.launch {
            fileRepo.deleteAttachment(attachment)
        }
    }

    fun deleteFile(file: FileEntity) {
        viewModelScope.launch {
            ReminderWorker.cancelReminder(appContext, "file_${file.id}")
            fileRepo.deleteFile(file)
        }
    }
}
