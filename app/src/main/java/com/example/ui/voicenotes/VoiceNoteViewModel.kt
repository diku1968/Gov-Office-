package com.example.ui.voicenotes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.MeetingEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.VoiceNoteEntity
import com.example.data.repository.MeetingRepository
import com.example.data.repository.NoteRepository
import com.example.data.repository.TaskRepository
import com.example.data.repository.VoiceNoteRepository
import com.example.util.AudioPlayerHelper
import com.example.util.AudioRecorderHelper
import com.example.util.SpeechRecognizerHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class VoiceNoteViewModel(
    private val voiceRepo: VoiceNoteRepository,
    private val taskRepo: TaskRepository,
    private val noteRepo: NoteRepository,
    private val meetingRepo: MeetingRepository,
    private val appContext: Context
) : ViewModel() {

    val voiceNotes: StateFlow<List<VoiceNoteEntity>> = voiceRepo.allVoiceNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val recorderHelper = AudioRecorderHelper(appContext)
    private val playerHelper = AudioPlayerHelper()
    private val speechHelper = SpeechRecognizerHelper(appContext)

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isListeningSpeech = MutableStateFlow(false)
    val isListeningSpeech: StateFlow<Boolean> = _isListeningSpeech.asStateFlow()

    private val _currentlyPlayingId = MutableStateFlow<Long?>(null)
    val currentlyPlayingId: StateFlow<Long?> = _currentlyPlayingId.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _speechText = MutableStateFlow("")
    val speechText: StateFlow<String> = _speechText.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var currentRecordedFile: File? = null

    fun startRecording() {
        val file = recorderHelper.startRecording()
        if (file != null) {
            currentRecordedFile = file
            _isRecording.value = true
            _errorMessage.value = null
        } else {
            _errorMessage.value = "Failed to start recording. Please grant microphone permission."
        }
    }

    fun stopRecording(title: String, notes: String) {
        if (!_isRecording.value) return
        val duration = recorderHelper.stopRecording()
        _isRecording.value = false

        val file = currentRecordedFile
        if (file != null && file.exists()) {
            viewModelScope.launch {
                val voiceNote = VoiceNoteEntity(
                    title = if (title.isBlank()) "Voice Note #${System.currentTimeMillis() % 10000}" else title,
                    filePath = file.absolutePath,
                    durationMillis = duration,
                    transcription = _speechText.value,
                    notes = notes,
                    createdDate = System.currentTimeMillis()
                )
                voiceRepo.insertVoiceNote(voiceNote)
                _speechText.value = ""
                currentRecordedFile = null
            }
        }
    }

    fun cancelRecording() {
        recorderHelper.cancelRecording()
        _isRecording.value = false
        currentRecordedFile = null
        _speechText.value = ""
    }

    fun startSpeechRecognition(languageCode: String = "en") {
        if (!speechHelper.isRecognitionAvailable()) {
            _errorMessage.value = "Speech Recognition is not available on this device."
            return
        }
        _isListeningSpeech.value = true
        speechHelper.startListening(
            languageCode = languageCode,
            onReady = {
                _isListeningSpeech.value = true
            },
            onResult = { result ->
                _isListeningSpeech.value = false
                _speechText.value = if (_speechText.value.isBlank()) result else "${_speechText.value} $result"
            },
            onError = { err ->
                _isListeningSpeech.value = false
                _errorMessage.value = err
            }
        )
    }

    fun stopSpeechRecognition() {
        speechHelper.stopListening()
        _isListeningSpeech.value = false
    }

    fun playAudio(voiceNote: VoiceNoteEntity) {
        if (_currentlyPlayingId.value == voiceNote.id && _isPlaying.value) {
            playerHelper.pauseAudio()
            _isPlaying.value = false
            return
        }

        if (_currentlyPlayingId.value == voiceNote.id && !_isPlaying.value) {
            playerHelper.resumeAudio()
            _isPlaying.value = true
            return
        }

        playerHelper.playAudio(
            filePath = voiceNote.filePath,
            onCompletion = {
                _isPlaying.value = false
                _currentlyPlayingId.value = null
            },
            onError = { msg ->
                _isPlaying.value = false
                _currentlyPlayingId.value = null
                _errorMessage.value = msg
            }
        )
        _isPlaying.value = true
        _currentlyPlayingId.value = voiceNote.id
    }

    fun stopAudio() {
        playerHelper.stopAudio()
        _isPlaying.value = false
        _currentlyPlayingId.value = null
    }

    fun deleteVoiceNote(voiceNote: VoiceNoteEntity) {
        if (_currentlyPlayingId.value == voiceNote.id) {
            stopAudio()
        }
        viewModelScope.launch {
            voiceRepo.deleteVoiceNote(voiceNote)
        }
    }

    fun convertToTask(voiceNote: VoiceNoteEntity) {
        viewModelScope.launch {
            val task = TaskEntity(
                title = voiceNote.title,
                description = if (voiceNote.transcription.isNotBlank()) voiceNote.transcription else voiceNote.notes,
                category = "Voice Memos",
                priority = "Medium",
                dueDate = System.currentTimeMillis() + 86400000L, // Tomorrow
                notes = "Converted from voice note recorded on ${voiceNote.createdDate}"
            )
            taskRepo.insertTask(task)
        }
    }

    fun convertToNote(voiceNote: VoiceNoteEntity) {
        viewModelScope.launch {
            val content = buildString {
                if (voiceNote.transcription.isNotBlank()) {
                    append("Transcription:\n${voiceNote.transcription}\n\n")
                }
                if (voiceNote.notes.isNotBlank()) {
                    append("Notes:\n${voiceNote.notes}")
                }
            }.ifBlank { "Voice note memo" }

            val note = NoteEntity(
                title = voiceNote.title,
                content = content,
                category = "Audio Memos"
            )
            noteRepo.insertNote(note)
        }
    }

    fun convertToActionPoint(voiceNote: VoiceNoteEntity, meetingId: Long) {
        viewModelScope.launch {
            val meeting = meetingRepo.getMeetingById(meetingId) ?: return@launch
            val actionText = if (voiceNote.transcription.isNotBlank()) voiceNote.transcription else voiceNote.title
            val updatedActionPoints = if (meeting.actionPoints.isBlank()) {
                "• $actionText"
            } else {
                "${meeting.actionPoints}\n• $actionText"
            }
            meetingRepo.updateMeeting(meeting.copy(actionPoints = updatedActionPoints))
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        recorderHelper.cancelRecording()
        playerHelper.stopAudio()
        speechHelper.stopListening()
    }
}
