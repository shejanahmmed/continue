package com.shejan.acontinue.core.util

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import java.io.File
import java.nio.ByteBuffer

/**
 * Pure and platform-native audio trimming utility for note voice recordings.
 * Uses MediaExtractor and MediaMuxer to slice AAC/MPEG-4 streams losslessly without transcoding.
 */
object AudioTrimHelper {

    fun clampTrimRange(startMs: Int, endMs: Int, totalDurationMs: Int): Pair<Int, Int> {
        val total = totalDurationMs.coerceAtLeast(0)
        val validStart = startMs.coerceIn(0, total)
        val validEnd = endMs.coerceIn(validStart, total)
        return Pair(validStart, validEnd)
    }

    fun getAudioDurationMs(context: Context, uri: Uri): Int {
        return runCatching {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            retriever.release()
            durStr?.toIntOrNull() ?: 0
        }.getOrDefault(0)
    }

    /**
     * Losslessly trims an audio file from sourceUri between startMs and endMs into destFile.
     */
    fun trimAudio(
        context: Context,
        sourceUri: Uri,
        destFile: File,
        startMs: Long,
        endMs: Long
    ): Boolean {
        if (destFile.exists()) {
            destFile.delete()
        }

        val extractor = MediaExtractor()
        var muxer: MediaMuxer? = null

        return try {
            extractor.setDataSource(context, sourceUri, null)
            val trackCount = extractor.trackCount
            var audioTrackIndex = -1
            var audioFormat: MediaFormat? = null

            for (i in 0 until trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("audio/")) {
                    audioTrackIndex = i
                    audioFormat = format
                    break
                }
            }

            if (audioTrackIndex < 0 || audioFormat == null) {
                return false
            }

            extractor.selectTrack(audioTrackIndex)

            val startUs = (startMs * 1000L).coerceAtLeast(0L)
            val endUs = (endMs * 1000L).coerceAtLeast(startUs)

            extractor.seekTo(startUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

            muxer = MediaMuxer(destFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val writeTrackIndex = muxer.addTrack(audioFormat)
            muxer.start()

            val maxBufferSize = if (audioFormat.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                audioFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE).coerceAtLeast(128 * 1024)
            } else {
                128 * 1024
            }

            val buffer = ByteBuffer.allocate(maxBufferSize)
            val bufferInfo = MediaCodec.BufferInfo()
            var basePresentationTimeUs: Long? = null

            while (true) {
                bufferInfo.offset = 0
                bufferInfo.size = extractor.readSampleData(buffer, 0)

                if (bufferInfo.size < 0) {
                    bufferInfo.size = 0
                    break
                }

                val presentationTimeUs = extractor.sampleTime
                if (presentationTimeUs > endUs) {
                    break
                }

                if (presentationTimeUs >= startUs) {
                    if (basePresentationTimeUs == null) {
                        basePresentationTimeUs = presentationTimeUs
                    }
                    bufferInfo.presentationTimeUs = presentationTimeUs - basePresentationTimeUs
                    bufferInfo.flags = extractor.sampleFlags
                    muxer.writeSampleData(writeTrackIndex, buffer, bufferInfo)
                }

                extractor.advance()
            }

            muxer.stop()
            destFile.exists() && destFile.length() > 0
        } catch (_: Exception) {
            destFile.delete()
            false
        } finally {
            try { extractor.release() } catch (_: Exception) {}
            try { muxer?.release() } catch (_: Exception) {}
        }
    }
}
