package com.example.ui.meetings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.MeetingEntity
import com.example.data.repository.MeetingRepository
import com.example.util.DateUtils
import com.example.util.ReminderWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MeetingViewModel(
    private val meetingRepo: MeetingRepository,
    private val appContext: Context
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val meetings: StateFlow<List<MeetingEntity>> = combine(
        meetingRepo.allMeetings,
        _searchQuery
    ) { all, query ->
        if (query.isBlank()) {
            all
        } else {
            all.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.agenda.contains(query, ignoreCase = true) ||
                        it.location.contains(query, ignoreCase = true) ||
                        it.participants.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun saveMeeting(
        id: Long = 0,
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
    ) {
        viewModelScope.launch {
            val meeting = MeetingEntity(
                id = id,
                title = title,
                date = date,
                startTime = startTime,
                endTime = endTime,
                location = location,
                participants = participants,
                agenda = agenda,
                discussion = discussion,
                decisions = decisions,
                actionPoints = actionPoints,
                notes = notes,
                reminderDateTime = date - (15 * 60 * 1000) // 15 minutes before meeting
            )

            val meetingId = if (id == 0L) {
                meetingRepo.insertMeeting(meeting)
            } else {
                meetingRepo.updateMeeting(meeting)
                id
            }

            // Schedule reminder 15 minutes before meeting
            val reminderTime = date - (15 * 60 * 1000)
            if (reminderTime > System.currentTimeMillis()) {
                ReminderWorker.scheduleReminder(
                    context = appContext,
                    tag = "meeting_$meetingId",
                    notificationId = (meetingId + 20000).toInt(),
                    title = "Upcoming Meeting: $title",
                    message = "Time: $startTime - $endTime | Venue: ${location.ifBlank { "Office" }}",
                    type = "Meeting",
                    targetTimeMillis = reminderTime
                )
            }
        }
    }

    fun deleteMeeting(meeting: MeetingEntity) {
        viewModelScope.launch {
            ReminderWorker.cancelReminder(appContext, "meeting_${meeting.id}")
            meetingRepo.deleteMeeting(meeting)
        }
    }
}
