package com.shejan.acontinue.ui.theme

import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HapticFeedbackDelegationTest {

    private class FakeHapticFeedback : HapticFeedback {
        var callCount = 0
        var lastType: HapticFeedbackType? = null

        override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
            callCount++
            lastType = hapticFeedbackType
        }
    }

    private fun createEffectiveHaptic(
        systemHaptic: HapticFeedback,
        hapticEnabled: Boolean
    ): HapticFeedback {
        return object : HapticFeedback {
            override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
                if (hapticEnabled) {
                    systemHaptic.performHapticFeedback(hapticFeedbackType)
                }
            }
        }
    }

    @Test
    fun `when haptic feedback is disabled in settings, calls are suppressed`() {
        val fakeSystemHaptic = FakeHapticFeedback()
        val effectiveHaptic = createEffectiveHaptic(fakeSystemHaptic, hapticEnabled = false)

        effectiveHaptic.performHapticFeedback(HapticFeedbackType.LongPress)
        effectiveHaptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

        assertEquals("Expected 0 calls when haptic feedback is disabled", 0, fakeSystemHaptic.callCount)
    }

    @Test
    fun `when haptic feedback is enabled in settings, calls are forwarded to system haptic`() {
        val fakeSystemHaptic = FakeHapticFeedback()
        val effectiveHaptic = createEffectiveHaptic(fakeSystemHaptic, hapticEnabled = true)

        effectiveHaptic.performHapticFeedback(HapticFeedbackType.LongPress)

        assertEquals(1, fakeSystemHaptic.callCount)
        assertEquals(HapticFeedbackType.LongPress, fakeSystemHaptic.lastType)
    }
}

