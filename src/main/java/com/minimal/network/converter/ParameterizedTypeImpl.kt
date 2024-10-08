package com.minimal.network.converter

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Create by Qing at 2024/9/2 13:08
 */
class ParameterizedTypeImpl(
    private val ownerType: Type? = null,
    private val rawType: Class<*>,
    vararg typeArguments: Type
): ParameterizedType {

    constructor(rawType: Class<*>, vararg typeArguments: Type): this(null, rawType, *typeArguments)

    private val typeArguments: Array<Type>

    init {
        this.typeArguments = arrayOf(*typeArguments)
    }

    override fun getActualTypeArguments(): Array<Type> {
        return typeArguments
    }

    override fun getRawType(): Type {
        return rawType
    }

    override fun getOwnerType(): Type? {
       return ownerType
    }
}