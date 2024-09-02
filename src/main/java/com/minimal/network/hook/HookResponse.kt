package com.minimal.network.hook

import com.minimal.network.annotation.NetInternalAPI
import com.minimal.network.request.Request
import com.minimal.network.response.Response

/**
 * Create by Qing at 2024/9/2 11:14
 */
interface HookResponse {

    @OptIn(NetInternalAPI::class)
    val Response.request: Request
        get() = client.request

    @OptIn(NetInternalAPI::class)
    fun Response.setRetryCall() {
        this.isRetry = true
    }
}