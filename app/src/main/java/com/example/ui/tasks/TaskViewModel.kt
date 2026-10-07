package com.example.ui.tasks

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.TaskEntity
import com.example.data.repository.TaskRepository
import com.example.util.DateUtils
import com.example.util.ReminderWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TaskFilter {
    ALL, TODAY, UPCOMING, OVERDUE, COMPLETED, HIGH_PRIORITY
}

class TaskViewModel(
    private val taskRepo: TaskRepository,
    private val appContext: Context
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedFilter = MutableStateFlow(TaskFilter.ALL)
    val selectedFilter: StateFlow<TaskFilter> = _selectedFilter

    val tasks: StateFlow<List<TaskEntity>> = combine(
        taskRepo.allTasks,
        _searchQuery,
        _selectedFilter
    ) { allTasks, query, filter ->
        val now = System.currentTimeMillis()
        val startOfToday = DateUtils.getStartOfDay(now)
        val endOfToday = DateUtils.getEndOfDay(now)

        allTasks.filter { task ->
            val matchesQuery = query.isBlank() ||
                    task.title.contains(query, ignoreCase = true) ||
                    task.description.contains(query, ignoreCase = true) ||
                    task.category.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                TaskFilter.ALL -> true
                TaskFilter.TODAY -> task.dueDate in startOfToday..endOfToday
                TaskFilter.UPCOMING -> task.dueDate > endOfToday && task.status != "Completed"
                TaskFilter.OVERDUE -> task.dueDate < startOfToday && task.status != "Completed"
                TaskFilter.COMPLETED -> task.status == "Completed"
                TaskFilter.HIGH_PRIORITY -> task.priority in listOf("High", "Urgent") && task.status != "Completed"
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterSelected(filter: TaskFilter) {
        _selectedFilter.value = filter
    }

    fun saveTask(
        id: Long = 0,
        title: String,
        description: String,
        category: String,
        priority: String,
        dueDate: Long,
        reminderDateTime: Long?,
        notes: String
    ) {
        viewModelScope.launch {
            val task = TaskEntity(
                id = id,
                title = title,
                description = description,
                category = category,
                priority = priority,
                status = "Pending",
                createdDate = System.currentTimeMillis(),
                dueDate = dueDate,
                reminderDateTime = reminderDateTime,
                notes = notes
            )
            val taskId = if (id == 0L) {
                taskRepo.insertTask(task)
            } else {
                taskRepo.updateTask(task)
                id
            }

            // Schedule reminder notification if requested
            reminderDateTime?.let { reminderTime ->
                if (reminderTime > System.currentTimeMillis()) {
                    ReminderWorker.scheduleReminder(
                        context = appContext,
                        tag = "task_$taskId",
                        notificationId = taskId.toInt(),
                        title = "Task Reminder: $title",
                        message = "Priority: $priority | Due: ${DateUtils.formatDate(dueDate)}",
                        type = "Task",
                        targetTimeMillis = reminderTime
                    )
                }
            }
        }
    }

    fun toggleTaskComplete(task: TaskEntity) {
        viewModelScope.launch {
            val newStatus = if (task.status == "Completed") "Pending" else "Completed"
            val completedDate = if (newStatus == "Completed") System.currentTimeMillis() else null
            taskRepo.updateTask(task.copy(status = newStatus, completedDate = completedDate))
            if (newStatus == "Completed") {
                ReminderWorker.cancelReminder(appContext, "task_${task.id}")
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            ReminderWorker.cancelReminder(appContext, "task_${task.id}")
            taskRepo.deleteTask(task)
        }
    }
}
