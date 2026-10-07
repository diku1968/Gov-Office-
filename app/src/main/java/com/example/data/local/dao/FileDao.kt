package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.FileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {
    @Query("SELECT * FROM files ORDER BY modifiedDate DESC")
    fun getAllFiles(): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE id = :id LIMIT 1")
    suspend fun getFileById(id: Long): FileEntity?

    @Query("SELECT * FROM files WHERE id = :id LIMIT 1")
    fun getFileFlow(id: Long): Flow<FileEntity?>

    @Query("SELECT * FROM files WHERE currentStatus != 'Approved' AND currentStatus != 'Closed' AND currentStatus != 'Rejected' ORDER BY dueDate ASC")
    fun getPendingFiles(): Flow<List<FileEntity>>

    @Query("SELECT * FROM files ORDER BY modifiedDate DESC LIMIT :limit")
    fun getRecentFiles(limit: Int = 5): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE fileNumber LIKE '%' || :query || '%' OR subject LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchFiles(query: String): Flow<List<FileEntity>>

    @Query("SELECT * FROM files")
    suspend fun getAllFilesSnapshot(): List<FileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(files: List<FileEntity>)

    @Update
    suspend fun updateFile(file: FileEntity)

    @Delete
    suspend fun deleteFile(file: FileEntity)

    @Query("DELETE FROM files WHERE id = :id")
    suspend fun deleteFileById(id: Long)
}
