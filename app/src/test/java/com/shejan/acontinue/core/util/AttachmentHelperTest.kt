package com.shejan.acontinue.core.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AttachmentHelperTest {

    @Test
    fun isSupportedFile_acceptsImagesPdfsDocsSheetsAndTxt() {
        // Images
        assertTrue(AttachmentHelper.isSupportedFile("photo.png"))
        assertTrue(AttachmentHelper.isSupportedFile("image.jpg"))
        assertTrue(AttachmentHelper.isSupportedFile("picture.jpeg"))
        assertTrue(AttachmentHelper.isSupportedFile("banner.webp"))

        // PDFs
        assertTrue(AttachmentHelper.isSupportedFile("document.pdf"))

        // Word docs
        assertTrue(AttachmentHelper.isSupportedFile("notes.doc"))
        assertTrue(AttachmentHelper.isSupportedFile("report.docx"))

        // Spreadsheets
        assertTrue(AttachmentHelper.isSupportedFile("budget.xls"))
        assertTrue(AttachmentHelper.isSupportedFile("data.xlsx"))
        assertTrue(AttachmentHelper.isSupportedFile("export.csv"))

        // Text files
        assertTrue(AttachmentHelper.isSupportedFile("readme.txt"))
    }

    @Test
    fun isSupportedFile_rejectsUnsupportedFormats() {
        assertFalse(AttachmentHelper.isSupportedFile("app.apk"))
        assertFalse(AttachmentHelper.isSupportedFile("script.sh"))
        assertFalse(AttachmentHelper.isSupportedFile("archive.zip"))
        assertFalse(AttachmentHelper.isSupportedFile("binary.exe"))
    }

    @Test
    fun getCategory_resolvesCorrectAttachmentCategory() {
        assertEquals(AttachmentCategory.IMAGE, AttachmentHelper.getCategory("sample.jpg", null))
        assertEquals(AttachmentCategory.PDF, AttachmentHelper.getCategory("sample.pdf", null))
        assertEquals(AttachmentCategory.DOCUMENT, AttachmentHelper.getCategory("sample.docx", null))
        assertEquals(AttachmentCategory.SPREADSHEET, AttachmentHelper.getCategory("sample.xlsx", null))
        assertEquals(AttachmentCategory.TEXT, AttachmentHelper.getCategory("sample.txt", null))
    }

    @Test
    fun formatFileSize_formatsCorrectly() {
        assertEquals("0 B", AttachmentHelper.formatFileSize(0))
        assertEquals("512 B", AttachmentHelper.formatFileSize(512))
        assertEquals("1.0 KB", AttachmentHelper.formatFileSize(1024))
        assertEquals("1.5 MB", AttachmentHelper.formatFileSize((1.5 * 1024 * 1024).toLong()))
        assertEquals("25.0 MB", AttachmentHelper.formatFileSize(25 * 1024 * 1024L))
    }

    @Test
    fun maxFileSize_isExactly25MB() {
        assertEquals(26214400L, AttachmentHelper.MAX_FILE_SIZE_BYTES)
    }
}
