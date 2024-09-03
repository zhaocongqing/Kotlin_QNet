package com.minimal.network

import com.minimal.network.interceptor.RequestInterceptor

/**
 * Create by Qing at 2024/7/22 16:54
 */
object NetConfig {

    const val TAG = "QNet"

    /**
     * 是否开启请求加密和响应校验，默认开启
     */
    var isEncrypt = true

    /**
     * 请求拦截器(先接收到请求，后接收到响应)集合，按顺序添加
     */
    var requestInterceptorList: MutableList<RequestInterceptor> = mutableListOf()

    // 请求成功的code
    const val SUCCESS_CODE = "200"
    // 网框层请求失败的code
    const val FAIL_CODE_UNKNOWN_HOST = -5001
    const val FAIL_CODE_SSL_ERROR = -5002
    const val FAIL_CODE_NET_TIMEOUT = -5003
    const val FAIL_CODE_CONNECT_ERROR = -5004
    const val FAIL_CODE_UNKNOWN_ERROR = -5005
    const val FAIL_CODE_PARSER_ERROR = -5006
}