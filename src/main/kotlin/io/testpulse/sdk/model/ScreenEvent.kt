package io.testpulse.sdk.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ScreenEvent(
    val screenName: String,
    val enteredAt: String,
    val exitedAt: String? = null,
    val durationSec: Int? = null
)
