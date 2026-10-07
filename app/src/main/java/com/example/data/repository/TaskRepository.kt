package com.example.data.repository

import com.example.data.local.dao.TaskDao
import com.example.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val pendingTasks: Flow<List<TaskEntity>> = taskDao.getPendingTasks()
    val completedTasks: Flow<List<TaskEntity>> = taskDao.getCompletedTasks()

    fun getTodayTasks(startOfDay: Long, endOfDay: Long): Flow<List<TaskEntity>> =
        taskDao.getTodayTasks(startOfDay, endOfDay)

    fun getOverdueTasks(currentTime: Long): Flow<List<TaskEntity>> =
        taskDao.getOverdueTasks(currentTime)

    fun searchTasks(query: String): Flow<List<TaskEntity>> =
        taskDao.searchTasks(query)

    suspend fun getTaskById(id: Long): TaskEntity? = taskDao.getTaskById(id)

    fun getTaskFlow(id: Long): Flow<TaskEntity?> = taskDao.getTaskFlow(id)

    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)

    suspend fun getAllTasksSnapshot(): List<TaskEntity> = taskDao.getAllTasksSnapshot()

    suspend fun insertAll(tasks: List<TaskEntity>) = taskDao.insertAll(tasks)
}
