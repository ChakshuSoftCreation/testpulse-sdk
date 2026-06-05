package io.testpulse.sdk.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CustomEventData(
    val eventName: String,
    val eventData: Map<String, String>? = null,
    val timestamp: String
)
