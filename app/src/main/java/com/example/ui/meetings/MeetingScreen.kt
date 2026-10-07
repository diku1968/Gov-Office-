package com.example.ui.meetings

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.entity.MeetingEntity
import com.example.ui.components.EmptyStateView
import com.example.util.DateUtils
import java.util.Calendar

@Composable
fun MeetingScreen(
    viewModel: MeetingViewModel
) {
    val meetings by viewModel.meetings.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var meetingToEdit by remember { mutableStateOf<MeetingEntity?>(null) }
    var meetingToDelete by remember { mutableStateOf<MeetingEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    meetingToEdit = null
                    showAddEditDialog = true
                },
                modifier = Modifier.testTag("fab_add_meeting"),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_meeting),
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
                placeholder = { Text("Search meetings by title, agenda, location...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("meeting_search_input")
            )

            if (meetings.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Event,
                    message = stringResource(R.string.empty_meetings),
                    actionLabel = stringResource(R.string.add_meeting),
                    onActionClick = {
                        meetingToEdit = null
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
                    items(meetings, key = { it.id }) { meeting ->
                        MeetingItemCard(
                            meeting = meeting,
                            onEdit = {
                                meetingToEdit = meeting
                                showAddEditDialog = true
                            },
                            onDelete = { meetingToDelete = meeting }
                        )
                    }
                }
            }
        }
    }

    if (showAddEditDialog) {
        AddEditMeetingDialog(
            meetingToEdit = meetingToEdit,
            onDismiss = { showAddEditDialog = false },
            onSave = { title, date, start, end, loc, part, agenda, disc, dec, action, notes ->
                viewModel.saveMeeting(
                    id = meetingToEdit?.id ?: 0L,
                    title = title,
                    date = date,
                    startTime = start,
                    endTime = end,
                    location = loc,
                    participants = part,
                    agenda = agenda,
                    discussion = disc,
                    decisions = dec,
                    actionPoints = action,
                    notes = notes
                )
                showAddEditDialog = false
            }
        )
    }

    if (meetingToDelete != null) {
        AlertDialog(
            onDismissRequest = { meetingToDelete = null },
            title = { Text(stringResource(R.string.btn_delete)) },
            text = { Text(stringResource(R.string.dialog_delete_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        meetingToDelete?.let { viewModel.deleteMeeting(it) }
                        meetingToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.btn_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { meetingToDelete = null }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

@Composable
fun MeetingItemCard(
    meeting: MeetingEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("meeting_card_${meeting.id}"),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = meeting.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.height(14.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${DateUtils.formatDate(meeting.date)} • ${meeting.startTime} - ${meeting.endTime}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.height(18.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.height(18.dp))
                    }
                }
            }

            if (meeting.location.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.height(14.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = meeting.location, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                }
            }

            if (meeting.participants.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.height(14.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Attendees: ${meeting.participants}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline, maxLines = 1)
                }
            }

            if (meeting.agenda.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Agenda: ${meeting.agenda}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            if (meeting.decisions.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Decisions: ${meeting.decisions}", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            }

            if (meeting.actionPoints.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Action Points: ${meeting.actionPoints}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun AddEditMeetingDialog(
    meetingToEdit: MeetingEntity?,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        date: Long,
        startTime: String,
        endTime: String,
        location: String,
        participants: String,
        agenda: String,
        discussion: String,
        decisions: String,
        actionPoints: String,
        notes: String
    ) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(meetingToEdit?.title ?: "") }
    var date by remember { mutableLongStateOf(meetingToEdit?.date ?: System.currentTimeMillis()) }
    var startTime by remember { mutableStateOf(meetingToEdit?.startTime ?: "10:00") }
    var endTime by remember { mutableStateOf(meetingToEdit?.endTime ?: "11:00") }
    var location by remember { mutableStateOf(meetingToEdit?.location ?: "") }
    var participants by remember { mutableStateOf(meetingToEdit?.participants ?: "") }
    var agenda by remember { mutableStateOf(meetingToEdit?.agenda ?: "") }
    var discussion by remember { mutableStateOf(meetingToEdit?.discussion ?: "") }
    var decisions by remember { mutableStateOf(meetingToEdit?.decisions ?: "") }
    var actionPoints by remember { mutableStateOf(meetingToEdit?.actionPoints ?: "") }
    var notes by remember { mutableStateOf(meetingToEdit?.notes ?: "") }

    var titleError by remember { mutableStateOf(false) }

    fun pickDate() {
        val cal = Calendar.getInstance().apply { timeInMillis = date }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply { set(year, month, dayOfMonth, 10, 0, 0) }
                date = newCal.timeInMillis
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun pickStartTime() {
        TimePickerDialog(context, { _, hour, minute ->
            startTime = String.format("%02d:%02d", hour, minute)
        }, 10, 0, true).show()
    }

    fun pickEndTime() {
        TimePickerDialog(context, { _, hour, minute ->
            endTime = String.format("%02d:%02d", hour, minute)
        }, 11, 0, true).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (meetingToEdit == null) stringResource(R.string.add_meeting) else stringResource(R.string.edit_meeting)) },
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
                    label = { Text(stringResource(R.string.meeting_title_label) + " *") },
                    isError = titleError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_meeting_title")
                )

                OutlinedButton(onClick = { pickDate() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Date: ${DateUtils.formatDate(date)}")
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { pickStartTime() }, modifier = Modifier.weight(1f)) {
                        Text("Start: $startTime")
                    }
                    OutlinedButton(onClick = { pickEndTime() }, modifier = Modifier.weight(1f)) {
                        Text("End: $endTime")
                    }
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text(stringResource(R.string.meeting_location)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = participants,
                    onValueChange = { participants = it },
                    label = { Text(stringResource(R.string.meeting_participants)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = agenda,
                    onValueChange = { agenda = it },
                    label = { Text(stringResource(R.string.meeting_agenda)) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = discussion,
                    onValueChange = { discussion = it },
                    label = { Text(stringResource(R.string.meeting_discussion)) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = decisions,
                    onValueChange = { decisions = it },
                    label = { Text(stringResource(R.string.meeting_decisions)) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = actionPoints,
                    onValueChange = { actionPoints = it },
                    label = { Text(stringResource(R.string.meeting_action_points)) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.meeting_notes)) },
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
                            date,
                            startTime,
                            endTime,
                            location.trim(),
                            participants.trim(),
                            agenda.trim(),
                            discussion.trim(),
                            decisions.trim(),
                            actionPoints.trim(),
                            notes.trim()
                        )
                    }
                },
                modifier = Modifier.testTag("dialog_save_meeting_button")
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
