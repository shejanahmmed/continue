package com.shejan.acontinue.core.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationHelperTest {

    @Test
    fun `test isMapLocationLink returns true for google maps urls`() {
        assertTrue(LocationHelper.isMapLocationLink("https://maps.google.com/?q=23.8103,90.4125"))
        assertTrue(LocationHelper.isMapLocationLink("https://maps.app.goo.gl/abcdef123"))
        assertTrue(LocationHelper.isMapLocationLink("https://goo.gl/maps/xyz456"))
    }

    @Test
    fun `test isMapLocationLink returns true for generic urls and geo uris`() {
        assertTrue(LocationHelper.isMapLocationLink("geo:23.8103,90.4125"))
        assertTrue(LocationHelper.isMapLocationLink("https://www.openstreetmap.org/#map=19/23.8103/90.4125"))
        assertTrue(LocationHelper.isMapLocationLink("http://waze.com/ul?ll=23.8103,90.4125"))
    }

    @Test
    fun `test isMapLocationLink returns false for regular text addresses`() {
        assertFalse(LocationHelper.isMapLocationLink("Dhaka, Bangladesh"))
        assertFalse(LocationHelper.isMapLocationLink("Room 302, Main Building"))
        assertFalse(LocationHelper.isMapLocationLink("Ashulia, Dhaka"))
        assertFalse(LocationHelper.isMapLocationLink(""))
    }
}
