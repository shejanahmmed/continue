package com.shejan.acontinue.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AudioRecordingManagerTest {

    @Test
    fun `test initial RecordingState has default idle values`() {
        val state = RecordingState()
        assertFalse(state.isRecording)
        assertFalse(state.isPaused)
        assertEquals(0L, state.elapsedMillis)
        assertNull(state.noteActivityId)
        assertNull(state.currentFile)
    }

    @Test
    fun `test RecordingState copy updates attributes correctly`() {
        val mockFile = File("/mock/audio.m4a")
        val state = RecordingState(
            isRecording = true,
            isPaused = false,
            elapsedMillis = 1500L,
            noteActivityId = 42L,
            currentFile = mockFile
        )

        assertTrue(state.isRecording)
        assertFalse(state.isPaused)
        assertEquals(1500L, state.elapsedMillis)
        assertEquals(42L, state.noteActivityId)
        assertEquals(mockFile, state.currentFile)

        val pausedState = state.copy(isPaused = true, elapsedMillis = 2000L)
        assertTrue(pausedState.isRecording)
        assertTrue(pausedState.isPaused)
        assertEquals(2000L, pausedState.elapsedMillis)
        assertEquals(42L, pausedState.noteActivityId)
    }

    @Test
    fun `test RecordingCancelled event singleton`() {
        val cancelledEvent = RecordingEvent.RecordingCancelled
        assertEquals(RecordingEvent.RecordingCancelled, cancelledEvent)
    }
}
