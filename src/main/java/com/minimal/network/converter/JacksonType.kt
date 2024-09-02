package com.minimal.network.converter

import com.fasterxml.jackson.core.type.TypeReference
import java.lang.reflect.Type

/**
 * Create by Qing at 2024/9/2 13:25
 */
class JacksonType<T>(private val type: Type): TypeReference<T>() {
    override fun getType(): Type {
        return type
    }
}