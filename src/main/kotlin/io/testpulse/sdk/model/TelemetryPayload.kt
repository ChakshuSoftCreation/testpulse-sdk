package io.testpulse.sdk.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TelemetryPayload(
    val deviceUuid: String,
    val sessions: List<SessionEvent>,
    val crashes: List<CrashData> = emptyList(),
    val sdkVersion: String? = null
)
