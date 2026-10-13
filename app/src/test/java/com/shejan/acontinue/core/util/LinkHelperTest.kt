package com.shejan.acontinue.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LinkHelperTest {

    @Test
    fun extractUrls_findsStandardHttpAndHttpsUrls() {
        val text = "Check out https://github.com/shejanahmmed and http://example.com/test for details."
        val urls = LinkHelper.extractUrls(text)

        assertEquals(2, urls.size)
        assertEquals("https://github.com/shejanahmmed", urls[0])
        assertEquals("http://example.com/test", urls[1])
    }

    @Test
    fun extractUrls_stripsTrailingPunctuation() {
        val text = "Visit (https://google.com), or www.android.com; thanks!"
        val urls = LinkHelper.extractUrls(text)

        assertEquals(2, urls.size)
        assertEquals("https://google.com", urls[0])
        assertEquals("www.android.com", urls[1])
    }

    @Test
    fun extractUrls_handlesEmptyOrBlankText() {
        assertTrue(LinkHelper.extractUrls("").isEmpty())
        assertTrue(LinkHelper.extractUrls("Just some plain text without links").isEmpty())
    }

    @Test
    fun formatUrl_prependsHttpsWhenMissing() {
        assertEquals("https://www.google.com", LinkHelper.formatUrl("www.google.com"))
        assertEquals("https://google.com", LinkHelper.formatUrl("google.com"))
        assertEquals("https://google.com", LinkHelper.formatUrl("https://google.com"))
        assertEquals("http://insecure.org", LinkHelper.formatUrl("http://insecure.org"))
    }
}
