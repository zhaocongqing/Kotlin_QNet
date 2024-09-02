package com.minimal.network.response

import com.minimal.network.NetConfig
import com.minimal.network.NetHelper
import com.minimal.network.NetHttp
import com.minimal.network.annotation.NetInternalAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.Reader

/**
 * Create by Qing at 2024/8/29 17:07
 */
data class Response(val code: Int, val message: String, val body: Body?) {

    @NetInternalAPI
    lateinit var client: NetHttp

    // 是否重新请求
    @NetInternalAPI
    var isRetry: Boolean = false
        internal set(value){
            field = value
            client.request.reqCall = field
        }

    val isSuccessful: Boolean
        get() = code in 200..299

    abstract class Body {

        abstract fun string(): String

        open fun bytes(): ByteArray {
            throw IllegalArgumentException("This response body does not support byte arrays")
        }

        open fun charStream(): Reader {
            throw IllegalArgumentException("This response body does not support char streams")
        }

        open fun byteStream(): InputStream {
            throw IllegalArgumentException("This response body does not support byte streams")
        }
    }
}

suspend inline fun <reified T> Response.body(): T {
    return bodyOrNull(T::class.java)!!
}

suspend fun <T> Response.body(type: Class<T>): T {
    return bodyOrNull(type)!!
}

suspend inline fun <reified T> Response.bodyOrNull(): T? {
    return bodyOrNull(T::class.java)
}

@OptIn(NetInternalAPI::class)
suspend fun <T> Response.bodyOrNull(type: Class<T>): T? = withContext(Dispatchers.IO) {
    if (body == null) {
        return@withContext null
    }
    try {
        if (isBasicType(type)) {
            @Suppress("UNCHECKED_CAST")
            body.string() as T
        } else {
            client.respConverter.convert(body, type)
        }
    } catch (ex: Exception) {
        if (NetHelper.isDebug == true) {
            ex.printStackTrace()
        }
        null
    }
}

@OptIn(NetInternalAPI::class)
suspend inline fun <reified T, reified RESULT: NetResult<T>> Response.result(): RESULT = withContext(Dispatchers.IO) {
    var result = convertResult<T, RESULT>(this@result)
    result.response = this@result
    result = NetHelper.applyHookResult(result) as RESULT
    if (result.response.isRetry) {
        convertResult<T, RESULT>(client.awaitImpl())
    } else {
        result
    }
}

@NetInternalAPI
inline fun <reified T, reified RESULT: NetResult<T>> convertResult(response: Response): RESULT {
    return if (response.isSuccessful && response.body != null) {
        try {
            if (isBasicType(T::class.java)) {
                response.client.respConverter.convertResult(response.code.toString(), response.message, response.body.string() as T, RESULT::class.java)
            } else {
                response.client.respConverter.convertResult(response.body, RESULT::class.java, T::class.java)
            }
        } catch (ex: Exception) {
            if (NetHelper.isDebug == true) {
                ex.printStackTrace()
            }
            response.client.respConverter.convertResult(NetConfig.FAIL_CODE_PARSER_ERROR.toString(), "数据解析异常", resultType=RESULT::class.java)
        }
    } else {
        response.client.respConverter.convertResult(response.code.toString(), response.message, resultType=RESULT::class.java)
    }
}

@OptIn(NetInternalAPI::class)
suspend inline fun <reified T, reified RESULT: NetResult<List<T>>> Response.resultList(): RESULT = withContext(Dispatchers.IO) {
    var result = convertResultList<T, RESULT>(this@resultList)
    result.response = this@resultList
    result = NetHelper.applyHookResult(result) as RESULT
    if (result.response.isRetry) {
        convertResultList<T, RESULT>(client.awaitImpl())
    } else {
        result
    }
}

@NetInternalAPI
inline fun <reified T, reified RESULT: NetResult<List<T>>> convertResultList(response: Response): RESULT {
    return if (response.isSuccessful && response.body != null) {
        try {
            response.client.respConverter.convertResultList(response.body, RESULT::class.java, T::class.java)
        } catch (ex: Exception) {
            if (NetHelper.isDebug == true) {
                ex.printStackTrace()
            }
            response.client.respConverter.convertResult(NetConfig.FAIL_CODE_PARSER_ERROR.toString(), "数据解析异常", resultType=RESULT::class.java)
        }
    } else {
        response.client.respConverter.convertResult(response.code.toString(), response.message, resultType=RESULT::class.java)
    }
}

@NetInternalAPI
fun <T> isBasicType(type: Class<T>): Boolean {
    return type == String::class.java || type == Int::class.java || type == Long::class.java ||
            type == Boolean::class.java || type == Double::class.java || type == Float::class.java
}

