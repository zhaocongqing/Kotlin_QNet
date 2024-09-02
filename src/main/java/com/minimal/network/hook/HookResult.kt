package com.minimal.network.hook

import com.minimal.network.annotation.NetInternalAPI
import com.minimal.network.request.Request
import com.minimal.network.response.NetResult

/**
 * Create by Qing at 2024/9/2 10:47
 */
interface HookResult {

    @OptIn(NetInternalAPI::class)
    val NetResult<*>.request: Request
        get() = response.client.request

    @OptIn(NetInternalAPI::class)
    fun NetResult<*>.setRetryCall() {
        response.isRetry = true
    }
}