package com.minimal.network.call

import com.minimal.network.request.Request
import com.minimal.network.response.Response


/**
 * Create by Qing at 2024/8/29 16:58
 */
interface NetHttpCall {
    suspend fun await(request: Request): Response
}