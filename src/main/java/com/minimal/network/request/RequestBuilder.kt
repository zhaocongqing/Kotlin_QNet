package com.minimal.network.request

import com.minimal.network.NetHelper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Create by Qing at 2024/9/2 13:46
 */
fun Request.buildOkhttp3Request(): okhttp3.Request {
    val builder = okhttp3.Request.Builder().url(url)
    if (mergeParamsToUrl) {
        builder.url(NetHelper.mergeParamsToUrl(url, params))
    } else {
        builder.url(url)
    }
    headers?.forEach { builder.addHeader(it.key, it.value) }
    var okhttpReqBody = when(body) {
        is StringBody -> {
            (body as StringBody).content.toRequestBody(body!!.contentType.toMediaTypeOrNull())
        }
        is FileBody -> {
            (body as FileBody).content.asRequestBody(body!!.contentType.toMediaTypeOrNull())
        }
        is ByteArrayBody -> {
            (body as ByteArrayBody).content.toRequestBody(body!!.contentType.toMediaTypeOrNull())
        }
        is EntityBody<*> -> {
            val entityBody = (body as EntityBody<*>)
            bodyConverter.convert(entityBody.content, entityBody.tType).toRequestBody(bodyConverter.contentType.toMediaTypeOrNull())
        }
        is FormBody -> {
            val formBody = body as FormBody
            params?.forEach { formBody.content.add(StringPart(it.key, it.value.toString())) }
            val bodyBuilder = okhttp3.FormBody.Builder()
            if (formBody.encoded) {
                formBody.content.forEach{ bodyBuilder.add(it.name, it.value!!) }
            } else {
                formBody.content.forEach{ bodyBuilder.addEncoded(it.name, it.value!!) }
            }
            bodyBuilder.build()
        }
        is MultipartBody -> {
            val multipartBody = body as MultipartBody
            params?.forEach { multipartBody.content.add(StringPart(it.key, it.value.toString())) }
            val bodyBuilder = okhttp3.MultipartBody.Builder().setType(body!!.contentType.toMediaType())
            for (part in multipartBody.content) {
                when(part){
                    is StringPart -> {
                        bodyBuilder.addFormDataPart(part.name, part.value)
                    }
                    is FilePart -> {
                        if (!part.data.exists() || !part.data.isFile) continue
                        val requestBody = if (part.contentType != null) {
                            part.data.asRequestBody(part.contentType.toMediaTypeOrNull())
                        } else {
                            part.data.asRequestBody(NetHelper.getMediaType(part.data.name))
                        }
                        bodyBuilder.addFormDataPart(part.name, part.value, requestBody)
                    }
                    is ByteArrayPart -> {
                        bodyBuilder.addFormDataPart(part.name, part.value, part.data.toRequestBody(part.contentType?.toMediaTypeOrNull()))
                    }
                }
            }
            bodyBuilder.build()
        }
        else -> {
            if (!mergeParamsToUrl && params != null) {
                bodyConverter.convert(params, Map::class.java).toRequestBody(bodyConverter.contentType.toMediaTypeOrNull())
            } else {
                null
            }
        }
    }
    if (okhttpReqBody != null && onProgress != null) {
        okhttpReqBody = ProgressRequestBody(okhttpReqBody, onProgress!!)
    }
    return builder.method(method, okhttpReqBody).tag(tag).build()
}