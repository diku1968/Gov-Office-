package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.MeetingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeetingDao {
    @Query("SELECT * FROM meetings ORDER BY date ASC, startTime ASC")
    fun getAllMeetings(): Flow<List<MeetingEntity>>

    @Query("SELECT * FROM meetings WHERE id = :id LIMIT 1")
    suspend fun getMeetingById(id: Long): MeetingEntity?

    @Query("SELECT * FROM meetings WHERE id = :id LIMIT 1")
    fun getMeetingFlow(id: Long): Flow<MeetingEntity?>

    @Query("SELECT * FROM meetings WHERE date >= :startOfDay AND date <= :endOfDay ORDER BY startTime ASC")
    fun getTodayMeetings(startOfDay: Long, endOfDay: Long): Flow<List<MeetingEntity>>

    @Query("SELECT * FROM meetings WHERE date >= :startOfDay ORDER BY date ASC, startTime ASC")
    fun getUpcomingMeetings(startOfDay: Long): Flow<List<MeetingEntity>>

    @Query("SELECT * FROM meetings WHERE title LIKE '%' || :query || '%' OR agenda LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%'")
    fun searchMeetings(query: String): Flow<List<MeetingEntity>>

    @Query("SELECT * FROM meetings")
    suspend fun getAllMeetingsSnapshot(): List<MeetingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeeting(meeting: MeetingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(meetings: List<MeetingEntity>)

    @Update
    suspend fun updateMeeting(meeting: MeetingEntity)

    @Delete
    suspend fun deleteMeeting(meeting: MeetingEntity)

    @Query("DELETE FROM meetings WHERE id = :id")
    suspend fun deleteMeetingById(id: Long)
}
