package com.example.data.repository

import com.example.data.local.dao.MeetingDao
import com.example.data.local.entity.MeetingEntity
import kotlinx.coroutines.flow.Flow

class MeetingRepository(private val meetingDao: MeetingDao) {
    val allMeetings: Flow<List<MeetingEntity>> = meetingDao.getAllMeetings()

    fun getTodayMeetings(startOfDay: Long, endOfDay: Long): Flow<List<MeetingEntity>> =
        meetingDao.getTodayMeetings(startOfDay, endOfDay)

    fun getUpcomingMeetings(startOfDay: Long): Flow<List<MeetingEntity>> =
        meetingDao.getUpcomingMeetings(startOfDay)

    fun searchMeetings(query: String): Flow<List<MeetingEntity>> =
        meetingDao.searchMeetings(query)

    suspend fun getMeetingById(id: Long): MeetingEntity? = meetingDao.getMeetingById(id)

    fun getMeetingFlow(id: Long): Flow<MeetingEntity?> = meetingDao.getMeetingFlow(id)

    suspend fun insertMeeting(meeting: MeetingEntity): Long = meetingDao.insertMeeting(meeting)

    suspend fun updateMeeting(meeting: MeetingEntity) = meetingDao.updateMeeting(meeting)

    suspend fun deleteMeeting(meeting: MeetingEntity) = meetingDao.deleteMeeting(meeting)

    suspend fun deleteMeetingById(id: Long) = meetingDao.deleteMeetingById(id)

    suspend fun getAllMeetingsSnapshot(): List<MeetingEntity> = meetingDao.getAllMeetingsSnapshot()

    suspend fun insertAll(meetings: List<MeetingEntity>) = meetingDao.insertAll(meetings)
}
