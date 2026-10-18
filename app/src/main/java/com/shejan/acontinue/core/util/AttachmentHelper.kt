package com.shejan.acontinue.core.util

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.UUID

data class ProcessedAttachment(
    val id: String = UUID.randomUUID().toString(),
    val fileName: String,
    val filePath: String,
    val mimeType: String,
    val sizeBytes: Long
)

enum class AttachmentCategory {
    IMAGE,
    PDF,
    DOCUMENT,
    SPREADSHEET,
    TEXT,
    OTHER
}

object AttachmentHelper {
    const val MAX_FILE_SIZE_BYTES = 25 * 1024 * 1024L // 25 MB

    val SUPPORTED_MIME_TYPES = arrayOf(
        "image/*",
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "text/plain",
        "text/csv"
    )

    private val SUPPORTED_EXTENSIONS = setOf(
        "png", "jpg", "jpeg", "webp", "gif", "bmp", "svg",
        "pdf",
        "doc", "docx",
        "xls", "xlsx", "csv",
        "txt"
    )

    fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
    }

    fun isSupportedFile(fileName: String, mimeType: String? = null): Boolean {
        val ext = getFileExtension(fileName)
        if (SUPPORTED_EXTENSIONS.contains(ext)) return true
        if (mimeType != null) {
            val lowerMime = mimeType.lowercase(Locale.ROOT)
            if (lowerMime.startsWith("image/") ||
                lowerMime == "application/pdf" ||
                lowerMime == "application/msword" ||
                lowerMime == "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ||
                lowerMime == "application/vnd.ms-excel" ||
                lowerMime == "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" ||
                lowerMime == "text/plain" ||
                lowerMime == "text/csv"
            ) {
                return true
            }
        }
        return false
    }

    fun getCategory(fileName: String, mimeType: String?): AttachmentCategory {
        val ext = getFileExtension(fileName)
        val lowerMime = mimeType?.lowercase(Locale.ROOT) ?: ""

        return when {
            lowerMime.startsWith("image/") || ext in setOf("png", "jpg", "jpeg", "webp", "gif", "bmp", "svg") -> AttachmentCategory.IMAGE
            lowerMime == "application/pdf" || ext == "pdf" -> AttachmentCategory.PDF
            lowerMime in setOf("application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document") || ext in setOf("doc", "docx") -> AttachmentCategory.DOCUMENT
            lowerMime in setOf("application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "text/csv") || ext in setOf("xls", "xlsx", "csv") -> AttachmentCategory.SPREADSHEET
            lowerMime == "text/plain" || ext == "txt" -> AttachmentCategory.TEXT
            else -> AttachmentCategory.OTHER
        }
    }

    fun formatFileSize(sizeBytes: Long): String {
        if (sizeBytes <= 0) return "0 B"
        if (sizeBytes < 1024) return "$sizeBytes B"
        val kb = sizeBytes / 1024.0
        if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
        val mb = kb / 1024.0
        return String.format(Locale.US, "%.1f MB", mb)
    }

    fun processAndCopyUri(context: Context, uri: Uri): Result<ProcessedAttachment> {
        return runCatching {
            var fileName = "attachment_${System.currentTimeMillis()}"
            var estimatedSize = 0L

            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIdx != -1) {
                        val nameStr = cursor.getString(nameIdx)
                        if (!nameStr.isNullOrBlank()) fileName = nameStr
                    }
                    if (sizeIdx != -1) {
                        estimatedSize = cursor.getLong(sizeIdx)
                    }
                }
            }

            if (estimatedSize > MAX_FILE_SIZE_BYTES) {
                throw IllegalArgumentException("\"$fileName\" exceeds the 25MB size limit.")
            }

            val resolvedMime = context.contentResolver.getType(uri)
                ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(getFileExtension(fileName))
                ?: "application/octet-stream"

            if (!isSupportedFile(fileName, resolvedMime)) {
                throw IllegalArgumentException("\"$fileName\" is not a supported file format. Allowed: Images, PDF, Word, Excel, CSV, and TXT.")
            }

            val attachmentsDir = File(context.filesDir, "attachments").apply {
                if (!exists()) mkdirs()
            }

            val sanitizedBase = fileName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            val targetFile = File(attachmentsDir, "${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}_$sanitizedBase")

            var totalBytesRead = 0L
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val buffer = ByteArray(8192)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        totalBytesRead += read
                        if (totalBytesRead > MAX_FILE_SIZE_BYTES) {
                            targetFile.delete()
                            throw IllegalArgumentException("\"$fileName\" exceeds the 25MB size limit.")
                        }
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            } ?: throw IllegalStateException("Could not open input stream for file.")

            ProcessedAttachment(
                fileName = fileName,
                filePath = targetFile.absolutePath,
                mimeType = resolvedMime,
                sizeBytes = totalBytesRead
            )
        }
    }

    fun openAttachmentInDefaultApp(context: Context, filePath: String, mimeType: String): Result<Unit> {
        return runCatching {
            val (contentUri, fileName) = if (filePath.startsWith("content://")) {
                val uri = Uri.parse(filePath)
                var name = "attachment"
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIdx != -1) {
                        val nameStr = cursor.getString(nameIdx)
                        if (!nameStr.isNullOrBlank()) name = nameStr
                    }
                }
                Pair(uri, name)
            } else {
                val file = File(filePath)
                if (!file.exists()) {
                    throw IllegalStateException("File no longer exists on device.")
                }
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                Pair(uri, file.name)
            }

            // Derive specific MIME type from extension if picker passed generic stream type
            val ext = getFileExtension(fileName)
            val resolvedMime = when {
                ext.isNotEmpty() -> MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
                    ?: (if (mimeType.isNotBlank() && mimeType != "application/octet-stream") mimeType else "*/*")
                mimeType.isNotBlank() && mimeType != "application/octet-stream" -> mimeType
                else -> "*/*"
            }

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, resolvedMime)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                // ClipData is required on API 29+ to pass URI read permission across IPC boundaries
                clipData = ClipData.newUri(context.contentResolver, fileName, contentUri)
            }

            // Explicitly grant read permission to candidate activity packages for target URI
            val resInfoList = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.queryIntentActivities(
                    viewIntent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.queryIntentActivities(viewIntent, PackageManager.MATCH_DEFAULT_ONLY)
            }
            for (resolveInfo in resInfoList) {
                context.grantUriPermission(
                    resolveInfo.activityInfo.packageName,
                    contentUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

            try {
                context.startActivity(viewIntent)
            } catch (_: ActivityNotFoundException) {
                // If specific viewer isn't registered, fallback to generic chooser
                val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(contentUri, "*/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    clipData = ClipData.newUri(context.contentResolver, fileName, contentUri)
                }
                val chooser = Intent.createChooser(fallbackIntent, "Open with").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    clipData = ClipData.newUri(context.contentResolver, fileName, contentUri)
                }
                context.startActivity(chooser)
            }
        }
    }

    fun deleteLocalFile(filePath: String) {
        runCatching {
            val file = File(filePath)
            if (file.exists()) file.delete()
        }
    }
}
