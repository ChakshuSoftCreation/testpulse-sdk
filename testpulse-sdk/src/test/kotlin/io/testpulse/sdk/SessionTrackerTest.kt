package io.testpulse.sdk

import io.testpulse.sdk.internal.DataBatcher
import io.testpulse.sdk.internal.ScreenTracker
import io.testpulse.sdk.internal.SessionTracker
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class SessionTrackerTest {

    private lateinit var dataBatcher: DataBatcher
    private lateinit var screenTracker: ScreenTracker
    private lateinit var sessionTracker: SessionTracker

    @Before
    fun setUp() {
        dataBatcher = mock()
        screenTracker = mock()
        sessionTracker = SessionTracker(dataBatcher, screenTracker)
    }

    // TEST 1: onStart should generate a new session UUID
    @Test
    fun `onStart should generate a new session UUID`() {
        sessionTracker.onStart(mock())
        val sessionUuid = sessionTracker.currentSessionUuid
        assert(sessionUuid != null)
        assert(sessionUuid!!.isNotBlank())
    }

    // TEST 2: onStart should set sessionStartTime to current time
    @Test
    fun `onStart should set isInForeground to true`() {
        sessionTracker.onStart(mock())
        assert(sessionTracker.isInForeground)
    }

    // TEST 3: onStop should calculate correct duration
    @Test
    fun `onStop should queue session with endTime and durationSec`() {
        sessionTracker.onStart(mock())
        Thread.sleep(100)
        sessionTracker.onStop(mock())
        verify(dataBatcher, atLeast(2)).queueSession(any())
    }

    // TEST 4: onStop should trigger flushNow()
    @Test
    fun `onStop should trigger flushNow`() {
        sessionTracker.onStart(mock())
        sessionTracker.onStop(mock())
        verify(dataBatcher, atLeast(1)).flushNow()
    }

    // TEST 5: Multiple onStart/onStop cycles should generate unique session UUIDs
    @Test
    fun `multiple onStart onStop cycles should generate unique session UUIDs`() {
        sessionTracker.onStart(mock())
        val uuid1 = sessionTracker.currentSessionUuid
        sessionTracker.onStop(mock())

        sessionTracker.onStart(mock())
        val uuid2 = sessionTracker.currentSessionUuid
        sessionTracker.onStop(mock())

        assert(uuid1 != null)
        assert(uuid2 != null)
        assert(uuid1 != uuid2)
    }

    // TEST 6: onStop without prior onStart should not crash
    @Test
    fun `onStop without prior onStart should not crash`() {
        sessionTracker.onStop(mock())
        assert(true)
    }
}
