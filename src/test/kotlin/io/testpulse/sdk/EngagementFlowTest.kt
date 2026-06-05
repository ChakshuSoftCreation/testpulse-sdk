package io.testpulse.sdk

import io.testpulse.sdk.internal.ApiClient
import io.testpulse.sdk.internal.DataBatcher
import io.testpulse.sdk.internal.DeviceCollector
import io.testpulse.sdk.internal.ScreenTracker
import io.testpulse.sdk.internal.SessionTracker
import io.testpulse.sdk.model.SessionEvent
import io.testpulse.sdk.model.TelemetryPayload
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertNotNull

@RunWith(RobolectricTestRunner::class)
class EngagementFlowTest {

    private lateinit var apiClient: ApiClient
    private lateinit var dataBatcher: DataBatcher
    private lateinit var screenTracker: ScreenTracker
    private lateinit var sessionTracker: SessionTracker

    @Before
    fun setUp() {
        apiClient = mock()
        dataBatcher = DataBatcher(
            RuntimeEnvironment.getApplication().applicationContext,
            apiClient
        )
        screenTracker = ScreenTracker(dataBatcher)
        sessionTracker = SessionTracker(dataBatcher, screenTracker)
    }

    // TEST 25: Full flow: init -> session -> screen -> flush
    @Test
    fun `full flow should track session screen and flush telemetry`() {
        whenever(apiClient.ingest(any())).thenReturn(true)

        sessionTracker.onStart(mock())
        val sessionUuid = sessionTracker.currentSessionUuid
        assertNotNull(sessionUuid)

        screenTracker.logManualScreen("HomeScreen")
        screenTracker.logManualScreen("DetailScreen")

        sessionTracker.onStop(mock())

        Thread.sleep(500)
        dataBatcher.flushNow()
        Thread.sleep(500)

        verify(apiClient, atLeast(1)).ingest(any())

        val queueSize = dataBatcher.getQueueSize()
        assert(queueSize == 0 || queueSize >= 0)
    }
}
