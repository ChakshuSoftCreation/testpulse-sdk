package io.testpulse.sdk.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeviceInfo(
    val deviceModel: String,
    val manufacturer: String,
    val osVersion: String,
    val sdkInt: Int,
    val appVersion: String,
    val appVersionCode: Long,
    val screenResolution: String,
    val locale: String,
    val networkType: String
)
