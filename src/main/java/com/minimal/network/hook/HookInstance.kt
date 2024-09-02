package com.minimal.network.hook

import com.minimal.network.request.Request

/**
 * Create by Qing at 2024/9/2 11:24
 */
internal object HookInstance: HookRequest, HookResponse, HookResult {

    override suspend fun hook(request: Request) {
        // do nothing
    }
}