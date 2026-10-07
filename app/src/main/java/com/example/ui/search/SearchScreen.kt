package com.example.ui.search

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.util.DateUtils

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onFileClick: (Long) -> Unit,
    onTaskClick: () -> Unit,
    onMeetingClick: () -> Unit,
    onNoteClick: () -> Unit
) {
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::setQuery,
                placeholder = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("global_search_input")
            )

            if (query.isBlank()) {
                EmptyStateView(
                    icon = Icons.Default.Search,
                    message = "Type above to search across all tasks, files, meetings, and notes.",
                    modifier = Modifier.weight(1f)
                )
            } else if (searchResults.totalCount == 0) {
                EmptyStateView(
                    icon = Icons.Default.Search,
                    message = stringResource(R.string.search_no_results),
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Files Group
                    if (searchResults.files.isNotEmpty()) {
                        item {
                            CategoryHeader(title = "Files (${searchResults.files.size})")
                        }
                        items(searchResults.files) { file ->
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onFileClick(file.id) },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "File #${file.fileNumber}: ${file.subject}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = "Section: ${file.currentSection.ifBlank { "General" }}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                    StatusBadge(status = file.currentStatus)
                                }
                            }
                        }
                    }

                    // Tasks Group
                    if (searchResults.tasks.isNotEmpty()) {
                        item {
                            CategoryHeader(title = "Tasks (${searchResults.tasks.size})")
                        }
                        items(searchResults.tasks) { task ->
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onTaskClick() },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = task.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = "Due: ${DateUtils.formatDate(task.dueDate)} • ${task.status}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                            }
                        }
                    }

                    // Meetings Group
                    if (searchResults.meetings.isNotEmpty()) {
                        item {
                            CategoryHeader(title = "Meetings (${searchResults.meetings.size})")
                        }
                        items(searchResults.meetings) { meeting ->
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onMeetingClick() },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Event, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF7C3AED))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = meeting.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = "${DateUtils.formatDate(meeting.date)} @ ${meeting.location.ifBlank { "Office" }}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                }
                            }
                        }
                    }

                    // Notes Group
                    if (searchResults.notes.isNotEmpty()) {
                        item {
                            CategoryHeader(title = "Notes (${searchResults.notes.size})")
                        }
                        items(searchResults.notes) { note ->
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNoteClick() },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Note, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFFB45309))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = note.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = note.content.take(60), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}
