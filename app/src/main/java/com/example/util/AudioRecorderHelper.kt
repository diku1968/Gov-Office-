package com.example.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException

class AudioRecorderHelper(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var startTimeMillis: Long = 0L

    fun startRecording(): File? {
        val voiceNotesDir = File(context.filesDir, "voice_notes").apply {
            if (!exists()) mkdirs()
        }
        val file = File(voiceNotesDir, "rec_${System.currentTimeMillis()}.m4a")
        currentOutputFile = file

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000)
            setAudioSamplingRate(44100)
            setOutputFile(file.absolutePath)
            try {
                prepare()
                start()
                startTimeMillis = System.currentTimeMillis()
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
        }
        return file
    }

    fun stopRecording(): Long {
        val duration = System.currentTimeMillis() - startTimeMillis
        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaRecorder = null
        }
        return if (duration > 0) duration else 0L
    }

    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
        } catch (_: Exception) {}
        finally {
            mediaRecorder = null
            currentOutputFile?.let {
                if (it.exists()) it.delete()
            }
            currentOutputFile = null
        }
    }
}
