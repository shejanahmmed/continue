package com.shejan.acontinue.core.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/**
 * Handles device microphone audio recording with MediaRecorder, supporting
 * modern Android S+ API alongside legacy backward-compatible fallbacks.
 */
class AudioRecordHelper(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    var isRecording: Boolean = false
        private set
    var isPaused: Boolean = false
        private set
    var currentFile: File? = null
        private set

    fun startRecording(outputFile: File): Boolean {
        if (isRecording) return false
        return try {
            outputFile.parentFile?.let { if (!it.exists()) it.mkdirs() }

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            currentFile = outputFile
            isRecording = true
            isPaused = false
            true
        } catch (e: Exception) {
            release()
            false
        }
    }

    fun pauseRecording(): Boolean {
        if (!isRecording || isPaused) return false
        return try {
            mediaRecorder?.pause()
            isPaused = true
            true
        } catch (_: Exception) {
            false
        }
    }

    fun resumeRecording(): Boolean {
        if (!isRecording || !isPaused) return false
        return try {
            mediaRecorder?.resume()
            isPaused = false
            true
        } catch (_: Exception) {
            false
        }
    }

    fun stopRecording(): File? {
        if (!isRecording) return null
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            isPaused = false
            currentFile
        } catch (e: Exception) {
            release()
            null
        }
    }

    fun cancelRecording() {
        val file = currentFile
        release()
        file?.delete()
    }

    fun release() {
        try {
            if (isRecording) {
                mediaRecorder?.stop()
            }
        } catch (_: Exception) {}
        try {
            mediaRecorder?.release()
        } catch (_: Exception) {}
        mediaRecorder = null
        isRecording = false
        isPaused = false
        currentFile = null
    }
}
