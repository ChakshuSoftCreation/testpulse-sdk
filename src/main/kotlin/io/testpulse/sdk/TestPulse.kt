package io.testpulse.sdk

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import io.testpulse.sdk.internal.ApiClient
import io.testpulse.sdk.internal.DataBatcher
import io.testpulse.sdk.internal.DailyTaskChecker
import io.testpulse.sdk.internal.DeviceCollector
import io.testpulse.sdk.internal.EventTracker
import io.testpulse.sdk.internal.ScreenTracker
import io.testpulse.sdk.internal.SessionTracker
import io.testpulse.sdk.internal.TesterRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object TestPulse {

    private const val TAG = "TestPulse"
    private const val DEFAULT_BASE_URL = "https://testpulse-api-lfwq.onrender.com"

    @Volatile
    var isInitialized: Boolean = false
        private set

    var baseUrl: String = DEFAULT_BASE_URL
        private set

    private var apiKey: String = ""
    private lateinit var applicationContext: Context
    private lateinit var sessionTracker: SessionTracker
    private lateinit var screenTracker: ScreenTracker
    private lateinit var eventTracker: EventTracker
    private lateinit var dataBatcher: DataBatcher
    private lateinit var apiClient: ApiClient
    private lateinit var deviceCollector: DeviceCollector
    private lateinit var dailyTaskChecker: DailyTaskChecker
    private val ioScope = CoroutineScope(Dispatchers.IO)

    internal fun initialize(context: Context) {
        if (isInitialized) return

        applicationContext = context.applicationContext

        try {
            val ai = applicationContext.packageManager.getApplicationInfo(
                applicationContext.packageName,
                PackageManager.GET_META_DATA
            )
            val meta = ai.metaData

            apiKey = meta.getString("io.testpulse.API_KEY") ?: ""
            if (apiKey.isEmpty()) {
                Log.w(TAG, "API key not found in AndroidManifest meta-data")
                return
            }

            val customBaseUrl = meta.getString("io.testpulse.BASE_URL")
            if (!customBaseUrl.isNullOrEmpty()) {
                baseUrl = customBaseUrl
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read meta-data", e)
            return
        }

        apiClient = ApiClient(apiKey, baseUrl)
        deviceCollector = DeviceCollector(applicationContext)
        dataBatcher = DataBatcher(applicationContext, apiClient)
        eventTracker = EventTracker(dataBatcher)
        screenTracker = ScreenTracker(dataBatcher)
        sessionTracker = SessionTracker(dataBatcher, screenTracker)
        dailyTaskChecker = DailyTaskChecker(applicationContext, apiClient)

        if (TesterRegistration.isRegistered(applicationContext)) {
            val alias = TesterRegistration.getTesterAlias(applicationContext) ?: ""
            val deviceUuid = TesterRegistration.getDeviceUuid(applicationContext)
            ioScope.launch {
                apiClient.registerTester(deviceUuid, alias, deviceCollector.deviceInfo)
            }
        }

        // -- Crash handler
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val sw = java.io.StringWriter()
                val pw = java.io.PrintWriter(sw)
                throwable.printStackTrace(pw)
                pw.flush()
                val stackTrace = sw.toString()
                val sessionUuid = sessionTracker.currentSessionUuid

                dataBatcher.queueCrash(
                    throwable.javaClass.name,
                    throwable.message ?: "",
                    stackTrace,
                    sessionUuid
                )
                dataBatcher.flushNow()
            } catch (_: Exception) {
            } finally {
                defaultHandler?.uncaughtException(thread, throwable)
            }
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(sessionTracker)

        (applicationContext as Application).registerActivityLifecycleCallbacks(
            object : Application.ActivityLifecycleCallbacks {
                override fun onActivityResumed(activity: Activity) {
                    if (!TesterRegistration.isRegistered(applicationContext)) {
                        TesterRegistration.showRegistrationDialog(activity) { alias ->
                            val deviceUuid = TesterRegistration.getDeviceUuid(applicationContext)
                            ioScope.launch {
                                apiClient.registerTester(
                                    deviceUuid, alias, deviceCollector.deviceInfo
                                )
                            }
                        }
                    } else {
                        dailyTaskChecker.pollAndShowIfNeeded(activity)
                    }
                }
                override fun onActivityCreated(a: Activity, b: Bundle?) {}
                override fun onActivityStarted(a: Activity) {}
                override fun onActivityPaused(a: Activity) {}
                override fun onActivityStopped(a: Activity) {}
                override fun onActivitySaveInstanceState(a: Activity, b: Bundle) {}
                override fun onActivityDestroyed(a: Activity) {}
            }
        )

        (applicationContext as Application).registerActivityLifecycleCallbacks(screenTracker)

        dataBatcher.startPeriodicFlush()

        isInitialized = true
        Log.i(TAG, "TestPulse SDK initialized for project: ${apiKey.take(12)}...")
    }

    fun logEvent(name: String, data: Map<String, String> = emptyMap()) {
        if (!isInitialized) {
            Log.w(TAG, "logEvent called before SDK initialization")
            return
        }
        eventTracker.logEvent(name, data, sessionTracker.currentSessionUuid)
    }

    fun logScreen(screenName: String) {
        if (!isInitialized) {
            Log.w(TAG, "logScreen called before SDK initialization")
            return
        }
        screenTracker.logManualScreen(screenName)
    }

    fun flush() {
        if (!isInitialized) return
        dataBatcher.flushNow()
    }

    fun setTesterAlias(alias: String) {
        if (!isInitialized) return
        TesterRegistration.updateAlias(applicationContext, alias)
        val deviceUuid = TesterRegistration.getDeviceUuid(applicationContext)
        ioScope.launch {
            apiClient.registerTester(deviceUuid, alias, deviceCollector.deviceInfo)
        }
    }
}
