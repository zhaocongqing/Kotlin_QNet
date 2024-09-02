package com.minimal.network

/**
 * Create by Qing at 2024/7/22 16:54
 */
object NetConfig {

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