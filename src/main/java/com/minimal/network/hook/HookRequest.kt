package com.minimal.network.hook

import com.minimal.network.request.Request

/**
 * 请求勾子，可以自定义添加 header、params信息
 * Create by Qing at 2024/9/2 11:22
 */
fun interface HookRequest {
    suspend fun hook(request: Request)
}