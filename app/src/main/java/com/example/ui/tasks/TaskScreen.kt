package com.example.ui.tasks

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.entity.TaskEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PriorityBadge
import com.example.ui.components.StatusBadge
import com.example.util.DateUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    viewModel: TaskViewModel,
    onNavigateBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var taskToDelete by remember { mutableStateOf<TaskEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    taskToEdit = null
                    showAddEditDialog = true
                },
                modifier = Modifier.testTag("fab_add_task"),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_task),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                placeholder = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("task_search_input")
            )

            // Filter Chips Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == TaskFilter.ALL,
                        onClick = { viewModel.onFilterSelected(TaskFilter.ALL) },
                        label = { Text(stringResource(R.string.filter_all)) }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == TaskFilter.TODAY,
                        onClick = { viewModel.onFilterSelected(TaskFilter.TODAY) },
                        label = { Text(stringResource(R.string.filter_today)) }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == TaskFilter.UPCOMING,
                        onClick = { viewModel.onFilterSelected(TaskFilter.UPCOMING) },
                        label = { Text(stringResource(R.string.filter_upcoming)) }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == TaskFilter.OVERDUE,
                        onClick = { viewModel.onFilterSelected(TaskFilter.OVERDUE) },
                        label = { Text(stringResource(R.string.filter_overdue)) }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == TaskFilter.HIGH_PRIORITY,
                        onClick = { viewModel.onFilterSelected(TaskFilter.HIGH_PRIORITY) },
                        label = { Text(stringResource(R.string.filter_high_priority)) }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == TaskFilter.COMPLETED,
                        onClick = { viewModel.onFilterSelected(TaskFilter.COMPLETED) },
                        label = { Text(stringResource(R.string.filter_completed)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (tasks.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Assignment,
                    message = stringResource(R.string.empty_tasks),
                    actionLabel = stringResource(R.string.add_task),
                    onActionClick = {
                        taskToEdit = null
                        showAddEditDialog = true
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        TaskItemCard(
                            task = task,
                            onToggleComplete = {
                                val willBeCompleted = task.status != "Completed"
                                viewModel.toggleTaskComplete(task)
                                if (willBeCompleted) {
                                    val activity = context as? android.app.Activity
                                    com.example.util.InterstitialAdManager.triggerActionAd(activity)
                                }
                            },
                            onEdit = {
                                taskToEdit = task
                                showAddEditDialog = true
                            },
                            onDelete = { taskToDelete = task }
                        )
                    }
                }
            }
        }
    }

    if (showAddEditDialog) {
        AddEditTaskDialog(
            taskToEdit = taskToEdit,
            onDismiss = { showAddEditDialog = false },
            onSave = { title, desc, category, priority, due, reminder, notes ->
                viewModel.saveTask(
                    id = taskToEdit?.id ?: 0L,
                    title = title,
                    description = desc,
                    category = category,
                    priority = priority,
                    dueDate = due,
                    reminderDateTime = reminder,
                    notes = notes
                )
                showAddEditDialog = false
                val activity = context as? android.app.Activity
                com.example.util.InterstitialAdManager.triggerActionAd(activity)
            }
        )
    }

    if (taskToDelete != null) {
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text(stringResource(R.string.btn_delete)) },
            text = { Text(stringResource(R.string.dialog_delete_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        taskToDelete?.let { viewModel.deleteTask(it) }
                        taskToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.btn_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

@Composable
fun TaskItemCard(
    task: TaskEntity,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isCompleted = task.status == "Completed"

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            IconButton(
                onClick = onToggleComplete,
                modifier = Modifier.testTag("task_check_${task.id}")
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = if (isCompleted) "Completed" else "Mark Complete",
                    tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    PriorityBadge(priority = task.priority)
                }

                if (task.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Due: ${DateUtils.formatDate(task.dueDate)} • ${task.category}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.height(18.dp))
                        }
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.height(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskDialog(
    taskToEdit: TaskEntity?,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String,
        category: String,
        priority: String,
        dueDate: Long,
        reminderDateTime: Long?,
        notes: String
    ) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var description by remember { mutableStateOf(taskToEdit?.description ?: "") }
    var category by remember { mutableStateOf(taskToEdit?.category ?: "General") }
    var priority by remember { mutableStateOf(taskToEdit?.priority ?: "Medium") }
    var dueDate by remember { mutableLongStateOf(taskToEdit?.dueDate ?: (System.currentTimeMillis() + 86400000L)) }
    var reminderDateTime by remember { mutableStateOf(taskToEdit?.reminderDateTime) }
    var notes by remember { mutableStateOf(taskToEdit?.notes ?: "") }

    var priorityExpanded by remember { mutableStateOf(false) }
    val priorityOptions = listOf("Low", "Medium", "High", "Urgent")

    var titleError by remember { mutableStateOf(false) }

    fun pickDueDate() {
        val cal = Calendar.getInstance().apply { timeInMillis = dueDate }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 17, 0, 0)
                }
                dueDate = newCal.timeInMillis
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun pickReminder() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        val rCal = Calendar.getInstance().apply {
                            set(year, month, dayOfMonth, hourOfDay, minute, 0)
                        }
                        reminderDateTime = rCal.timeInMillis
                    },
                    10,
                    0,
                    false
                ).show()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (taskToEdit == null) stringResource(R.string.add_task) else stringResource(R.string.edit_task)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = false
                    },
                    label = { Text(stringResource(R.string.task_title) + " *") },
                    isError = titleError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_task_title")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.task_description)) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text(stringResource(R.string.task_category)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Priority dropdown
                ExposedDropdownMenuBox(
                    expanded = priorityExpanded,
                    onExpandedChange = { priorityExpanded = !priorityExpanded }
                ) {
                    OutlinedTextField(
                        value = priority,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.task_priority)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = priorityExpanded,
                        onDismissRequest = { priorityExpanded = false }
                    ) {
                        priorityOptions.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p) },
                                onClick = {
                                    priority = p
                                    priorityExpanded = false
                                }
                            )
                        }
                    }
                }

                // Due Date Picker Button
                OutlinedButton(
                    onClick = { pickDueDate() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Due: ${DateUtils.formatDate(dueDate)}")
                }

                // Reminder Picker Button
                OutlinedButton(
                    onClick = { pickReminder() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (reminderDateTime != null) "Reminder: ${DateUtils.formatDateTime(reminderDateTime!!)}"
                        else "Set Reminder"
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.task_notes)) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                    } else {
                        onSave(
                            title.trim(),
                            description.trim(),
                            category.trim(),
                            priority,
                            dueDate,
                            reminderDateTime,
                            notes.trim()
                        )
                    }
                },
                modifier = Modifier.testTag("dialog_save_task_button")
            ) {
                Text(stringResource(R.string.btn_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}
