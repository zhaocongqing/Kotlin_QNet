package com.minimal.network.converter

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.minimal.network.request.MediaConst
import com.minimal.network.response.NetResult
import com.minimal.network.response.Response
import java.lang.reflect.Type

/**
 * Create by Qing at 2024/9/2 13:13
 */
class GsonConverter(
    private var _gson: Gson? = null,
    onConfiguration: GsonBuilder.() -> Unit = {}
): NetHttpConverter {

    override val contentType: String = MediaConst.JSON.toString()
    private val gson: Gson
        get() = _gson!!

    init {
        if (_gson == null) {
            val builder = GsonBuilder().apply {
                onConfiguration()
            }
            _gson = builder.create()
        }
    }

    override fun <T> convert(body: Response.Body, tType: Class<T>): T {
        return gson.fromJson(body.string(), tType)
    }

    override fun <T, RESULT : NetResult<T>> convertResult(body: Response.Body, resultType: Class<RESULT>, tType: Type): RESULT {
        val realType = ParameterizedTypeImpl(resultType, tType)
        @Suppress("UNCHECKED_CAST")
        return gson.fromJson(body.string(), TypeToken.get(realType)) as RESULT
    }

    override fun <T, RESULT : NetResult<List<T>>> convertResultList(body: Response.Body, resultType: Class<RESULT>, tType: Type): RESULT {
        val realType = ParameterizedTypeImpl(resultType, ParameterizedTypeImpl(List::class.java, tType))
        @Suppress("UNCHECKED_CAST")
        return gson.fromJson(body.string(), TypeToken.get(realType)) as RESULT
    }

    override fun <T> convert(value: T, tType: Class<out T>): ByteArray {
        return gson.toJson(value).toByteArray()
    }
}