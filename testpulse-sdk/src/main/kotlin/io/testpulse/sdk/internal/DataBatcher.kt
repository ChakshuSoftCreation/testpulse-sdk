package io.testpulse.sdk.internal

import android.content.Context
import android.os.Handler
import android.os.Looper
import io.testpulse.sdk.db.EventDao
import io.testpulse.sdk.db.EventEntity
import io.testpulse.sdk.db.TestPulseDatabase
import io.testpulse.sdk.model.CustomEventData
import io.testpulse.sdk.model.ScreenEvent
import io.testpulse.sdk.model.SessionEvent
import io.testpulse.sdk.model.TelemetryPayload
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DataBatcher(context: Context, private val apiClient: ApiClient) {

    private val appContext = context.applicationContext
    private val db: TestPulseDatabase = TestPulseDatabase.getInstance(appContext)
    private val eventDao: EventDao = db.eventDao()
    private val flushHandler = Handler(Looper.getMainLooper())
    private val flushIntervalMs: Long = 60_000L
    private val scope = CoroutineScope(Dispatchers.IO)

    @Volatile
    var isFlushScheduled: Boolean = false
        private set

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val flushRunnable = Runnable { performFlush() }

    fun queueSession(session: SessionEvent) {
        scope.launch {
            val json = moshi.adapter(SessionEvent::class.java).toJson(session)
            eventDao.insert(
                EventEntity(
                    type = "session",
                    sessionUuid = session.sessionUuid,
                    jsonPayload = json
                )
            )
        }
    }

    fun queueScreen(screen: ScreenEvent, sessionUuid: String?) {
        scope.launch {
            val json = moshi.adapter(ScreenEvent::class.java).toJson(screen)
            eventDao.insert(
                EventEntity(
                    type = "screen",
                    sessionUuid = sessionUuid,
                    jsonPayload = json
                )
            )
        }
    }

    fun queueCustomEvent(event: CustomEventData, sessionUuid: String?) {
        scope.launch {
            val json = moshi.adapter(CustomEventData::class.java).toJson(event)
            eventDao.insert(
                EventEntity(
                    type = "custom",
                    sessionUuid = sessionUuid,
                    jsonPayload = json
                )
            )
        }
    }

    fun startPeriodicFlush() {
        if (isFlushScheduled) return
        isFlushScheduled = true
        flushHandler.postDelayed(flushRunnable, flushIntervalMs)
    }

    fun stopPeriodicFlush() {
        isFlushScheduled = false
        flushHandler.removeCallbacks(flushRunnable)
    }

    fun flushNow() {
        scope.launch { performFlushSync() }
    }

    private fun performFlush() {
        scope.launch {
            performFlushSync()
            if (isFlushScheduled) {
                flushHandler.postDelayed(flushRunnable, flushIntervalMs)
            }
        }
    }

    private suspend fun performFlushSync() {
        val unsynced = eventDao.getUnsynced(limit = 100)
        if (unsynced.isEmpty()) return

        val deviceUuid = TesterRegistration.getDeviceUuid(appContext)

        val sessionMap = mutableMapOf<String, SessionEvent>()
        val screenMap = mutableMapOf<String, MutableList<ScreenEvent>>()
        val eventMap = mutableMapOf<String, MutableList<CustomEventData>>()
        val processedIds = mutableListOf<Long>()

        for (entity in unsynced) {
            when (entity.type) {
                "session" -> {
                    val session = moshi.adapter(SessionEvent::class.java)
                        .fromJson(entity.jsonPayload)
                    if (session != null) {
                        sessionMap[session.sessionUuid] = session
                        processedIds.add(entity.id)
                    }
                }
                "screen" -> {
                    val screen = moshi.adapter(ScreenEvent::class.java)
                        .fromJson(entity.jsonPayload)
                    if (screen != null) {
                        val uuid = entity.sessionUuid ?: return
                        screenMap.getOrPut(uuid) { mutableListOf() }.add(screen)
                        processedIds.add(entity.id)
                    }
                }
                "custom" -> {
                    val event = moshi.adapter(CustomEventData::class.java)
                        .fromJson(entity.jsonPayload)
                    if (event != null) {
                        val uuid = entity.sessionUuid ?: return
                        eventMap.getOrPut(uuid) { mutableListOf() }.add(event)
                        processedIds.add(entity.id)
                    }
                }
            }
        }

        if (sessionMap.isEmpty()) return

        val sessionEvents = sessionMap.map { (uuid, session) ->
            session.copy(
                screens = screenMap[uuid] ?: emptyList(),
                events = eventMap[uuid] ?: emptyList()
            )
        }

        val payload = TelemetryPayload(
            deviceUuid = deviceUuid,
            sessions = sessionEvents
        )

        val success = apiClient.ingest(payload)
        if (success) {
            eventDao.markSynced(processedIds)
            eventDao.deleteOldSynced()
        }
    }

    fun getQueueSize(): Int {
        var count = 0
        val latch = java.util.concurrent.CountDownLatch(1)
        scope.launch {
            count = eventDao.getUnsyncedCount()
            latch.countDown()
        }
        latch.await()
        return count
    }
}
