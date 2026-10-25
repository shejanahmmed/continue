package com.shejan.acontinue.presentation.create

import org.junit.Assert.assertEquals
import org.junit.Test

class NoteAudioPlaybackHelperTest {

    @Test
    fun `formatDuration formats zero and negative milliseconds safely`() {
        assertEquals("00:00", NoteAudioPlaybackHelper.formatDuration(0))
        assertEquals("00:00", NoteAudioPlaybackHelper.formatDuration(-5000))
    }

    @Test
    fun `formatDuration formats standard seconds and minutes correctly`() {
        assertEquals("00:09", NoteAudioPlaybackHelper.formatDuration(9_000))
        assertEquals("01:05", NoteAudioPlaybackHelper.formatDuration(65_432))
        assertEquals("10:30", NoteAudioPlaybackHelper.formatDuration(630_000))
    }

    @Test
    fun `calculateProgress handles zero and negative durations gracefully`() {
        assertEquals(0f, NoteAudioPlaybackHelper.calculateProgress(100, 0), 0.0001f)
        assertEquals(0f, NoteAudioPlaybackHelper.calculateProgress(100, -1000), 0.0001f)
    }

    @Test
    fun `calculateProgress clamps within valid normalized zero to one range`() {
        assertEquals(0f, NoteAudioPlaybackHelper.calculateProgress(-100, 10_000), 0.0001f)
        assertEquals(0.5f, NoteAudioPlaybackHelper.calculateProgress(5_000, 10_000), 0.0001f)
        assertEquals(1f, NoteAudioPlaybackHelper.calculateProgress(15_000, 10_000), 0.0001f)
    }

    @Test
    fun `calculateSeekPosition maps fraction accurately with clamping`() {
        assertEquals(0, NoteAudioPlaybackHelper.calculateSeekPosition(0.5f, 0))
        assertEquals(0, NoteAudioPlaybackHelper.calculateSeekPosition(-0.2f, 20_000))
        assertEquals(10_000, NoteAudioPlaybackHelper.calculateSeekPosition(0.5f, 20_000))
        assertEquals(15_000, NoteAudioPlaybackHelper.calculateSeekPosition(0.75f, 20_000))
        assertEquals(20_000, NoteAudioPlaybackHelper.calculateSeekPosition(1.5f, 20_000))
    }
}
