/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lzx.extension_flac2120

import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.ParserException
import com.google.android.exoplayer2.decoder.DecoderInputBuffer
import com.google.android.exoplayer2.decoder.SimpleDecoder
import com.google.android.exoplayer2.decoder.SimpleDecoderOutputBuffer
import com.google.android.exoplayer2.extractor.FlacStreamMetadata
import com.google.android.exoplayer2.util.Util
import com.lzx.extension_flac2120.FlacDecoderException
import com.lzx.extension_flac2120.FlacDecoderJni.FlacFrameDecodeException
import java.io.IOException
import java.nio.ByteBuffer

/**
 * Flac decoder.
 */
/* package */
internal class FlacDecoder(
    numInputBuffers: Int,
    numOutputBuffers: Int,
    maxInputBufferSize: Int,
    initializationData: List<ByteArray?>,
) : SimpleDecoder<DecoderInputBuffer, SimpleDecoderOutputBuffer, FlacDecoderException>(arrayOfNulls<DecoderInputBuffer>(
    numInputBuffers).requireNoNulls(),
    arrayOfNulls<SimpleDecoderOutputBuffer>(numOutputBuffers).requireNoNulls()) {
    /** Returns the [FlacStreamMetadata] decoded from the initialization data.  */
    var streamMetadata: FlacStreamMetadata? = null
    private val decoderJni: FlacDecoderJni

    /**
     * Creates a Flac decoder.
     *
     * @param numInputBuffers The number of input buffers.
     * @param numOutputBuffers The number of output buffers.
     * @param maxInputBufferSize The maximum required input buffer size if known, or [     ][Format.NO_VALUE] otherwise.
     * @param initializationData Codec-specific initialization data. It should contain only one entry
     * which is the flac file header.
     * @throws FlacDecoderException Thrown if an exception occurs when initializing the decoder.
     */
    init {
        if (initializationData.size != 1) {
            throw FlacDecoderException("Initialization data must be of length 1")
        }
        decoderJni = FlacDecoderJni()
        decoderJni.setData(initializationData[0]?.let { ByteBuffer.wrap(it) })
        streamMetadata = try {
            decoderJni.decodeStreamMetadata()
        } catch (e: ParserException) {
            throw FlacDecoderException("Failed to decode StreamInfo", e)
        } catch (e: IOException) {
            // Never happens.
            throw IllegalStateException(e)
        }
        val initialInputBufferSize =
            if (maxInputBufferSize != Format.NO_VALUE) maxInputBufferSize else streamMetadata?.maxFrameSize
        if (initialInputBufferSize != null) {
            setInitialInputBufferSize(initialInputBufferSize)
        }
    }

    override fun getName(): String {
        return "libflac"
    }

    override fun createInputBuffer(): DecoderInputBuffer {
        return DecoderInputBuffer(DecoderInputBuffer.BUFFER_REPLACEMENT_MODE_NORMAL)
    }

    override fun createOutputBuffer(): SimpleDecoderOutputBuffer {
        return SimpleDecoderOutputBuffer { outputBuffer: SimpleDecoderOutputBuffer? ->
            releaseOutputBuffer(outputBuffer!!)
        }
    }

    override fun createUnexpectedDecodeException(error: Throwable): FlacDecoderException {
        return FlacDecoderException("Unexpected decode error", error)
    }

    /**
     * Decodes the `inputBuffer` and stores any decoded output in `outputBuffer`.
     *
     * @param inputBuffer  The buffer to decode.
     * @param outputBuffer The output buffer to store decoded data. The flag  will be set if the same flag is set on `inputBuffer`, but
     * may be set/unset as required. If the flag is set when the call returns then the output
     * buffer will not be made available to dequeue. The output buffer may not have been populated
     * in this case.
     * @param reset        Whether the decoder must be reset before decoding.
     * @return A decoder exception if an error occurred, or null if decoding was successful.
     */
    override fun decode(
        inputBuffer: DecoderInputBuffer,
        outputBuffer: SimpleDecoderOutputBuffer,
        reset: Boolean,
    ): FlacDecoderException? {
        if (reset) {
            decoderJni.flush()
        }
        decoderJni.setData(Util.castNonNull(inputBuffer.data))
        val outputData = outputBuffer.init(inputBuffer.timeUs, streamMetadata!!.maxDecodedFrameSize)
        try {
            decoderJni.decodeSample(outputData)
        } catch (e: FlacFrameDecodeException) {
            return FlacDecoderException("Frame decoding failed", e)
        } catch (e: IOException) {
            // Never happens.
            throw IllegalStateException(e)
        }
        return null
    }

    override fun release() {
        super.release()
        decoderJni.release()
    }
}