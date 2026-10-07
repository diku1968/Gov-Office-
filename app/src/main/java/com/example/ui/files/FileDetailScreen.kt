package com.example.ui.files

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.entity.AttachmentEntity
import com.example.data.local.entity.FileHistoryEntity
import com.example.ui.components.StatusBadge
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileDetailScreen(
    fileId: Long,
    viewModel: FileViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(fileId) {
        viewModel.setSelectedFileId(fileId)
    }

    val file by viewModel.currentFile.collectAsStateWithLifecycle()
    val historyList by viewModel.currentFileHistory.collectAsStateWithLifecycle()
    val attachments by viewModel.currentFileAttachments.collectAsStateWithLifecycle()

    var showMovementDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Media / Document picker for attachments
    val attachmentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}

            val fileName = it.lastPathSegment ?: "document_${System.currentTimeMillis()}"
            val mimeType = context.contentResolver.getType(it) ?: "application/octet-stream"
            viewModel.addAttachment(
                fileId = fileId,
                fileName = fileName,
                uri = it.toString(),
                mimeType = mimeType,
                fileSize = 0L
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(file?.let { "File #${it.fileNumber}" } ?: "File Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    file?.let { f ->
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete File")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (file == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("File not found or deleted")
            }
        } else {
            val current = file!!

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // File Overview Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "File #${current.fileNumber}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                StatusBadge(status = current.currentStatus)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = current.subject,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            if (current.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = current.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Received: ${DateUtils.formatDate(current.receivedDate)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = "Target Due: ${DateUtils.formatDate(current.dueDate)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Current Section: ${current.currentSection.ifBlank { "N/A" }}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Current Person / Dealing Hand: ${current.currentPerson.ifBlank { "N/A" }}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Next Action Required: ${current.nextAction.ifBlank { "Pending Review" }}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (current.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Notes: ${current.notes}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Action Bar: Update Movement Button
                item {
                    Button(
                        onClick = { showMovementDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_update_movement"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.SyncAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.file_add_movement))
                    }
                }

                // Attachments Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${stringResource(R.string.file_attachments)} (${attachments.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        FilledTonalButton(
                            onClick = {
                                attachmentPicker.launch(arrayOf("*/*"))
                            },
                            modifier = Modifier.testTag("btn_add_attachment")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.file_add_attachment), fontSize = 12.sp)
                        }
                    }
                }

                if (attachments.isEmpty()) {
                    item {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "No attachments uploaded yet. You can attach photos or documents.",
                                modifier = Modifier.padding(12.dp),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(attachments, key = { it.id }) { attachment ->
                        AttachmentRow(
                            attachment = attachment,
                            onOpen = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse(attachment.uri)
                                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            onShare = {
                                try {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = attachment.mimeType.ifBlank { "*/*" }
                                        putExtra(Intent.EXTRA_STREAM, Uri.parse(attachment.uri))
                                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Attachment"))
                                } catch (_: Exception) {}
                            },
                            onDelete = { viewModel.deleteAttachment(attachment) }
                        )
                    }
                }

                // File Movement History Timeline (Immutable History)
                item {
                    Text(
                        text = stringResource(R.string.file_history_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (historyList.isEmpty()) {
                    item {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.file_history_empty),
                                modifier = Modifier.padding(12.dp),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(historyList, key = { it.id }) { history ->
                        FileHistoryTimelineItem(history = history)
                    }
                }
            }
        }
    }

    if (showMovementDialog && file != null) {
        UpdateMovementDialog(
            currentStatus = file!!.currentStatus,
            currentSection = file!!.currentSection,
            currentPerson = file!!.currentPerson,
            onDismiss = { showMovementDialog = false },
            onUpdate = { newStatus, newSection, newPerson, nextAction, remarks ->
                viewModel.updateFileMovement(
                    fileId = fileId,
                    newStatus = newStatus,
                    newSection = newSection,
                    newPerson = newPerson,
                    nextAction = nextAction,
                    remarks = remarks
                )
                showMovementDialog = false
                val activity = context as? android.app.Activity
                com.example.util.InterstitialAdManager.triggerActionAd(activity)
            }
        )
    }

    if (showDeleteConfirm && file != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.btn_delete)) },
            text = { Text(stringResource(R.string.dialog_delete_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteFile(file!!)
                        showDeleteConfirm = false
                        onNavigateBack()
                    }
                ) {
                    Text(stringResource(R.string.btn_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

@Composable
fun AttachmentRow(
    attachment: AttachmentEntity,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AttachFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attachment.fileName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    maxLines = 1
                )
                Text(
                    text = "Added: ${DateUtils.formatDate(attachment.addedDate)}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            IconButton(onClick = onOpen) {
                Icon(Icons.Default.OpenInNew, contentDescription = "Open", modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onShare) {
                Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun FileHistoryTimelineItem(history: FileHistoryEntity) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(60.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        OutlinedCard(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = DateUtils.formatDate(history.date),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    StatusBadge(status = history.status)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Section: ${history.section.ifBlank { "General Office" }}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                if (history.person.isNotBlank()) {
                    Text(
                        text = "Officer/Person: ${history.person}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (history.actionTaken.isNotBlank()) {
                    Text(
                        text = "Action: ${history.actionTaken}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                if (history.remarks.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Remarks: ${history.remarks}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateMovementDialog(
    currentStatus: String,
    currentSection: String,
    currentPerson: String,
    onDismiss: () -> Unit,
    onUpdate: (
        newStatus: String,
        newSection: String,
        newPerson: String,
        nextAction: String,
        remarks: String
    ) -> Unit
) {
    var status by remember { mutableStateOf(currentStatus) }
    var section by remember { mutableStateOf(currentSection) }
    var person by remember { mutableStateOf(currentPerson) }
    var nextAction by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }

    var statusExpanded by remember { mutableStateOf(false) }
    val statuses = listOf("Received", "Under Process", "Forwarded", "Pending", "Approved", "Rejected", "Closed")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.file_add_movement)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Status selection
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded }
                ) {
                    OutlinedTextField(
                        value = status,
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
                                    status = s
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = section,
                    onValueChange = { section = it },
                    label = { Text(stringResource(R.string.file_movement_section)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = person,
                    onValueChange = { person = it },
                    label = { Text(stringResource(R.string.file_movement_person)) },
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

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text(stringResource(R.string.file_movement_action)) },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdate(status, section.trim(), person.trim(), nextAction.trim(), remarks.trim())
                },
                modifier = Modifier.testTag("dialog_confirm_movement_button")
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
