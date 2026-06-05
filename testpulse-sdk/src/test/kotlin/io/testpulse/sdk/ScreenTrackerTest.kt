package io.testpulse.sdk

import android.app.Activity
import io.testpulse.sdk.internal.DataBatcher
import io.testpulse.sdk.internal.ScreenTracker
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class ScreenTrackerTest {

    private lateinit var dataBatcher: DataBatcher
    private lateinit var screenTracker: ScreenTracker

    @Before
    fun setUp() {
        dataBatcher = mock()
        screenTracker = ScreenTracker(dataBatcher)
        screenTracker.onNewSession("session-1")
    }

    // TEST 7: onActivityResumed should track new screen
    @Test
    fun `onActivityResumed should track new screen`() {
        val activity = mock<Activity>()
        whenever(activity.javaClass).thenReturn(MainActivity::class.java)

        screenTracker.onActivityResumed(activity)

        // Verify no screen was queued yet (just opened, not closed)
        verify(dataBatcher, never()).queueScreen(any(), any())
    }

    // TEST 8: onActivityPaused should close screen with correct duration
    @Test
    fun `onActivityPaused should queue screen event`() {
        val activity = mock<Activity>()
        whenever(activity.javaClass).thenReturn(MainActivity::class.java)

        screenTracker.onActivityResumed(activity)
        screenTracker.onActivityPaused(activity)

        verify(dataBatcher, atLeast(1)).queueScreen(any(), eq("session-1"))
    }

    // TEST 9: Same activity resumed twice should not create duplicate screen
    @Test
    fun `same activity resumed twice should not create duplicate screen`() {
        val activity = mock<Activity>()
        whenever(activity.javaClass).thenReturn(MainActivity::class.java)

        screenTracker.onActivityResumed(activity)
        screenTracker.onActivityResumed(activity)

        verify(dataBatcher, never()).queueScreen(any(), any())
    }

    // TEST 10: Transition from A to B should close A and open B
    @Test
    fun `transition from ActivityA to ActivityB should close A and open B`() {
        val activityA = mock<Activity>()
        whenever(activityA.javaClass).thenReturn(ActivityA::class.java)
        val activityB = mock<Activity>()
        whenever(activityB.javaClass).thenReturn(ActivityB::class.java)

        screenTracker.onActivityResumed(activityA)
        screenTracker.onActivityResumed(activityB)

        verify(dataBatcher, atLeast(1)).queueScreen(any(), eq("session-1"))
    }

    // TEST 11: logManualScreen should work without Activity change
    @Test
    fun `logManualScreen should track screen without activity change`() {
        screenTracker.logManualScreen("HomeScreen")

        verify(dataBatcher, never()).queueScreen(any(), any())

        screenTracker.logManualScreen("SettingsScreen")

        verify(dataBatcher, atLeast(1)).queueScreen(any(), eq("session-1"))
    }

    // Mock activity classes
    private class MainActivity : Activity()
    private class ActivityA : Activity()
    private class ActivityB : Activity()
}
