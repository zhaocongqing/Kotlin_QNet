package com.minimal.network.annotation

import android.annotation.SuppressLint


/**
 * 内部注释
 */
@SuppressLint("ExperimentalAnnotationRetention")
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This API is internal to QNet and should not be used from outside"
)

@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.TYPEALIAS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.CONSTRUCTOR
)

internal annotation class NetInternalAPI