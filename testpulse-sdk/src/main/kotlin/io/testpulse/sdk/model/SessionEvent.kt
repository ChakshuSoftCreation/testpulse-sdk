package io.testpulse.sdk.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SessionEvent(
    val sessionUuid: String,
    val startTime: String,
    val endTime: String? = null,
    val durationSec: Int? = null,
    val screens: List<ScreenEvent> = emptyList(),
    val events: List<CustomEventData> = emptyList()
)
