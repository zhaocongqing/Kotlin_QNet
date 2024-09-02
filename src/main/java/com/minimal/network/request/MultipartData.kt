package com.minimal.network.request

import java.io.File

/**
 * Create by Qing at 2024/8/30 13:30
 */
sealed interface PartData {
    val name: String
    val value: String?
}

data class StringPart internal constructor(
    override val name: String,
    override val value: String
) : PartData

data class FilePart internal constructor(
    override val name: String,
    override val value: String?,
    val data: File,
    val contentType: String?
) : PartData

data class ByteArrayPart internal constructor(
    override val name: String,
    override val value: String?,
    val data: ByteArray,
    val contentType: String?
) : PartData {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ByteArrayPart

        if (name != other.name) return false
        if (value != other.value) return false
        if (!data.contentEquals(other.data)) return false
        if (contentType != other.contentType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (value?.hashCode() ?: 0)
        result = 31 * result + data.contentHashCode()
        result = 31 * result + (contentType?.hashCode() ?: 0)
        return result
    }
}
