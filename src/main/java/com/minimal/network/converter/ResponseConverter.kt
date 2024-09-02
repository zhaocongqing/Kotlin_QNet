package com.minimal.network.converter

import com.minimal.network.annotation.NetInternalAPI
import com.minimal.network.response.NetResult
import com.minimal.network.response.Response
import java.lang.reflect.Type

/**
 * Create by Qing at 2024/8/30 13:54
 */
interface ResponseConverter {

    fun <T> convert(body: Response.Body, tType: Class<T>): T

    fun <T, RESULT: NetResult<T>> convertResult(body: Response.Body, resultType: Class<RESULT>, tType: Type): RESULT

    fun <T, RESULT: NetResult<List<T>>> convertResultList(body: Response.Body, resultType: Class<RESULT>, tType: Type): RESULT

    @NetInternalAPI
    fun <RESULT: NetResult<*>> convertResult(code: String, msg: String, data: Any? = null, resultType: Class<RESULT>): RESULT {
        val httpResult = try {
            val constructor = resultType.getConstructor(String::class.java, String::class.java, Any::class.java)
            constructor.isAccessible = true
            constructor.newInstance(code, msg, data)
        } catch (_: NoSuchMethodException) {
            try {
                val constructor = resultType.getConstructor(Int::class.java, String::class.java, Any::class.java)
                constructor.isAccessible = true
                constructor.newInstance(code.toInt(), msg, data)
            } catch (_: NoSuchMethodException) {
                throw IllegalArgumentException("Ensure that the type and order of constructor " +
                        "parameters of resultClass(RESULT: NetResult<T>) are (String, String, T) " +
                        "or (Int, String, T). Otherwise, convert cannot be completed internally.")
            }
        }
        return httpResult
    }
}