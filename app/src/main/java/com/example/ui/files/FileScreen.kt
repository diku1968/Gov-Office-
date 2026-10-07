package com.example.ui.files

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.entity.FileEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.util.DateUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileScreen(
    viewModel: FileViewModel,
    onFileSelected: (Long) -> Unit
) {
    val context = LocalContext.current
    val files by viewModel.files.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    val statusOptions = listOf(
        "ALL", "Received", "Under Process", "Forwarded", "Pending", "Approved", "Rejected", "Closed"
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.testTag("fab_add_file"),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_file),
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
                placeholder = { Text("Search files by number, subject, section...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("file_search_input")
            )

            // Status Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(statusOptions) { status ->
                    FilterChip(
                        selected = statusFilter == status,
                        onClick = { viewModel.onStatusFilterChanged(status) },
                        label = { Text(status) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (files.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Description,
                    message = stringResource(R.string.empty_files),
                    actionLabel = stringResource(R.string.add_file),
                    onActionClick = { showAddDialog = true },
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(files, key = { it.id }) { file ->
                        FileListItemCard(file = file, onClick = { onFileSelected(file.id) })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddFileDialog(
            onDismiss = { showAddDialog = false },
            onSave = { fileNo, subject, desc, recvDate, status, section, person, nextAction, priority, due, notes ->
                viewModel.createFile(
                    fileNumber = fileNo,
                    subject = subject,
                    description = desc,
                    receivedDate = recvDate,
                    currentStatus = status,
                    currentSection = section,
                    currentPerson = person,
                    nextAction = nextAction,
                    priority = priority,
                    dueDate = due,
                    reminderDate = null,
                    notes = notes
                )
                showAddDialog = false
                val activity = context as? android.app.Activity
                com.example.util.InterstitialAdManager.triggerActionAd(activity)
            }
        )
    }
}

@Composable
fun FileListItemCard(file: FileEntity, onClick: () -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("file_card_${file.id}"),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "File #${file.fileNumber}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                StatusBadge(status = file.currentStatus)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = file.subject,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (file.currentSection.isNotBlank() || file.currentPerson.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Section: ${file.currentSection.ifBlank { "General" }} • Dealing Hand: ${file.currentPerson.ifBlank { "Office" }}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Due: ${DateUtils.formatDate(file.dueDate)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "Action: ${file.nextAction.ifBlank { "Processing" }}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFileDialog(
    onDismiss: () -> Unit,
    onSave: (
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
        notes: String
    ) -> Unit
) {
    val context = LocalContext.current
    var fileNumber by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var receivedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var currentStatus by remember { mutableStateOf("Received") }
    var currentSection by remember { mutableStateOf("") }
    var currentPerson by remember { mutableStateOf("") }
    var nextAction by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var dueDate by remember { mutableLongStateOf(System.currentTimeMillis() + (7 * 86400000L)) }
    var notes by remember { mutableStateOf("") }

    var statusExpanded by remember { mutableStateOf(false) }
    val statuses = listOf("Received", "Under Process", "Forwarded", "Pending", "Approved", "Rejected", "Closed")

    var priorityExpanded by remember { mutableStateOf(false) }
    val priorities = listOf("Low", "Medium", "High", "Urgent")

    var fileNumberError by remember { mutableStateOf(false) }
    var subjectError by remember { mutableStateOf(false) }

    fun pickDueDate() {
        val cal = Calendar.getInstance().apply { timeInMillis = dueDate }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply { set(year, month, dayOfMonth, 17, 0, 0) }
                dueDate = newCal.timeInMillis
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_file)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = fileNumber,
                    onValueChange = {
                        fileNumber = it
                        fileNumberError = false
                    },
                    label = { Text(stringResource(R.string.file_number) + " *") },
                    isError = fileNumberError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_file_number")
                )

                OutlinedTextField(
                    value = subject,
                    onValueChange = {
                        subject = it
                        subjectError = false
                    },
                    label = { Text(stringResource(R.string.file_subject) + " *") },
                    isError = subjectError,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_file_subject")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.file_description)) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = currentSection,
                    onValueChange = { currentSection = it },
                    label = { Text(stringResource(R.string.file_current_section)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = currentPerson,
                    onValueChange = { currentPerson = it },
                    label = { Text(stringResource(R.string.file_current_person)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = nextAction,
                    onValueChange = { nextAction = it },
                    label = { Text(stringResource(R.string.file_next_action)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Status dropdown
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded }
                ) {
                    OutlinedTextField(
                        value = currentStatus,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.file_current_status)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        statuses.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = {
                                    currentStatus = s
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }

                // Due Date Button
                OutlinedButton(
                    onClick = { pickDueDate() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Target Due Date: ${DateUtils.formatDate(dueDate)}")
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
                    if (fileNumber.isBlank()) {
                        fileNumberError = true
                    } else if (subject.isBlank()) {
                        subjectError = true
                    } else {
                        onSave(
                            fileNumber.trim(),
                            subject.trim(),
                            description.trim(),
                            receivedDate,
                            currentStatus,
                            currentSection.trim(),
                            currentPerson.trim(),
                            nextAction.trim(),
                            priority,
                            dueDate,
                            notes.trim()
                        )
                    }
                },
                modifier = Modifier.testTag("dialog_save_file_button")
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
