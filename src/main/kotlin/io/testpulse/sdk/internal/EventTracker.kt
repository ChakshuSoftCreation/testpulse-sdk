package io.testpulse.sdk.internal

import io.testpulse.sdk.model.CustomEventData
import java.time.Instant

class EventTracker(private val dataBatcher: DataBatcher) {

    fun logEvent(name: String, data: Map<String, String>?, sessionUuid: String?) {
        val event = CustomEventData(
            eventName = name,
            eventData = data,
            timestamp = Instant.now().toString()
        )
        dataBatcher.queueCustomEvent(event, sessionUuid)
    }
}
