package com.shejan.acontinue.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.shejan.acontinue.core.service.AudioRecordingService
import com.shejan.acontinue.domain.model.ActivityNote
import com.shejan.acontinue.domain.repository.ActivityRepository
import com.shejan.acontinue.domain.repository.NoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * State representing active voice recording session across the entire app.
 */
data class RecordingState(
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val elapsedMillis: Long = 0L,
    val noteActivityId: Long? = null,
    val currentFile: File? = null
)

sealed interface RecordingEvent {
    data class RecordingSaved(val noteId: Long?, val audioUri: Uri) : RecordingEvent
    data object RecordingCancelled : RecordingEvent
}

/**
 * Global singleton managing audio recording sessions, background foreground service lifecycle,
 * and real-time state synchronization across all application screens.
 */
class AudioRecordingManager(
    private val context: Context,
    private val activityRepository: ActivityRepository? = null,
    private val noteRepository: NoteRepository? = null,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + Job())
) {
    private val audioRecordHelper = AudioRecordHelper(context)
    private var tickerJob: Job? = null

    private val _state = MutableStateFlow(RecordingState())
    val state: StateFlow<RecordingState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<RecordingEvent>(extraBufferCapacity = 10)
    val events: SharedFlow<RecordingEvent> = _events.asSharedFlow()

    fun startRecording(noteActivityId: Long?): Boolean {
        if (_state.value.isRecording) return false

        val attachmentsDir = File(context.filesDir, "attachments").apply { if (!exists()) mkdirs() }
        val audioFile = File(attachmentsDir, "note_audio_${System.currentTimeMillis()}.m4a")

        if (!audioRecordHelper.startRecording(audioFile)) {
            return false
        }

        _state.update {
            RecordingState(
                isRecording = true,
                isPaused = false,
                elapsedMillis = 0L,
                noteActivityId = noteActivityId,
                currentFile = audioFile
            )
        }

        startServiceForeground(noteActivityId)
        startTicker()
        return true
    }

    fun updateNoteId(noteId: Long) {
        _state.update { it.copy(noteActivityId = noteId) }
    }

    fun pauseRecording(): Boolean {
        if (!_state.value.isRecording || _state.value.isPaused) return false
        if (audioRecordHelper.pauseRecording()) {
            _state.update { it.copy(isPaused = true) }
            stopTicker()
            notifyServiceAction(AudioRecordingService.ACTION_UPDATE)
            return true
        }
        return false
    }

    fun resumeRecording(): Boolean {
        if (!_state.value.isRecording || !_state.value.isPaused) return false
        if (audioRecordHelper.resumeRecording()) {
            _state.update { it.copy(isPaused = false) }
            startTicker()
            notifyServiceAction(AudioRecordingService.ACTION_UPDATE)
            return true
        }
        return false
    }

    fun stopRecording(): File? {
        if (!_state.value.isRecording) return null
        stopTicker()
        val file = audioRecordHelper.stopRecording()
        val noteId = _state.value.noteActivityId

        _state.update { RecordingState() }
        notifyServiceAction(AudioRecordingService.ACTION_STOP)

        if (file != null && file.exists() && file.length() > 0L) {
            val uri = Uri.fromFile(file)
            if (noteId != null && activityRepository != null && noteRepository != null) {
                scope.launch(Dispatchers.IO) {
                    saveAudioToNoteSuspending(noteId, uri)
                }
            }
            _events.tryEmit(RecordingEvent.RecordingSaved(noteId, uri))
        }
        return file
    }

    fun stopAndSaveBlocking(): File? {
        if (!_state.value.isRecording) return null
        stopTicker()
        val file = audioRecordHelper.stopRecording()
        val noteId = _state.value.noteActivityId

        _state.update { RecordingState() }
        notifyServiceAction(AudioRecordingService.ACTION_STOP)

        if (file != null && file.exists() && file.length() > 0L) {
            val uri = Uri.fromFile(file)
            if (noteId != null && activityRepository != null && noteRepository != null) {
                runBlocking(Dispatchers.IO) {
                    saveAudioToNoteSuspending(noteId, uri)
                }
            }
            _events.tryEmit(RecordingEvent.RecordingSaved(noteId, uri))
        }
        return file
    }

    fun cancelRecording() {
        stopTicker()
        audioRecordHelper.cancelRecording()
        _state.update { RecordingState() }
        notifyServiceAction(AudioRecordingService.ACTION_STOP)
        _events.tryEmit(RecordingEvent.RecordingCancelled)
    }

    private suspend fun saveAudioToNoteSuspending(noteId: Long, audioUri: Uri) {
        try {
            val activity = activityRepository?.getActivityById(noteId)?.firstOrNull() ?: return
            val audioTag = "\n\n![audio]($audioUri)"
            if (!activity.description.contains(audioUri.toString())) {
                val updatedDescription = (activity.description + audioTag).trim()
                activityRepository.updateActivity(activity.copy(description = updatedDescription))
            }
            val notes = noteRepository?.getNotesForActivity(noteId)?.firstOrNull() ?: emptyList()
            val targetNote = notes.firstOrNull()
            val now = System.currentTimeMillis()
            if (targetNote != null) {
                if (!targetNote.content.contains(audioUri.toString())) {
                    noteRepository?.updateNote(
                        targetNote.copy(
                            content = (targetNote.content + audioTag).trim(),
                            updatedAtMillis = now
                        )
                    )
                }
            } else {
                noteRepository?.insertNote(
                    ActivityNote(
                        activityId = noteId,
                        title = activity.title,
                        content = (activity.description + audioTag).trim(),
                        updatedAtMillis = now
                    )
                )
            }
        } catch (_: Exception) {}
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            var lastTime = System.currentTimeMillis()
            while (isActive && _state.value.isRecording && !_state.value.isPaused) {
                delay(100)
                val now = System.currentTimeMillis()
                val delta = now - lastTime
                lastTime = now
                _state.update { it.copy(elapsedMillis = it.elapsedMillis + delta) }
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private fun startServiceForeground(noteActivityId: Long?) {
        val intent = Intent(context, AudioRecordingService::class.java).apply {
            action = AudioRecordingService.ACTION_START
            if (noteActivityId != null) {
                putExtra(AudioRecordingService.EXTRA_NOTE_ID, noteActivityId)
            }
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (_: Exception) {}
    }

    private fun notifyServiceAction(actionName: String) {
        val intent = Intent(context, AudioRecordingService::class.java).apply {
            action = actionName
        }
        try {
            context.startService(intent)
        } catch (_: Exception) {}
    }
}
