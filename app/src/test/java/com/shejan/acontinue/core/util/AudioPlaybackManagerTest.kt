package com.shejan.acontinue.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioPlaybackManagerTest {

    @Test
    fun `test initial AudioPlaybackState has default idle values`() {
        val state = AudioPlaybackState()
        assertNull(state.currentUri)
        assertEquals("", state.title)
        assertFalse(state.isPlaying)
        assertEquals(0, state.currentPositionMs)
        assertEquals(0, state.totalDurationMs)
    }

    @Test
    fun `test AudioPlaybackState copy updates attributes correctly`() {
        val state = AudioPlaybackState(
            currentUri = null,
            title = "Recording 1",
            isPlaying = true,
            currentPositionMs = 5000,
            totalDurationMs = 30000
        )

        assertNull(state.currentUri)
        assertEquals("Recording 1", state.title)
        assertTrue(state.isPlaying)
        assertEquals(5000, state.currentPositionMs)
        assertEquals(30000, state.totalDurationMs)

        val pausedState = state.copy(isPlaying = false, currentPositionMs = 8000)
        assertFalse(pausedState.isPlaying)
        assertEquals(8000, pausedState.currentPositionMs)
        assertEquals(30000, pausedState.totalDurationMs)
        assertEquals("Recording 1", pausedState.title)
    }

    @Test
    fun `test AudioPlaybackState reset clears all playback properties`() {
        var state = AudioPlaybackState(
            currentUri = null,
            title = "Recording 2",
            isPlaying = true,
            currentPositionMs = 12000,
            totalDurationMs = 24000
        )

        state = AudioPlaybackState()
        assertNull(state.currentUri)
        assertEquals("", state.title)
        assertFalse(state.isPlaying)
        assertEquals(0, state.currentPositionMs)
        assertEquals(0, state.totalDurationMs)
    }
}
