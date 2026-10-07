package com.example.ui.reports

import android.content.Context
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
import com.example.data.repository.UserProfileRepository
import com.example.util.DateUtils
import com.example.util.PdfReportGenerator
import com.example.util.ReportData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class ReportType {
    DAILY, WEEKLY, MONTHLY
}

data class ReportPreviewState(
    val reportType: ReportType = ReportType.DAILY,
    val periodLabel: String = "",
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val pendingTasks: Int = 0,
    val overdueTasks: Int = 0,
    val filesProcessed: Int = 0,
    val meetingsCount: Int = 0,
    val notesCount: Int = 0,
    val tasksList: List<TaskEntity> = emptyList(),
    val filesList: List<FileEntity> = emptyList(),
    val meetingsList: List<MeetingEntity> = emptyList(),
    val notesList: List<NoteEntity> = emptyList()
)

class ReportViewModel(
    private val profileRepo: UserProfileRepository,
    private val taskRepo: TaskRepository,
    private val fileRepo: FileRepository,
    private val meetingRepo: MeetingRepository,
    private val noteRepo: NoteRepository,
    private val appContext: Context
) : ViewModel() {

    private val _selectedType = MutableStateFlow(ReportType.DAILY)
    val selectedType: StateFlow<ReportType> = _selectedType.asStateFlow()

    private val _generatedPdfFile = MutableStateFlow<File?>(null)
    val generatedPdfFile: StateFlow<File?> = _generatedPdfFile.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    val reportState: StateFlow<ReportPreviewState> = combine(
        _selectedType,
        taskRepo.allTasks,
        fileRepo.allFiles,
        meetingRepo.allMeetings,
        noteRepo.allNotes
    ) { type, allTasks, allFiles, allMeetings, allNotes ->
        val now = System.currentTimeMillis()
        val (startTime, endTime, periodLabel) = when (type) {
            ReportType.DAILY -> Triple(
                DateUtils.getStartOfDay(now),
                DateUtils.getEndOfDay(now),
                DateUtils.formatDate(now)
            )
            ReportType.WEEKLY -> Triple(
                DateUtils.getStartOfWeek(now),
                DateUtils.getEndOfDay(now),
                "${DateUtils.formatDate(DateUtils.getStartOfWeek(now))} - ${DateUtils.formatDate(now)}"
            )
            ReportType.MONTHLY -> Triple(
                DateUtils.getStartOfMonth(now),
                DateUtils.getEndOfDay(now),
                "${DateUtils.formatDate(DateUtils.getStartOfMonth(now))} - ${DateUtils.formatDate(now)}"
            )
        }

        val filteredTasks = allTasks.filter { it.createdDate in startTime..endTime || it.dueDate in startTime..endTime }
        val filteredFiles = allFiles.filter { it.modifiedDate in startTime..endTime || it.receivedDate in startTime..endTime }
        val filteredMeetings = allMeetings.filter { it.date in startTime..endTime }
        val filteredNotes = allNotes.filter { it.createdDate in startTime..endTime }

        ReportPreviewState(
            reportType = type,
            periodLabel = periodLabel,
            totalTasks = filteredTasks.size,
            completedTasks = filteredTasks.count { it.status == "Completed" },
            pendingTasks = filteredTasks.count { it.status == "Pending" || it.status == "In Progress" },
            overdueTasks = filteredTasks.count { it.dueDate < now && it.status != "Completed" },
            filesProcessed = filteredFiles.size,
            meetingsCount = filteredMeetings.size,
            notesCount = filteredNotes.size,
            tasksList = filteredTasks,
            filesList = filteredFiles,
            meetingsList = filteredMeetings,
            notesList = filteredNotes
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportPreviewState())

    fun selectReportType(type: ReportType) {
        _selectedType.value = type
        _generatedPdfFile.value = null
    }

    fun generatePdf(onSuccess: (File) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isGenerating.value = true
            val profile = profileRepo.getProfileOnce()
            val state = reportState.value
            val data = ReportData(
                reportType = state.reportType.name.lowercase().replaceFirstChar { it.uppercase() },
                periodString = state.periodLabel,
                tasks = state.tasksList,
                files = state.filesList,
                meetings = state.meetingsList,
                notes = state.notesList
            )

            val file = PdfReportGenerator.generatePdfReport(
                context = appContext,
                profile = profile,
                reportData = data
            )

            _isGenerating.value = false
            if (file != null) {
                _generatedPdfFile.value = file
                onSuccess(file)
            } else {
                onError("Failed to generate PDF report")
            }
        }
    }
}
