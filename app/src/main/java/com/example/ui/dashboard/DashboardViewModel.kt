package com.example.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.FileEntity
import com.example.data.local.entity.MeetingEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.repository.FileRepository
import com.example.data.repository.MeetingRepository
import com.example.data.repository.TaskRepository
import com.example.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

sealed interface ScheduleItem {
    val timeMillis: Long

    data class TaskItem(val task: TaskEntity) : ScheduleItem {
        override val timeMillis: Long = task.dueDate
    }

    data class MeetingItem(val meeting: MeetingEntity) : ScheduleItem {
        override val timeMillis: Long = meeting.date
    }

    data class FileActionItem(val file: FileEntity) : ScheduleItem {
        override val timeMillis: Long = file.dueDate
    }
}

data class DashboardUiState(
    val todayTasksCount: Int = 0,
    val pendingTasksCount: Int = 0,
    val overdueTasksCount: Int = 0,
    val pendingFilesCount: Int = 0,
    val todayMeetingsCount: Int = 0,
    val scheduleItems: List<ScheduleItem> = emptyList(),
    val recentFiles: List<FileEntity> = emptyList()
)

class DashboardViewModel(
    taskRepo: TaskRepository,
    fileRepo: FileRepository,
    meetingRepo: MeetingRepository
) : ViewModel() {

    private val now = System.currentTimeMillis()
    private val startOfDay = DateUtils.getStartOfDay(now)
    private val endOfDay = DateUtils.getEndOfDay(now)

    val uiState: StateFlow<DashboardUiState> = combine(
        taskRepo.allTasks,
        fileRepo.pendingFiles,
        meetingRepo.getTodayMeetings(startOfDay, endOfDay),
        fileRepo.getRecentFiles(6)
    ) { allTasks, pendingFiles, todayMeetings, recentFiles ->
        val todayTasks = allTasks.filter { it.dueDate in startOfDay..endOfDay }
        val pendingTasks = allTasks.filter { it.status != "Completed" && it.status != "Cancelled" }
        val overdueTasks = allTasks.filter { it.status != "Completed" && it.status != "Cancelled" && it.dueDate < startOfDay }

        val schedule = mutableListOf<ScheduleItem>()
        todayTasks.forEach { schedule.add(ScheduleItem.TaskItem(it)) }
        todayMeetings.forEach { schedule.add(ScheduleItem.MeetingItem(it)) }
        pendingFiles.filter { it.dueDate in startOfDay..endOfDay }.forEach {
            schedule.add(ScheduleItem.FileActionItem(it))
        }
        schedule.sortBy { it.timeMillis }

        DashboardUiState(
            todayTasksCount = todayTasks.size,
            pendingTasksCount = pendingTasks.size,
            overdueTasksCount = overdueTasks.size,
            pendingFilesCount = pendingFiles.size,
            todayMeetingsCount = todayMeetings.size,
            scheduleItems = schedule,
            recentFiles = recentFiles
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )
}
