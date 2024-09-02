package com.minimal.network.request

import okhttp3.MediaType.Companion.toMediaType

/**
 * Create by Qing at 2024/8/29 17:34
 */
object MediaConst {

    val IMG = "image/*".toMediaType()

    val GIF = "image/gif".toMediaType()

    val JPEG = "image/jpeg".toMediaType()

    val PNG = "image/png".toMediaType()

    val MP4 = "video/mpeg".toMediaType()

    val TXT = "text/plain".toMediaType()

    val JSON = "application/json; charset=utf-8".toMediaType()

    val XML = "application/xml".toMediaType()

    val HTML = "text/html".toMediaType()

    val FORM = "multipart/form-data".toMediaType()

    val OCTET_STREAM = "application/octet-stream".toMediaType()

    val URLENCODED = "application/x-www-form-urlencoded".toMediaType()
}