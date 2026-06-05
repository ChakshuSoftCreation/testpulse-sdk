package io.testpulse.sdk.internal

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.testpulse.sdk.model.SessionEvent
import java.time.Instant
import java.util.UUID

class SessionTracker(
    private val dataBatcher: DataBatcher,
    private val screenTracker: ScreenTracker
) : DefaultLifecycleObserver {

    @Volatile
    var currentSessionUuid: String? = null
        private set

    var isInForeground: Boolean = false
        private set

    private var sessionStartTime: Long = 0L

    override fun onStart(owner: LifecycleOwner) {
        val sessionUuid = UUID.randomUUID().toString()
        currentSessionUuid = sessionUuid
        sessionStartTime = System.currentTimeMillis()
        isInForeground = true

        val sessionEvent = SessionEvent(
            sessionUuid = sessionUuid,
            startTime = Instant.ofEpochMilli(sessionStartTime).toString(),
            endTime = null,
            durationSec = null
        )
        dataBatcher.queueSession(sessionEvent)
        screenTracker.onNewSession(sessionUuid)
    }

    override fun onStop(owner: LifecycleOwner) {
        val endTime = System.currentTimeMillis()
        val durationSec = ((endTime - sessionStartTime) / 1000).toInt()

        val sessionUuid = currentSessionUuid
        if (sessionUuid != null) {
            val sessionEvent = SessionEvent(
                sessionUuid = sessionUuid,
                startTime = Instant.ofEpochMilli(sessionStartTime).toString(),
                endTime = Instant.ofEpochMilli(endTime).toString(),
                durationSec = durationSec
            )
            dataBatcher.queueSession(sessionEvent)
        }

        isInForeground = false
        dataBatcher.flushNow()
        screenTracker.onSessionEnd()
        currentSessionUuid = null
    }
}
