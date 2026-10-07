package com.example.ui.voicenotes

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.entity.VoiceNoteEntity
import com.example.ui.components.EmptyStateView
import com.example.util.DateUtils

@Composable
fun VoiceNoteScreen(
    viewModel: VoiceNoteViewModel
) {
    val context = LocalContext.current
    val voiceNotes by viewModel.voiceNotes.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val isListeningSpeech by viewModel.isListeningSpeech.collectAsStateWithLifecycle()
    val currentlyPlayingId by viewModel.currentlyPlayingId.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val speechText by viewModel.speechText.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    var showSaveRecordingDialog by remember { mutableStateOf(false) }
    var recordingTitle by remember { mutableStateOf("") }
    var recordingNotes by remember { mutableStateOf("") }
    var noteToDelete by remember { mutableStateOf<VoiceNoteEntity?>(null) }

    // Audio permission launcher
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startRecording()
        } else {
            Toast.makeText(context, context.getString(R.string.permission_mic_required), Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Recording Station Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recording_station_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isRecording) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isRecording) "Recording in progress..." else "Voice Recorder & Speech-to-Text",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isRecording) Color(0xFFDC2626) else MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!isRecording) {
                            Button(
                                onClick = {
                                    val hasMic = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (hasMic) {
                                        viewModel.startRecording()
                                    } else {
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(64.dp)
                                    .testTag("btn_start_recording"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = "Start Recording", modifier = Modifier.size(28.dp))
                            }
                        } else {
                            Button(
                                onClick = {
                                    showSaveRecordingDialog = true
                                },
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(64.dp)
                                    .testTag("btn_stop_recording"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = "Stop Recording", modifier = Modifier.size(28.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Speech recognition toggle button
                    FilledTonalButton(
                        onClick = {
                            if (!isListeningSpeech) {
                                viewModel.startSpeechRecognition("en")
                            } else {
                                viewModel.stopSpeechRecognition()
                            }
                        },
                        modifier = Modifier.testTag("btn_speech_to_text")
                    ) {
                        Text(
                            text = if (isListeningSpeech) "Listening... (Tap to stop)"
                            else "Speech-to-Text Transcribe"
                        )
                    }

                    if (speechText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Transcription: $speechText",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Recorded Voice Memos (${voiceNotes.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (voiceNotes.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Mic,
                    message = stringResource(R.string.empty_voice_notes),
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(voiceNotes, key = { it.id }) { item ->
                        VoiceNoteItemCard(
                            voiceNote = item,
                            isPlaying = currentlyPlayingId == item.id && isPlaying,
                            onPlayPause = { viewModel.playAudio(item) },
                            onDelete = { noteToDelete = item },
                            onConvertToTask = {
                                viewModel.convertToTask(item)
                                Toast.makeText(context, "Converted to Task", Toast.LENGTH_SHORT).show()
                            },
                            onConvertToNote = {
                                viewModel.convertToNote(item)
                                Toast.makeText(context, "Converted to Note", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }

    if (showSaveRecordingDialog) {
        AlertDialog(
            onDismissRequest = {
                viewModel.cancelRecording()
                showSaveRecordingDialog = false
            },
            title = { Text("Save Voice Note") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = recordingTitle,
                        onValueChange = { recordingTitle = it },
                        label = { Text("Title (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_voice_title")
                    )
                    OutlinedTextField(
                        value = recordingNotes,
                        onValueChange = { recordingNotes = it },
                        label = { Text("Notes / Remarks") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.stopRecording(recordingTitle.trim(), recordingNotes.trim())
                        recordingTitle = ""
                        recordingNotes = ""
                        showSaveRecordingDialog = false
                    },
                    modifier = Modifier.testTag("btn_save_voice_note")
                ) {
                    Text(stringResource(R.string.btn_save))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelRecording()
                        showSaveRecordingDialog = false
                    }
                ) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    if (noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text(stringResource(R.string.btn_delete)) },
            text = { Text(stringResource(R.string.dialog_delete_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        noteToDelete?.let { viewModel.deleteVoiceNote(it) }
                        noteToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.btn_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }
}

@Composable
fun VoiceNoteItemCard(
    voiceNote: VoiceNoteEntity,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onDelete: () -> Unit,
    onConvertToTask: () -> Unit,
    onConvertToNote: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("voice_note_card_${voiceNote.id}"),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier.testTag("btn_play_pause_${voiceNote.id}")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = voiceNote.title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${DateUtils.formatDateTime(voiceNote.createdDate)} • ${voiceNote.durationMillis / 1000}s",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(20.dp))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            if (voiceNote.transcription.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Speech: \"${voiceNote.transcription}\"",
                    fontSize = 12.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (voiceNote.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Notes: ${voiceNote.notes}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Conversion Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onConvertToTask,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("To Task", fontSize = 11.sp)
                }

                FilledTonalButton(
                    onClick = onConvertToNote,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Note, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("To Note", fontSize = 11.sp)
                }
            }
        }
    }
}
