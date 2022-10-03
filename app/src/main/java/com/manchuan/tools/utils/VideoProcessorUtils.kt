package com.manchuan.tools.utils

import android.annotation.SuppressLint
import kotlin.Throws
import android.media.MediaMuxer
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaCodec
import java.io.IOException
import java.nio.ByteBuffer

object VideoProcessorUtils {
    //从视频中分离音频
    @SuppressLint("WrongConstant")
    @Throws(IOException::class)
    fun splitAudioFile(inputPath: String?, audioPath: String?) {
        var mediaMuxer: MediaMuxer? = null
        val mediaExtractor = MediaExtractor()
        mediaExtractor.setDataSource(inputPath!!)
        var audioTrackIndex = -1
        for (index in 0 until mediaExtractor.trackCount) {
            val format = mediaExtractor.getTrackFormat(index)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (!mime!!.startsWith("audio/")) {
                continue
            }
            mediaExtractor.selectTrack(index)
            mediaMuxer = MediaMuxer(audioPath!!, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            audioTrackIndex = mediaMuxer.addTrack(format)
            mediaMuxer.start()
        }
        if (mediaMuxer == null) {
            return
        }
        val info = MediaCodec.BufferInfo()
        info.presentationTimeUs = 0
        val buffer = ByteBuffer.allocate(500 * 1024)
        var sampleSize = 0
        while (mediaExtractor.readSampleData(buffer, 0).also { sampleSize = it } > 0) {
            info.offset = 0
            info.size = sampleSize
            info.flags = mediaExtractor.sampleFlags
            info.presentationTimeUs = mediaExtractor.sampleTime
            mediaMuxer.writeSampleData(audioTrackIndex, buffer, info)
            mediaExtractor.advance()
        }
        mediaExtractor.release()
        mediaMuxer.stop()
        mediaMuxer.release()
    }

}