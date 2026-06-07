package io.testpulse.sdk.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CrashData(
    val sessionUuid: String?,
    val exceptionType: String,
    val message: String,
    val stackTrace: String,
    val timestamp: String,
    val sdkVersion: String? = null
)
