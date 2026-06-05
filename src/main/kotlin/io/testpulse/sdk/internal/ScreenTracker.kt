package io.testpulse.sdk.internal

import android.app.Activity
import android.app.Application
import android.os.Bundle
import io.testpulse.sdk.model.ScreenEvent
import java.time.Instant

class ScreenTracker(
    private val dataBatcher: DataBatcher
) : Application.ActivityLifecycleCallbacks {

    @Volatile
    var currentSessionUuid: String? = null
        private set

    private var currentScreenName: String? = null
    private var screenEnteredAt: Long = 0L

    fun onNewSession(sessionUuid: String) {
        currentSessionUuid = sessionUuid
        currentScreenName = null
    }

    fun onSessionEnd() {
        closeCurrentScreen()
        currentSessionUuid = null
    }

    fun logManualScreen(screenName: String) {
        if (currentSessionUuid == null) return

        closeCurrentScreen()

        currentScreenName = screenName
        screenEnteredAt = System.currentTimeMillis()
    }

    private fun closeCurrentScreen() {
        val name = currentScreenName ?: return
        val enteredAt = screenEnteredAt
        if (enteredAt == 0L) return

        val now = System.currentTimeMillis()
        val durationSec = ((now - enteredAt) / 1000).toInt()

        val screenEvent = ScreenEvent(
            screenName = name,
            enteredAt = Instant.ofEpochMilli(enteredAt).toString(),
            exitedAt = Instant.ofEpochMilli(now).toString(),
            durationSec = durationSec
        )
        dataBatcher.queueScreen(screenEvent, currentSessionUuid)

        currentScreenName = null
        screenEnteredAt = 0L
    }

    override fun onActivityResumed(activity: Activity) {
        if (currentSessionUuid == null) return

        val screenName = activity.javaClass.simpleName
        if (screenName == currentScreenName) return

        closeCurrentScreen()

        currentScreenName = screenName
        screenEnteredAt = System.currentTimeMillis()
    }

    override fun onActivityPaused(activity: Activity) {
        val screenName = activity.javaClass.simpleName
        if (screenName == currentScreenName) {
            closeCurrentScreen()
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}
