package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.FileHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FileHistoryDao {
    @Query("SELECT * FROM file_history WHERE fileId = :fileId ORDER BY date DESC")
    fun getHistoryForFile(fileId: Long): Flow<List<FileHistoryEntity>>

    @Query("SELECT * FROM file_history ORDER BY date DESC")
    fun getAllHistory(): Flow<List<FileHistoryEntity>>

    @Query("SELECT * FROM file_history")
    suspend fun getAllHistorySnapshot(): List<FileHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: FileHistoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(historyList: List<FileHistoryEntity>)

    @Query("DELETE FROM file_history WHERE fileId = :fileId")
    suspend fun deleteHistoryForFile(fileId: Long)
}
