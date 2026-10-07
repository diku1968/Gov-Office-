package com.example.data.repository

import com.example.data.local.dao.AttachmentDao
import com.example.data.local.dao.FileDao
import com.example.data.local.dao.FileHistoryDao
import com.example.data.local.entity.AttachmentEntity
import com.example.data.local.entity.FileEntity
import com.example.data.local.entity.FileHistoryEntity
import kotlinx.coroutines.flow.Flow

class FileRepository(
    private val fileDao: FileDao,
    private val historyDao: FileHistoryDao,
    private val attachmentDao: AttachmentDao
) {
    val allFiles: Flow<List<FileEntity>> = fileDao.getAllFiles()
    val pendingFiles: Flow<List<FileEntity>> = fileDao.getPendingFiles()

    fun getRecentFiles(limit: Int = 5): Flow<List<FileEntity>> = fileDao.getRecentFiles(limit)

    fun searchFiles(query: String): Flow<List<FileEntity>> = fileDao.searchFiles(query)

    suspend fun getFileById(id: Long): FileEntity? = fileDao.getFileById(id)

    fun getFileFlow(id: Long): Flow<FileEntity?> = fileDao.getFileFlow(id)

    fun getHistoryForFile(fileId: Long): Flow<List<FileHistoryEntity>> =
        historyDao.getHistoryForFile(fileId)

    fun getAttachmentsForFile(fileId: Long): Flow<List<AttachmentEntity>> =
        attachmentDao.getAttachmentsForFile(fileId)

    suspend fun createFile(
        file: FileEntity,
        initialSection: String = "",
        initialPerson: String = "",
        remarks: String = "File created / registered"
    ): Long {
        val fileId = fileDao.insertFile(file)
        // Record initial history movement
        historyDao.insertHistory(
            FileHistoryEntity(
                fileId = fileId,
                date = file.receivedDate,
                status = file.currentStatus,
                section = if (initialSection.isNotEmpty()) initialSection else file.currentSection,
                person = if (initialPerson.isNotEmpty()) initialPerson else file.currentPerson,
                actionTaken = file.nextAction,
                remarks = remarks
            )
        )
        return fileId
    }

    suspend fun updateFileDetails(file: FileEntity) {
        fileDao.updateFile(file.copy(modifiedDate = System.currentTimeMillis()))
    }

    suspend fun updateFileMovement(
        fileId: Long,
        newStatus: String,
        newSection: String,
        newPerson: String,
        nextAction: String,
        remarks: String
    ) {
        val existing = fileDao.getFileById(fileId) ?: return
        val updatedFile = existing.copy(
            currentStatus = newStatus,
            currentSection = if (newSection.isNotEmpty()) newSection else existing.currentSection,
            currentPerson = if (newPerson.isNotEmpty()) newPerson else existing.currentPerson,
            nextAction = nextAction,
            modifiedDate = System.currentTimeMillis()
        )
        fileDao.updateFile(updatedFile)

        // Always create a brand new immutable history record
        historyDao.insertHistory(
            FileHistoryEntity(
                fileId = fileId,
                date = System.currentTimeMillis(),
                status = newStatus,
                section = newSection,
                person = newPerson,
                actionTaken = nextAction,
                remarks = remarks
            )
        )
    }

    suspend fun addAttachment(attachment: AttachmentEntity): Long =
        attachmentDao.insertAttachment(attachment)

    suspend fun deleteAttachment(attachment: AttachmentEntity) =
        attachmentDao.deleteAttachment(attachment)

    suspend fun deleteFile(file: FileEntity) {
        historyDao.deleteHistoryForFile(file.id)
        fileDao.deleteFile(file)
    }

    suspend fun getAllFilesSnapshot(): List<FileEntity> = fileDao.getAllFilesSnapshot()

    suspend fun getAllHistorySnapshot(): List<FileHistoryEntity> = historyDao.getAllHistorySnapshot()

    suspend fun insertAllFiles(files: List<FileEntity>) = fileDao.insertAll(files)

    suspend fun insertAllHistory(history: List<FileHistoryEntity>) = historyDao.insertAll(history)
}
