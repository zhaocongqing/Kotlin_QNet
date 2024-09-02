package com.minimal.network.response

import com.minimal.network.NetConfig
import com.minimal.network.annotation.NetInternalAPI

/**
 * 请求返回结果基类, T为返回数据类型
 * 调用者可实现自己的基类，属性名称无限制，但构造器（参数顺序及个数）必须包含与NetResult一致的构造器
 * Create by Qing at 2024/8/30 13:55
 */
abstract class NetResult<T>(
    private val code: String,
    private val msg: String,
    private val data: T?
) {
    @NetInternalAPI
    lateinit var response: Response
    val success: Boolean = code == NetConfig.SUCCESS_CODE
}

/**
 * Http请求返回结果默认实现
 * Create by Qing at 2024/8/30 13:55
 */
data class HttpResult<T>(
    val code: String,
    val msg: String,
    val data: T?
): NetResult<T>(code, msg, data)