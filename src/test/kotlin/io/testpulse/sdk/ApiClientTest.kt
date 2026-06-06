package io.testpulse.sdk

import io.testpulse.sdk.internal.ApiClient
import io.testpulse.sdk.model.DeviceInfo
import io.testpulse.sdk.model.TelemetryPayload
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test

class ApiClientTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiClient: ApiClient

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        apiClient = ApiClient("test-api-key", mockWebServer.url("").toString().trimEnd('/'))
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    // TEST 19: registerTester should POST with correct headers and body
    @Test
    fun `registerTester should POST to register-tester endpoint`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(201))

        val deviceInfo = DeviceInfo(
            deviceModel = "Pixel 7",
            manufacturer = "Google",
            osVersion = "Android 14",
            sdkInt = 34,
            appVersion = "1.0.0",
            appVersionCode = 1,
            screenResolution = "1080x2400",
            locale = "en_US",
            networkType = "wifi"
        )

        val result = apiClient.registerTester("device-uuid-1", "tp_android_id", "Rahul", deviceInfo)

        val request = mockWebServer.takeRequest()
        assert(request.method == "POST")
        assert(request.path!!.contains("/api/v1/ingest/register-tester"))
        assert(request.getHeader("X-API-Key") == "test-api-key")
        assert(result)
    }

    // TEST 20: registerTester should return true on 201
    @Test
    fun `registerTester should return true on success`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(201))

        val deviceInfo = DeviceInfo(
            deviceModel = "Pixel 7", manufacturer = "Google", osVersion = "Android 14",
            sdkInt = 34, appVersion = "1.0.0", appVersionCode = 1,
            screenResolution = "1080x2400", locale = "en_US", networkType = "wifi"
        )

        val result = apiClient.registerTester("uuid", "tp_android_id", "Rahul", deviceInfo)
        assert(result)
    }

    // TEST 21: registerTester should return false on network error
    @Test
    fun `registerTester should return false on network error`() {
        mockWebServer.shutdown()
        val deviceInfo = DeviceInfo(
            deviceModel = "Pixel 7", manufacturer = "Google", osVersion = "Android 14",
            sdkInt = 34, appVersion = "1.0.0", appVersionCode = 1,
            screenResolution = "1080x2400", locale = "en_US", networkType = "wifi"
        )
        val result = apiClient.registerTester("uuid", "tp_android_id", "Rahul", deviceInfo)
        assert(!result)
    }

    // TEST 22: ingest should POST with correct X-API-Key header
    @Test
    fun `ingest should POST with correct API key header`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val payload = TelemetryPayload(
            deviceUuid = "device-uuid-1",
            sessions = emptyList()
        )

        apiClient.ingest(payload)

        val request = mockWebServer.takeRequest()
        assert(request.method == "POST")
        assert(request.path!!.contains("/api/v1/ingest/"))
        assert(request.getHeader("X-API-Key") == "test-api-key")
    }

    // TEST 23: ingest should return true on 200
    @Test
    fun `ingest should return true on success`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200))

        val payload = TelemetryPayload(deviceUuid = "uuid", sessions = emptyList())
        val result = apiClient.ingest(payload)
        assert(result)
    }

    // TEST 24: ingest should return false on 401
    @Test
    fun `ingest should return false on 401`() {
        mockWebServer.enqueue(MockResponse().setResponseCode(401))

        val payload = TelemetryPayload(deviceUuid = "uuid", sessions = emptyList())
        val result = apiClient.ingest(payload)
        assert(!result)
    }
}
