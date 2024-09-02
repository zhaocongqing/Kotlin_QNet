package com.minimal.network.request

import com.minimal.network.NetHelper
import com.minimal.network.converter.RequestBodyConverter
import java.io.File
import java.lang.IllegalArgumentException

/**
 * Create by Qing at 2024/8/30 13:38
 */
class Request internal constructor(val url: String, val method: String) {

    // 请求标记
    private var _tag: Any? = null
    // 请求头
    private var _headers: MutableMap<String, String>? = null
    // 请求参数
    private var _params: MutableMap<String, Any>? = null
    // 请求体
    private var _body: Body<*>? = null
        set(value) {
            if (field != null) {
                throw IllegalArgumentException("The body cannot be set repeatedly!")
            }
            field = value
        }

    val tag: Any?
        get() = _tag

    val headers: Map<String, String>?
        get() = _headers

    val params: Map<String, Any>?
        get() = _params

    val body: Body<*>?
        get() = _body

    // 如果是GET请求将参数合并到Url上
    var mergeParamsToUrl = method == Method.GET.name
    // body转换器
    var bodyConverter: RequestBodyConverter = NetHelper.converter
    /**
     * 文件上传进度监听(totalLength, currentLength) -> Unit
     */
    var onProgress: ((Long, Long) -> Unit)? = null

    internal var reqCall = false

    fun tag(tag: Any?) {
        _tag = tag
    }

    fun header(name: String, value: String) {
        if (_headers == null) {
            _headers = HashMap()
        }
        _headers!![name] = value
    }

    fun headers(headers: Map<String, String>) {
        if (_headers == null) {
            _headers = HashMap()
        }
        _headers!!.putAll(headers)
    }

    fun param(key: String, value: Any) {
        if (_params == null) {
            _params = HashMap()
        }
        _params!![key] = value
    }

    fun params(params: Map<String, Any>) {
        if (_params == null) {
            _params = HashMap()
        }
        _params!!.putAll(params)
    }

    fun setBody(body: String, contentType: String = MediaConst.JSON.toString()) {
        _body = StringBody(body, contentType)
    }

    fun <T> setBody(body: T, tType: Class<T>, contentType: String = MediaConst.JSON.toString(),
                    bodyConverter: RequestBodyConverter? = null) {
        _body = EntityBody(body, tType, contentType)
        bodyConverter?.let { this.bodyConverter = it }
    }

    fun setBody(body: File, contentType: String = MediaConst.OCTET_STREAM.toString()) {
        _body = FileBody(body, contentType)
    }

    fun setBody(body: ByteArray, contentType: String = MediaConst.OCTET_STREAM.toString()) {
        _body = ByteArrayBody(body, contentType)
    }

    fun formBody(block: FormBody.() -> Unit = {}) {
        _body = FormBody(mutableListOf(), MediaConst.URLENCODED.toString()).apply(block)
    }

    fun multipartBody(type: String = MediaConst.FORM.toString(),
                      block: MultipartBody.() -> Unit) {
        _body = MultipartBody(mutableListOf(), type).apply(block)
    }

    fun containsHeader(key: String): Boolean {
        return _headers?.containsKey(key) ?: false
    }

    fun containsParam(key: String): Boolean {
        return _params?.containsKey(key) ?: false
    }

    override fun toString(): String {
        val strBuilder = StringBuilder()
        strBuilder.append("url='").append(url).append('\'')
        if (_tag != null) {
            strBuilder.append(", tags=").append(_tag)
        }
        if (_headers != null) {
            strBuilder.append(", headers=").append(_headers)
        }
        if (_params != null) {
            strBuilder.append(", params=").append(_params)
        }
        return strBuilder.toString()
    }
}