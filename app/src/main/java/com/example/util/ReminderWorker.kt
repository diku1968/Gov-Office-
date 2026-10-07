package com.example.util

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class ReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: "Reminder"
        val message = inputData.getString(KEY_MESSAGE) ?: "You have a pending work item."
        val type = inputData.getString(KEY_TYPE) ?: "GovWork"
        val id = inputData.getInt(KEY_ID, System.currentTimeMillis().toInt())

        NotificationHelper.showReminderNotification(
            context = applicationContext,
            notificationId = id,
            title = title,
            message = message,
            type = type
        )

        return Result.success()
    }

    companion object {
        const val KEY_TITLE = "key_title"
        const val KEY_MESSAGE = "key_message"
        const val KEY_TYPE = "key_type"
        const val KEY_ID = "key_id"

        fun scheduleReminder(
            context: Context,
            tag: String,
            notificationId: Int,
            title: String,
            message: String,
            type: String,
            targetTimeMillis: Long
        ) {
            val delayMillis = targetTimeMillis - System.currentTimeMillis()
            if (delayMillis <= 0) return

            val data = Data.Builder()
                .putString(KEY_TITLE, title)
                .putString(KEY_MESSAGE, message)
                .putString(KEY_TYPE, type)
                .putInt(KEY_ID, notificationId)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag(tag)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }

        fun cancelReminder(context: Context, tag: String) {
            WorkManager.getInstance(context).cancelAllWorkByTag(tag)
        }
    }
}
