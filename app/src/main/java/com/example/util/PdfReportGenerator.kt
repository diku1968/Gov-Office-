package com.example.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.local.entity.FileEntity
import com.example.data.local.entity.MeetingEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.UserProfileEntity
import java.io.File
import java.io.FileOutputStream

data class ReportData(
    val reportType: String, // "Daily", "Weekly", "Monthly"
    val periodString: String,
    val tasks: List<TaskEntity>,
    val files: List<FileEntity>,
    val meetings: List<MeetingEntity>,
    val notes: List<NoteEntity>
)

object PdfReportGenerator {

    fun generatePdfReport(
        context: Context,
        profile: UserProfileEntity?,
        reportData: ReportData
    ): File? {
        val reportsDir = File(context.filesDir, "reports").apply {
            if (!exists()) mkdirs()
        }
        val fileName = "GovWork_Report_${reportData.reportType}_${System.currentTimeMillis()}.pdf"
        val outputFile = File(reportsDir, fileName)

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 points
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(30, 58, 138) // Deep Navy
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subTitlePaint = Paint().apply {
            color = Color.rgb(71, 85, 105) // Slate
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            isAntiAlias = true
        }

        val headerPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 10f
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.rgb(203, 213, 225)
            strokeWidth = 1f
        }

        var yPos = 40f

        // Document Header
        canvas.drawText("GovWork Assistant", 40f, yPos, titlePaint)
        yPos += 16f
        canvas.drawText("Personal Work Management & Official Executive Log", 40f, yPos, subTitlePaint)
        yPos += 14f

        canvas.drawLine(40f, yPos, 555f, yPos, linePaint)
        yPos += 20f

        // Profile Details & Report Period
        val empName = profile?.employeeName?.ifBlank { "Government Employee" } ?: "Government Employee"
        val dept = profile?.department?.ifBlank { "Administrative Department" } ?: "Administrative Department"
        val desig = profile?.designation?.ifBlank { "Officer" } ?: "Officer"
        val office = profile?.office?.ifBlank { "General Office" } ?: "General Office"

        canvas.drawText("Employee: $empName ($desig)", 40f, yPos, headerPaint)
        yPos += 16f
        canvas.drawText("Department: $dept | Office: $office", 40f, yPos, textPaint)
        yPos += 16f
        canvas.drawText("Report Type: ${reportData.reportType} Report | Period: ${reportData.periodString}", 40f, yPos, textPaint)
        yPos += 18f

        canvas.drawLine(40f, yPos, 555f, yPos, linePaint)
        yPos += 22f

        // Summary Statistics Box
        val completedTasks = reportData.tasks.count { it.status == "Completed" }
        val pendingTasks = reportData.tasks.count { it.status == "Pending" || it.status == "In Progress" }
        val overdueTasks = reportData.tasks.count { it.dueDate < System.currentTimeMillis() && it.status != "Completed" }

        canvas.drawText("EXECUTIVE WORK SUMMARY", 40f, yPos, headerPaint)
        yPos += 16f
        canvas.drawText("• Tasks Completed: $completedTasks", 48f, yPos, textPaint)
        canvas.drawText("• Tasks Pending: $pendingTasks", 220f, yPos, textPaint)
        canvas.drawText("• Overdue Tasks: $overdueTasks", 380f, yPos, textPaint)
        yPos += 16f
        canvas.drawText("• Files Tracked/Processed: ${reportData.files.size}", 48f, yPos, textPaint)
        canvas.drawText("• Meetings Scheduled: ${reportData.meetings.size}", 220f, yPos, textPaint)
        canvas.drawText("• Notes Recorded: ${reportData.notes.size}", 380f, yPos, textPaint)
        yPos += 24f

        canvas.drawLine(40f, yPos, 555f, yPos, linePaint)
        yPos += 22f

        // Tasks Details Section
        canvas.drawText("1. TASKS BREAKDOWN", 40f, yPos, headerPaint)
        yPos += 16f
        if (reportData.tasks.isEmpty()) {
            canvas.drawText("No tasks recorded for this period.", 48f, yPos, textPaint)
            yPos += 16f
        } else {
            reportData.tasks.take(8).forEach { task ->
                val taskLine = "[${task.status.uppercase()}] ${task.title} (Priority: ${task.priority}, Due: ${DateUtils.formatDate(task.dueDate)})"
                canvas.drawText(taskLine.take(85), 48f, yPos, textPaint)
                yPos += 14f
            }
        }
        yPos += 10f

        // Files Section
        canvas.drawText("2. OFFICIAL FILES & MOVEMENTS", 40f, yPos, headerPaint)
        yPos += 16f
        if (reportData.files.isEmpty()) {
            canvas.drawText("No file movements tracked for this period.", 48f, yPos, textPaint)
            yPos += 16f
        } else {
            reportData.files.take(6).forEach { file ->
                val fileLine = "File #${file.fileNumber}: ${file.subject} [${file.currentStatus}] - Current: ${file.currentSection.ifBlank { "Office" }}"
                canvas.drawText(fileLine.take(85), 48f, yPos, textPaint)
                yPos += 14f
            }
        }
        yPos += 10f

        // Meetings Section
        canvas.drawText("3. MEETINGS & DELIBERATIONS", 40f, yPos, headerPaint)
        yPos += 16f
        if (reportData.meetings.isEmpty()) {
            canvas.drawText("No meetings scheduled during this period.", 48f, yPos, textPaint)
            yPos += 16f
        } else {
            reportData.meetings.take(5).forEach { m ->
                val meetingLine = "${DateUtils.formatDate(m.date)} (${m.startTime}-${m.endTime}): ${m.title} @ ${m.location.ifBlank { "Office" }}"
                canvas.drawText(meetingLine.take(85), 48f, yPos, textPaint)
                yPos += 14f
            }
        }
        yPos += 10f

        // Notes / Action Points
        canvas.drawText("4. IMPORTANT NOTES & ACTION POINTS", 40f, yPos, headerPaint)
        yPos += 16f
        if (reportData.notes.isEmpty()) {
            canvas.drawText("No notes logged for this period.", 48f, yPos, textPaint)
            yPos += 16f
        } else {
            reportData.notes.take(4).forEach { note ->
                val noteLine = "• ${note.title}: ${note.content.replace("\n", " ").take(65)}"
                canvas.drawText(noteLine, 48f, yPos, textPaint)
                yPos += 14f
            }
        }

        // Footer
        canvas.drawLine(40f, 800f, 555f, 800f, linePaint)
        canvas.drawText("Generated on ${DateUtils.formatDateTime(System.currentTimeMillis())} via GovWork Assistant (Offline Personal Assistant)", 40f, 815f, subTitlePaint)

        document.finishPage(page)

        try {
            val fos = FileOutputStream(outputFile)
            document.writeTo(fos)
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            return null
        } finally {
            document.close()
        }

        return outputFile
    }

    fun getFileUri(context: Context, file: File) =
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
}
