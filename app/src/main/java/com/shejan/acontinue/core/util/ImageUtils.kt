package com.shejan.acontinue.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.system.Os
import android.system.OsConstants
import android.util.Log
import java.io.BufferedInputStream
import java.io.File
import java.io.FileDescriptor

/**
 * High-performance image loading utility with memory downsampling, non-blocking I/O support,
 * and EXIF orientation normalization.
 */
object ImageUtils {

    private const val TAG = "ImageUtils"

    /**
     * Decodes a bitmap directly from a local [File] with memory downsampling and EXIF orientation normalization.
     */
    fun loadSampledBitmapFromFile(
        file: File,
        reqWidth: Int = 1080,
        reqHeight: Int = 1920
    ): Bitmap? {
        if (!file.exists() || file.length() <= 0L) {
            Log.e(TAG, "loadSampledBitmapFromFile: File does not exist or is empty: ${file.absolutePath}")
            return null
        }
        return runCatching {
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, boundsOptions)

            if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
                Log.e(TAG, "loadSampledBitmapFromFile: Failed to read image bounds for ${file.absolutePath}")
                return null
            }

            val inSampleSize = calculateInSampleSize(boundsOptions, reqWidth, reqHeight)
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }

            val rawBitmap = BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
                ?: run {
                    Log.e(TAG, "loadSampledBitmapFromFile: BitmapFactory.decodeFile returned null for ${file.absolutePath}")
                    return null
                }

            val rotationDegrees = getExifRotationFromFile(file)
            applyRotation(rawBitmap, rotationDegrees)
        }.onFailure {
            Log.e(TAG, "loadSampledBitmapFromFile error: ${it.message}", it)
        }.getOrNull()
    }

    /**
     * Decodes a bitmap from a [Uri] (supporting file:// and content://) with memory downsampling and EXIF normalization.
     */
    fun loadSampledBitmapFromUri(
        context: Context,
        uri: Uri,
        reqWidth: Int = 1080,
        reqHeight: Int = 1920
    ): Bitmap? {
        // Direct file scheme resolution
        if (uri.scheme == "file") {
            val file = uri.path?.let { File(it) }
            if (file != null && file.exists()) {
                return loadSampledBitmapFromFile(file, reqWidth, reqHeight)
            }
        }

        // Content scheme resolution
        return runCatching {
            // First attempt: openFileDescriptor (native seekable descriptor)
            val pfd = runCatching { context.contentResolver.openFileDescriptor(uri, "r") }.getOrNull()
            if (pfd != null) {
                pfd.use { descriptor ->
                    val fd = descriptor.fileDescriptor
                    val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFileDescriptor(fd, null, boundsOptions)

                    if (boundsOptions.outWidth > 0 && boundsOptions.outHeight > 0) {
                        val inSampleSize = calculateInSampleSize(boundsOptions, reqWidth, reqHeight)
                        val decodeOptions = BitmapFactory.Options().apply {
                            this.inSampleSize = inSampleSize
                        }

                        // Reset descriptor offset after bounds inspection
                        runCatching { Os.lseek(fd, 0, OsConstants.SEEK_SET) }

                        val rawBitmap = BitmapFactory.decodeFileDescriptor(fd, null, decodeOptions)
                        if (rawBitmap != null) {
                            val rotationDegrees = getExifRotationFromFd(fd)
                            return@runCatching applyRotation(rawBitmap, rotationDegrees)
                        }
                    }
                }
            }

            // Fallback: BufferedInputStream decode
            decodeFromBufferedStream(context, uri, reqWidth, reqHeight)
        }.onFailure {
            Log.e(TAG, "loadSampledBitmapFromUri error: ${it.message}", it)
        }.getOrNull()
    }

    private fun decodeFromBufferedStream(
        context: Context,
        uri: Uri,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap? {
        return runCatching {
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.let { BufferedInputStream(it) }?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }

            if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) return null

            val inSampleSize = calculateInSampleSize(boundsOptions, reqWidth, reqHeight)
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }

            val rawBitmap = context.contentResolver.openInputStream(uri)?.let { BufferedInputStream(it) }?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return null

            val rotationDegrees = getExifRotationFromStream(context, uri)
            applyRotation(rawBitmap, rotationDegrees)
        }.getOrNull()
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        val height = options.outHeight
        val width = options.outWidth
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun applyRotation(bitmap: Bitmap, degrees: Float): Bitmap {
        if (degrees == 0f) return bitmap
        return runCatching {
            val matrix = Matrix().apply { postRotate(degrees) }
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated != bitmap) {
                bitmap.recycle()
            }
            rotated
        }.getOrDefault(bitmap)
    }

    private fun getExifRotationFromFile(file: File): Float {
        return runCatching {
            val exif = ExifInterface(file.absolutePath)
            orientationToDegrees(exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL))
        }.getOrDefault(0f)
    }

    private fun getExifRotationFromFd(fd: FileDescriptor): Float {
        return runCatching {
            val exif = ExifInterface(fd)
            orientationToDegrees(exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL))
        }.getOrDefault(0f)
    }

    private fun getExifRotationFromStream(context: Context, uri: Uri): Float {
        return runCatching {
            context.contentResolver.openInputStream(uri)?.let { BufferedInputStream(it) }?.use { stream ->
                val exif = ExifInterface(stream)
                orientationToDegrees(exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL))
            } ?: 0f
        }.getOrDefault(0f)
    }

    private fun orientationToDegrees(orientation: Int): Float {
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
    }
}
