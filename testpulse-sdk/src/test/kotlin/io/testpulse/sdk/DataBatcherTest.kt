package io.testpulse.sdk

import android.content.Context
import io.testpulse.sdk.internal.ApiClient
import io.testpulse.sdk.internal.DataBatcher
import io.testpulse.sdk.model.ScreenEvent
import io.testpulse.sdk.model.SessionEvent
import io.testpulse.sdk.model.TelemetryPayload
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.*
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class DataBatcherTest {

    private lateinit var context: Context
    private lateinit var apiClient: ApiClient
    private lateinit var dataBatcher: DataBatcher

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication().applicationContext
        apiClient = mock()
        dataBatcher = DataBatcher(context, apiClient)
    }

    @Test
    fun `queueSession should insert event in Room DB`() {
        val session = SessionEvent(
            sessionUuid = "sess-1",
            startTime = "2026-06-05T10:00:00Z"
        )
        dataBatcher.queueSession(session)
        val queueSize = dataBatcher.getQueueSize()
        assert(queueSize > 0)
    }

    @Test
    fun `queueScreen should insert event in Room DB`() {
        val screen = ScreenEvent(
            screenName = "MainActivity",
            enteredAt = "2026-06-05T10:00:00Z"
        )
        dataBatcher.queueScreen(screen, "sess-1")
        val queueSize = dataBatcher.getQueueSize()
        assert(queueSize > 0)
    }

    @Test
    fun `flushNow should send unsynced events to ApiClient`() {
        whenever(apiClient.ingest(any())).thenReturn(true)

        val session = SessionEvent(
            sessionUuid = "sess-flush",
            startTime = "2026-06-05T10:00:00Z"
        )
        dataBatcher.queueSession(session)
        Thread.sleep(200)
        dataBatcher.flushNow()
        Thread.sleep(200)

        verify(apiClient, atLeast(1)).ingest(any())
    }

    @Test
    fun `flushNow should mark events as synced on success`() {
        whenever(apiClient.ingest(any())).thenReturn(true)

        dataBatcher.queueSession(
            SessionEvent(sessionUuid = "sess-sync", startTime = "2026-06-05T10:00:00Z")
        )
        Thread.sleep(200)
        dataBatcher.flushNow()
        Thread.sleep(200)

        val queueSize = dataBatcher.getQueueSize()
        assert(queueSize == 0)
    }

    @Test
    fun `flushNow should not mark events as synced on failure`() {
        whenever(apiClient.ingest(any())).thenReturn(false)

        dataBatcher.queueSession(
            SessionEvent(sessionUuid = "sess-fail", startTime = "2026-06-05T10:00:00Z")
        )
        Thread.sleep(200)
        dataBatcher.flushNow()
        Thread.sleep(200)

        val queueSize = dataBatcher.getQueueSize()
        assert(queueSize > 0)
    }

    @Test
    fun `flushNow with empty queue should not call ApiClient`() {
        dataBatcher.flushNow()
        verify(apiClient, never()).ingest(any())
    }

    @Test
    fun `queueSession should be stored and retrievable`() {
        dataBatcher.queueSession(
            SessionEvent(sessionUuid = "sess-store", startTime = "2026-06-05T10:00:00Z")
        )
        Thread.sleep(100)
        dataBatcher.queueScreen(
            ScreenEvent(screenName = "Test", enteredAt = "2026-06-05T10:00:00Z"),
            "sess-store"
        )
        Thread.sleep(100)
        dataBatcher.queueCustomEvent(
            io.testpulse.sdk.model.CustomEventData(
                eventName = "test_event",
                timestamp = "2026-06-05T10:00:00Z"
            ),
            "sess-store"
        )
        Thread.sleep(100)

        assert(dataBatcher.getQueueSize() >= 3)
    }
}
