package com.example.util

import android.content.Context
import com.example.data.local.entity.FileEntity
import com.example.data.local.entity.FileHistoryEntity
import com.example.data.local.entity.MeetingEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserProfileEntity
import com.example.data.repository.FileRepository
import com.example.data.repository.MeetingRepository
import com.example.data.repository.NoteRepository
import com.example.data.repository.TaskRepository
import com.example.data.repository.UserProfileRepository
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

object BackupRestoreManager {

    suspend fun createJsonBackup(
        context: Context,
        userProfileRepo: UserProfileRepository,
        taskRepo: TaskRepository,
        fileRepo: FileRepository,
        meetingRepo: MeetingRepository,
        noteRepo: NoteRepository
    ): File? {
        return try {
            val root = JSONObject()
            root.put("version", 1)
            root.put("timestamp", System.currentTimeMillis())

            // 1. Profile
            val profile = userProfileRepo.getProfileOnce()
            if (profile != null) {
                val profileObj = JSONObject().apply {
                    put("employeeName", profile.employeeName)
                    put("department", profile.department)
                    put("designation", profile.designation)
                    put("office", profile.office)
                    put("employeeId", profile.employeeId)
                    put("languageCode", profile.languageCode)
                    put("themeMode", profile.themeMode)
                    put("notificationsEnabled", profile.notificationsEnabled)
                }
                root.put("profile", profileObj)
            }

            // 2. Tasks
            val tasks = taskRepo.getAllTasksSnapshot()
            val tasksArray = JSONArray()
            tasks.forEach { t ->
                val obj = JSONObject().apply {
                    put("title", t.title)
                    put("description", t.description)
                    put("category", t.category)
                    put("priority", t.priority)
                    put("status", t.status)
                    put("createdDate", t.createdDate)
                    put("dueDate", t.dueDate)
                    t.reminderDateTime?.let { put("reminderDateTime", it) }
                    t.completedDate?.let { put("completedDate", it) }
                    put("notes", t.notes)
                }
                tasksArray.put(obj)
            }
            root.put("tasks", tasksArray)

            // 3. Files
            val files = fileRepo.getAllFilesSnapshot()
            val filesArray = JSONArray()
            files.forEach { f ->
                val obj = JSONObject().apply {
                    put("fileNumber", f.fileNumber)
                    put("subject", f.subject)
                    put("description", f.description)
                    put("receivedDate", f.receivedDate)
                    put("currentStatus", f.currentStatus)
                    put("currentSection", f.currentSection)
                    put("currentPerson", f.currentPerson)
                    put("nextAction", f.nextAction)
                    put("priority", f.priority)
                    put("dueDate", f.dueDate)
                    f.reminderDate?.let { put("reminderDate", it) }
                    put("notes", f.notes)
                    put("createdDate", f.createdDate)
                    put("modifiedDate", f.modifiedDate)
                }
                filesArray.put(obj)
            }
            root.put("files", filesArray)

            // 4. File History
            val historyList = fileRepo.getAllHistorySnapshot()
            val historyArray = JSONArray()
            historyList.forEach { h ->
                val obj = JSONObject().apply {
                    put("fileId", h.fileId)
                    put("date", h.date)
                    put("status", h.status)
                    put("section", h.section)
                    put("person", h.person)
                    put("actionTaken", h.actionTaken)
                    put("remarks", h.remarks)
                }
                historyArray.put(obj)
            }
            root.put("file_history", historyArray)

            // 5. Meetings
            val meetings = meetingRepo.getAllMeetingsSnapshot()
            val meetingsArray = JSONArray()
            meetings.forEach { m ->
                val obj = JSONObject().apply {
                    put("title", m.title)
                    put("date", m.date)
                    put("startTime", m.startTime)
                    put("endTime", m.endTime)
                    put("location", m.location)
                    put("participants", m.participants)
                    put("agenda", m.agenda)
                    put("discussion", m.discussion)
                    put("decisions", m.decisions)
                    put("actionPoints", m.actionPoints)
                    put("notes", m.notes)
                }
                meetingsArray.put(obj)
            }
            root.put("meetings", meetingsArray)

            // 6. Notes
            val notes = noteRepo.getAllNotesSnapshot()
            val notesArray = JSONArray()
            notes.forEach { n ->
                val obj = JSONObject().apply {
                    put("title", n.title)
                    put("content", n.content)
                    put("category", n.category)
                    put("createdDate", n.createdDate)
                    put("modifiedDate", n.modifiedDate)
                    put("isPinned", n.isPinned)
                    n.relatedType?.let { put("relatedType", it) }
                    n.relatedId?.let { put("relatedId", it) }
                }
                notesArray.put(obj)
            }
            root.put("notes", notesArray)

            val backupDir = File(context.filesDir, "backups").apply {
                if (!exists()) mkdirs()
            }
            val backupFile = File(backupDir, "GovWork_Backup_${System.currentTimeMillis()}.json")
            FileOutputStream(backupFile).use { out ->
                out.write(root.toString(2).toByteArray(Charsets.UTF_8))
            }
            backupFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun restoreFromJson(
        jsonString: String,
        userProfileRepo: UserProfileRepository,
        taskRepo: TaskRepository,
        fileRepo: FileRepository,
        meetingRepo: MeetingRepository,
        noteRepo: NoteRepository
    ): Boolean {
        return try {
            val root = JSONObject(jsonString)

            // Profile
            if (root.has("profile")) {
                val p = root.getJSONObject("profile")
                val current = userProfileRepo.getProfileOnce() ?: UserProfileEntity()
                userProfileRepo.saveProfile(
                    current.copy(
                        employeeName = p.optString("employeeName", current.employeeName),
                        department = p.optString("department", current.department),
                        designation = p.optString("designation", current.designation),
                        office = p.optString("office", current.office),
                        employeeId = p.optString("employeeId", current.employeeId),
                        isProfileConfigured = true
                    )
                )
            }

            // Tasks (merge without duplicate titles on same date)
            if (root.has("tasks")) {
                val arr = root.getJSONArray("tasks")
                val existing = taskRepo.getAllTasksSnapshot().associateBy { it.title.trim().lowercase() + it.dueDate }
                val newTasks = mutableListOf<TaskEntity>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val title = obj.getString("title")
                    val dueDate = obj.getLong("dueDate")
                    val key = title.trim().lowercase() + dueDate
                    if (!existing.containsKey(key)) {
                        newTasks.add(
                            TaskEntity(
                                title = title,
                                description = obj.optString("description", ""),
                                category = obj.optString("category", "General"),
                                priority = obj.optString("priority", "Medium"),
                                status = obj.optString("status", "Pending"),
                                createdDate = obj.optLong("createdDate", System.currentTimeMillis()),
                                dueDate = dueDate,
                                reminderDateTime = if (obj.has("reminderDateTime")) obj.getLong("reminderDateTime") else null,
                                completedDate = if (obj.has("completedDate")) obj.getLong("completedDate") else null,
                                notes = obj.optString("notes", "")
                            )
                        )
                    }
                }
                if (newTasks.isNotEmpty()) {
                    taskRepo.insertAll(newTasks)
                }
            }

            // Files & History
            if (root.has("files")) {
                val arr = root.getJSONArray("files")
                val existingFiles = fileRepo.getAllFilesSnapshot().associateBy { it.fileNumber.trim().lowercase() }
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val fileNo = obj.getString("fileNumber")
                    if (!existingFiles.containsKey(fileNo.trim().lowercase())) {
                        fileRepo.createFile(
                            FileEntity(
                                fileNumber = fileNo,
                                subject = obj.optString("subject", "Official File"),
                                description = obj.optString("description", ""),
                                receivedDate = obj.optLong("receivedDate", System.currentTimeMillis()),
                                currentStatus = obj.optString("currentStatus", "Received"),
                                currentSection = obj.optString("currentSection", ""),
                                currentPerson = obj.optString("currentPerson", ""),
                                nextAction = obj.optString("nextAction", ""),
                                priority = obj.optString("priority", "Medium"),
                                dueDate = obj.optLong("dueDate", System.currentTimeMillis()),
                                reminderDate = if (obj.has("reminderDate")) obj.getLong("reminderDate") else null,
                                notes = obj.optString("notes", ""),
                                createdDate = obj.optLong("createdDate", System.currentTimeMillis()),
                                modifiedDate = obj.optLong("modifiedDate", System.currentTimeMillis())
                            ),
                            initialSection = obj.optString("currentSection", ""),
                            initialPerson = obj.optString("currentPerson", ""),
                            remarks = "Imported from backup"
                        )
                    }
                }
            }

            // Meetings
            if (root.has("meetings")) {
                val arr = root.getJSONArray("meetings")
                val existingMeetings = meetingRepo.getAllMeetingsSnapshot().associateBy { it.title.trim().lowercase() + it.date }
                val newMeetings = mutableListOf<MeetingEntity>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val title = obj.getString("title")
                    val date = obj.getLong("date")
                    val key = title.trim().lowercase() + date
                    if (!existingMeetings.containsKey(key)) {
                        newMeetings.add(
                            MeetingEntity(
                                title = title,
                                date = date,
                                startTime = obj.optString("startTime", "10:00"),
                                endTime = obj.optString("endTime", "11:00"),
                                location = obj.optString("location", ""),
                                participants = obj.optString("participants", ""),
                                agenda = obj.optString("agenda", ""),
                                discussion = obj.optString("discussion", ""),
                                decisions = obj.optString("decisions", ""),
                                actionPoints = obj.optString("actionPoints", ""),
                                notes = obj.optString("notes", "")
                            )
                        )
                    }
                }
                if (newMeetings.isNotEmpty()) {
                    meetingRepo.insertAll(newMeetings)
                }
            }

            // Notes
            if (root.has("notes")) {
                val arr = root.getJSONArray("notes")
                val existingNotes = noteRepo.getAllNotesSnapshot().associateBy { it.title.trim().lowercase() }
                val newNotes = mutableListOf<NoteEntity>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val title = obj.getString("title")
                    if (!existingNotes.containsKey(title.trim().lowercase())) {
                        newNotes.add(
                            NoteEntity(
                                title = title,
                                content = obj.optString("content", ""),
                                category = obj.optString("category", "General"),
                                createdDate = obj.optLong("createdDate", System.currentTimeMillis()),
                                modifiedDate = obj.optLong("modifiedDate", System.currentTimeMillis()),
                                isPinned = obj.optBoolean("isPinned", false),
                                relatedType = if (obj.has("relatedType")) obj.getString("relatedType") else null,
                                relatedId = if (obj.has("relatedId")) obj.getLong("relatedId") else null
                            )
                        )
                    }
                }
                if (newNotes.isNotEmpty()) {
                    noteRepo.insertAll(newNotes)
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
