package com.minimal.network.converter

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.json.JsonWriteFeature
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.minimal.network.request.MediaConst
import com.minimal.network.response.NetResult
import com.minimal.network.response.Response
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Create by Qing at 2024/9/2 13:25
 */
class JacksonConverter(
    private var _jsonMapper: JsonMapper? = null,
    onConfiguration: JsonMapper.Builder.() -> Unit = {}
): NetHttpConverter {

    override val contentType: String = MediaConst.JSON.toString()
    private val jsonMapper: JsonMapper
        get() = _jsonMapper!!

    init {
        if (_jsonMapper == null) {
            val builder = JsonMapper.builder().apply {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                configure(JsonWriteFeature.WRITE_NAN_AS_STRINGS, true)
                configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                defaultDateFormat(SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()))
                serializationInclusion(JsonInclude.Include.NON_NULL)
                onConfiguration()
                addModule(KotlinModule.Builder().build())
            }
            _jsonMapper = builder.build()
        }
    }

    override fun <T> convert(body: Response.Body, tType: Class<T>): T {
        return jsonMapper.readValue(body.string(), JacksonType(tType))
    }

    override fun <T, RESULT : NetResult<T>> convertResult(body: Response.Body, resultType: Class<RESULT>, tType: Type): RESULT {
        val realType = ParameterizedTypeImpl(resultType, tType)
        return jsonMapper.readValue(body.string(), JacksonType(realType))
    }

    override fun <T, RESULT : NetResult<List<T>>> convertResultList(body: Response.Body, resultType: Class<RESULT>, tType: Type): RESULT {
        val realType = ParameterizedTypeImpl(resultType, ParameterizedTypeImpl(List::class.java, tType))
        return jsonMapper.readValue(body.string(), JacksonType(realType))
    }

    override fun <T> convert(value: T, tClass: Class<out T>): ByteArray {
        return jsonMapper.writeValueAsBytes(value)
    }
}