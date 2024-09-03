package com.minimal.network

import android.util.Log
import com.minimal.network.annotation.NetInternalAPI
import com.minimal.network.call.NetHttpCall
import com.minimal.network.call.Okhttp3Call
import com.minimal.network.converter.JacksonConverter
import com.minimal.network.converter.NetHttpConverter
import com.minimal.network.hook.HookInstance
import com.minimal.network.hook.HookRequest
import com.minimal.network.hook.HookResponse
import com.minimal.network.hook.HookResult
import com.minimal.network.request.MediaConst
import com.minimal.network.request.Request
import com.minimal.network.response.NetResult
import com.minimal.network.response.Response
import com.minimal.network.utils.Https
import com.minimal.network.utils.setEncrypt
import com.minimal.network.utils.trustSSLCertificate
import kotlinx.coroutines.CoroutineScope
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.logging.HttpLoggingInterceptor
import java.io.InterruptedIOException
import java.net.Proxy
import java.net.SocketException
import java.net.URLConnection
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLException

/**
 * Create by Qing at 2024/8/29 16:49
 */
object NetHelper {

    // 是否是debug模式
    var isDebug: Boolean? = false

    internal lateinit var scope: CoroutineScope
    internal lateinit var call: NetHttpCall
    internal lateinit var converter: NetHttpConverter
    internal var hookRequest: HookRequest = HookInstance
    internal var hookResponse: suspend HookResponse.(Response) -> Response = {
        it
    }
    internal var hookResult: suspend HookResult.(NetResult<*>) -> NetResult<*> = {
        it
    }

    /**
     * 初始化
     * @param scope 协程作用域
     * @param isDebug 是否是debug模式
     * @param call 网络请求实现
     * @param converter 数据转换器
     */
    @JvmOverloads
    fun init(
        scope: CoroutineScope,
        isDebug: Boolean? = false,
        call: NetHttpCall = Okhttp3Call {
            // 总超时时间：15秒
            callTimeout(15, TimeUnit.SECONDS)
            // 是否开启请求加密和响应校验
            setEncrypt(isDebug != true)
            if (isDebug == true) {
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            } else {
                // 非调试模式下不使用代理
                proxy(Proxy.NO_PROXY)
            }
            // 信任所有证书
            trustSSLCertificate()
        },
        // 默认使用Jackson转换器
        converter: NetHttpConverter = JacksonConverter()
    ){
        this.scope = scope
        this.isDebug = isDebug
        this.call = call
        this.converter = converter
    }

    fun hookRequest(hookRequest: HookRequest) {
        this.hookRequest = hookRequest
    }

    fun hookResponse(hookResponse: suspend HookResponse.(Response) -> Response) {
        this.hookResponse = hookResponse
    }

    fun hookResult(hookResult: suspend HookResult.(NetResult<*>) -> NetResult<*>) {
        this.hookResult = hookResult
    }

    internal suspend inline fun applyHookRequest(request: Request) {
        hookRequest.hook(request)
    }

    internal suspend inline fun applyHookResponse(response: Response): Response {
        return HookInstance.hookResponse(response)
    }

    @NetInternalAPI
    suspend fun applyHookResult(result: NetResult<*>): NetResult<*> {
        return HookInstance.hookResult(result)
    }

    /**
     * 获取文件类型
     */
    internal fun getMediaType(fName: String): MediaType {
        var contentType: String? = URLConnection.guessContentTypeFromName(fName)
        if (contentType.isNullOrEmpty()) {
            contentType = MediaConst.OCTET_STREAM.toString()
        }
        return contentType.toMediaType()
    }

    /**
     * 将参数拼接到url上
     */
    internal fun mergeParamsToUrl(url: String, params: Map<String, Any>?): String {
        return if (params != null) {
            val appendUrl = StringBuilder(url)
            val iterator = params.entries.iterator()
            appendUrl.append("?")
            while (iterator.hasNext()) {
                val (key, value) = iterator.next()
                appendUrl.append(key).append("=").append(value)
                if (!iterator.hasNext()) {
                    break
                }
                appendUrl.append("&")
            }
            appendUrl.toString()
        } else {
            url
        }
    }

    /**
     * 异常转换为失败信息
     */
    @NetInternalAPI
    fun exToFailInfo(ex: Exception): FailInfo {
        if (isDebug == true) {
            ex.printStackTrace()
        }
        val failInfo = when (ex) {
            is UnknownHostException -> {
                FailInfo(NetConfig.FAIL_CODE_UNKNOWN_HOST, "UnknownHostException")
            }
            is SSLException -> {
                FailInfo(NetConfig.FAIL_CODE_SSL_ERROR, "SSLException")
            }
            is InterruptedIOException -> {
                FailInfo(NetConfig.FAIL_CODE_NET_TIMEOUT, "InterruptedIOException")
            }
            is SocketException -> {
                FailInfo(NetConfig.FAIL_CODE_CONNECT_ERROR, "SocketException")
            }
            else -> {
                FailInfo(NetConfig.FAIL_CODE_UNKNOWN_ERROR,  "UnknownException")
            }
        }
        if (ex.message != null) {
            failInfo.msg = "${failInfo.msg}：${ex.message}"
        }
        return failInfo
    }

    @NetInternalAPI
    data class FailInfo(val code: Int, var msg: String)

    @JvmStatic
    fun debug(message: Any) {
        if (isDebug == true) {
            val adjustMessage = if(message is Throwable) {
                message.stackTraceToString()
            } else {
                val occurred = Throwable().stackTrace.getOrNull(1)?.run { " (${fileName}:${lineNumber})" } ?: ""
                message.toString() + occurred
            }
            Log.d(NetConfig.TAG, adjustMessage)
        }
    }
}