package com.minimal.network.converter

/**
 * Create by Qing at 2024/8/30 13:51
 */
interface RequestBodyConverter {

    val contentType: String

    fun <T> convert(value: T, tType: Class<out T>): ByteArray
}