package com.shejan.acontinue.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class AudioTrimHelperTest {

    @Test
    fun `test clampTrimRange handles zero and negative durations gracefully`() {
        val (start, end) = AudioTrimHelper.clampTrimRange(-10, 50, 0)
        assertEquals(0, start)
        assertEquals(0, end)

        val (startNeg, endNeg) = AudioTrimHelper.clampTrimRange(-50, -20, -100)
        assertEquals(0, startNeg)
        assertEquals(0, endNeg)
    }

    @Test
    fun `test clampTrimRange clamps within valid duration bounds`() {
        val (start, end) = AudioTrimHelper.clampTrimRange(-500, 35000, 30000)
        assertEquals(0, start)
        assertEquals(30000, end)
    }

    @Test
    fun `test clampTrimRange corrects inverted start and end bounds`() {
        val (start, end) = AudioTrimHelper.clampTrimRange(15000, 5000, 30000)
        assertEquals(15000, start)
        assertEquals(15000, end)
    }

    @Test
    fun `test clampTrimRange preserves valid intermediate range`() {
        val (start, end) = AudioTrimHelper.clampTrimRange(5000, 20000, 30000)
        assertEquals(5000, start)
        assertEquals(20000, end)
    }
}
