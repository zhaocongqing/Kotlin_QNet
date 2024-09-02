package com.minimal.network

import com.minimal.network.annotation.NetInternalAPI
import com.minimal.network.converter.ResponseConverter
import com.minimal.network.request.Method
import com.minimal.network.request.Request
import com.minimal.network.response.NetResult
import com.minimal.network.response.Response
import com.minimal.network.response.result
import com.minimal.network.response.resultList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * 使用协程扩展Http
 * Create by Qing at 2024/8/29 17:17
 */
class NetHttp private constructor(
    internal val request: Request,
    private val block: suspend Request.() -> Unit) {

    companion object {
        fun get(url: String, block: suspend Request.() -> Unit ={}): NetHttp {
            return request(url, Method.GET.name, block)
        }

        fun head(url: String, block: suspend Request.() -> Unit = {}): NetHttp {
            return request(url, Method.HEAD.name, block)
        }

        fun post(url: String, block: suspend Request.() -> Unit = {}): NetHttp {
            return request(url, Method.POST.name, block)
        }

        fun delete(url: String, block: suspend Request.() -> Unit = {}): NetHttp {
            return request(url, Method.DELETE.name, block)
        }

        fun put(url: String, block: suspend Request.() -> Unit = {}): NetHttp {
            return request(url, Method.PUT.name, block)
        }

        fun patch(url: String, block: suspend Request.() -> Unit = {}): NetHttp {
            return request(url, Method.PATCH.name, block)
        }

        fun request(url: String, method: String, block: suspend Request.() -> Unit = {}): NetHttp {
            return NetHttp(Request(url, method), block)
        }

        fun request(request: Request): NetHttp {
            return NetHttp(request){}
        }
    }

    @NetInternalAPI
    var scope: CoroutineScope = NetHelper.scope
        private set

    @NetInternalAPI
    var respConverter: ResponseConverter = NetHelper.converter
        private set

    /**
     * 自定义作用域
     */
    @OptIn(NetInternalAPI::class)
    fun scope(scope: CoroutineScope) = apply {
        this.scope = scope
    }

    /**
     * 设置响应转换器, 默认使用NetHelper中的转换器, 如果需要单独设置, 可自定义转换Response to NetResult
     */
    @OptIn(NetInternalAPI::class)
    fun responseConverter(converter: ResponseConverter) = apply {
        this.respConverter = converter
    }

    @OptIn(NetInternalAPI::class)
    inline fun launch(crossinline responseBlock: suspend CoroutineScope.(Response) -> Unit) = scope.launch {
        responseBlock(await())
    }

    @OptIn(NetInternalAPI::class)
    inline fun <reified T, reified RESULT: NetResult<T>> launchResult(
        crossinline resultBlock: suspend CoroutineScope.(RESULT) -> Unit) = scope.launch {
        resultBlock(awaitResult())
    }

    @OptIn(NetInternalAPI::class)
    inline fun <reified T, reified RESULT: NetResult<List<T>>> launchResultList(
        crossinline resultBlock: suspend CoroutineScope.(RESULT) -> Unit) = scope.launch {
        resultBlock(awaitResultList())
    }

    @OptIn(NetInternalAPI::class)
    suspend fun await(): Response = withContext(Dispatchers.IO) {
        awaitImpl()
    }

    @OptIn(NetInternalAPI::class)
    suspend inline fun <reified T, reified RESULT: NetResult<T>> awaitResult(): RESULT = withContext(Dispatchers.IO) {
        awaitImpl().result<T, RESULT>()
    }

    @OptIn(NetInternalAPI::class)
    suspend inline fun <reified T, reified RESULT: NetResult<List<T>>> awaitResultList(): RESULT = withContext(Dispatchers.IO) {
        awaitImpl().resultList<T, RESULT>()
    }

    @OptIn(NetInternalAPI::class)
    fun async(): Deferred<Response> = scope.async(Dispatchers.IO) {
        awaitImpl()
    }

    @OptIn(NetInternalAPI::class)
    inline fun <reified T, reified RESULT: NetResult<T>> asyncResult(): Deferred<RESULT> = scope.async(Dispatchers.IO) {
        awaitImpl().result<T, RESULT>()
    }

    @OptIn(NetInternalAPI::class)
    inline fun <reified T, reified RESULT: NetResult<List<T>>> asyncResultList(): Deferred<RESULT> = scope.async(Dispatchers.IO) {
        awaitImpl().resultList<T, RESULT>()
    }

    @OptIn(NetInternalAPI::class)
    suspend fun asFlow(): Flow<Response> = flow {
        emit(awaitImpl())
    }.flowOn(Dispatchers.IO)

    @OptIn(NetInternalAPI::class)
    suspend inline fun <reified T, reified RESULT: NetResult<T>> resultAsFlow(): Flow<RESULT> = flow {
        emit(awaitImpl().result<T, RESULT>())
    }.flowOn(Dispatchers.IO)

    @OptIn(NetInternalAPI::class)
    suspend inline fun <reified T, reified RESULT: NetResult<List<T>>> resultListAsFlow(): Flow<RESULT> = flow {
        emit(awaitImpl().resultList<T, RESULT>())
    }.flowOn(Dispatchers.IO)

    @NetInternalAPI
    suspend fun awaitImpl(): Response {
        var response = try {
            if (!request.reqCall) {//避免重新请求时多次调用
                request.block()
            }
            // Hook and Execute request
            request.let {
                NetHelper.applyHookRequest(it)
                NetHelper.call.await(it)
            }
        } catch (ie: IOException) {
            val failInfo = NetHelper.exToFailInfo(ie)
            Response(failInfo.code, failInfo.msg, null)
        }
        response.client = this
        response = NetHelper.applyHookResponse(response)
        if (response.isRetry) {
            return awaitImpl()
        }
        return response
    }

}