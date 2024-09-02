package com.minimal.network.request

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.IOException
import okio.buffer

/**
 * Create by Qing at 2024/9/2 14:06
 */
internal class ProgressRequestBody(
    private val requestBody: RequestBody,
    private val onProgress: (Long, Long) -> Unit
): RequestBody() {

    @Throws(IOException::class)
    override fun contentLength(): Long {
        return requestBody.contentLength()
    }

    override fun contentType(): MediaType {
        return requestBody.contentType()!!
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val totalLength = contentLength()
        var currentLength = 0L
        val forwardingSink: ForwardingSink = object : ForwardingSink(sink) {
            @Throws(IOException::class)
            override fun write(source: Buffer, byteCount: Long) {
                currentLength += byteCount
                onProgress(totalLength, currentLength)
                super.write(source, byteCount)
            }
        }
        val buffer: BufferedSink = forwardingSink.buffer()
        requestBody.writeTo(buffer)
        buffer.flush()
    }
}