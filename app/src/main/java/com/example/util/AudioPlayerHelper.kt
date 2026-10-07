package com.example.util

import android.media.MediaPlayer
import java.io.File
import java.io.IOException

class AudioPlayerHelper {
    private var mediaPlayer: MediaPlayer? = null
    var currentPlayingPath: String? = null
        private set
    var isPlaying: Boolean = false
        private set

    fun playAudio(
        filePath: String,
        onCompletion: () -> Unit,
        onError: (String) -> Unit
    ) {
        val file = File(filePath)
        if (!file.exists()) {
            onError("Audio file does not exist")
            return
        }

        stopAudio()

        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                setOnCompletionListener {
                    this@AudioPlayerHelper.isPlaying = false
                    this@AudioPlayerHelper.currentPlayingPath = null
                    onCompletion()
                }
                setOnErrorListener { _, what, extra ->
                    this@AudioPlayerHelper.isPlaying = false
                    this@AudioPlayerHelper.currentPlayingPath = null
                    onError("Playback error: $what, $extra")
                    true
                }
                start()
            }
            isPlaying = true
            currentPlayingPath = filePath
        } catch (e: IOException) {
            e.printStackTrace()
            onError("Cannot play audio: ${e.localizedMessage}")
        }
    }

    fun pauseAudio() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            isPlaying = false
        }
    }

    fun resumeAudio() {
        if (mediaPlayer != null && !isPlaying) {
            mediaPlayer?.start()
            isPlaying = true
        }
    }

    fun stopAudio() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.reset()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        finally {
            mediaPlayer = null
            isPlaying = false
            currentPlayingPath = null
        }
    }
}
