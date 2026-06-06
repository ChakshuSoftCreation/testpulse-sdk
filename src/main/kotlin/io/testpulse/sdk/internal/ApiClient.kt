package io.testpulse.sdk.internal

import io.testpulse.sdk.model.DeviceInfo
import io.testpulse.sdk.model.TelemetryPayload
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

class ApiClient(private val apiKey: String, private val baseUrl: String) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun registerTester(
        deviceUuid: String,
        deviceFingerprint: String,
        alias: String,
        deviceInfo: DeviceInfo
    ): Boolean {
        return try {
            val json = moshi.adapter(Map::class.java).toJson(mapOf(
                "deviceUuid" to deviceUuid,
                "deviceFingerprint" to deviceFingerprint,
                "alias" to alias,
                "deviceModel" to deviceInfo.deviceModel,
                "osVersion" to deviceInfo.osVersion,
                "appVersion" to deviceInfo.appVersion,
                "screenResolution" to deviceInfo.screenResolution,
                "locale" to deviceInfo.locale
            ))

            val request = Request.Builder()
                .url("$baseUrl/api/v1/ingest/register-tester")
                .header("X-API-Key", apiKey)
                .post(json.toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: IOException) {
            false
        }
    }

    fun getDailyTask(deviceUuid: String): String? {
        return try {
            val request = Request.Builder()
                .url("$baseUrl/api/v1/ingest/daily-task?deviceUuid=$deviceUuid")
                .header("X-API-Key", apiKey)
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) response.body?.string() else null
        } catch (e: IOException) {
            null
        }
    }

    fun markDailyTaskSeen(deviceUuid: String, dayNumber: Int) {
        try {
            val json = moshi.adapter(Map::class.java).toJson(mapOf(
                "deviceUuid" to deviceUuid,
                "dayNumber" to dayNumber
            ))

            val request = Request.Builder()
                .url("$baseUrl/api/v1/ingest/daily-task/seen")
                .header("X-API-Key", apiKey)
                .post(json.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute()
        } catch (_: IOException) {
        }
    }

    fun ingest(payload: TelemetryPayload): Boolean {
        return try {
            val adapter = moshi.adapter(TelemetryPayload::class.java)
            val json = adapter.toJson(payload)

            val request = Request.Builder()
                .url("$baseUrl/api/v1/ingest/")
                .header("X-API-Key", apiKey)
                .post(json.toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            response.isSuccessful
        } catch (e: IOException) {
            false
        }
    }
}
