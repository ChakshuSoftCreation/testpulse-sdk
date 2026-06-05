package io.testpulse.sdk.internal

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.os.Build
import android.util.DisplayMetrics
import io.testpulse.sdk.model.DeviceInfo
import java.util.Locale

class DeviceCollector(context: Context) {

    val deviceInfo: DeviceInfo

    init {
        val pm = context.packageManager
        val pi = if (Build.VERSION.SDK_INT >= 33) {
            pm.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION") pm.getPackageInfo(context.packageName, 0)
        }
        val dm = context.resources.displayMetrics
        val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        deviceInfo = DeviceInfo(
            deviceModel = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            osVersion = "Android ${Build.VERSION.RELEASE}",
            sdkInt = Build.VERSION.SDK_INT,
            appVersion = pi.versionName ?: "unknown",
            appVersionCode = if (Build.VERSION.SDK_INT >= 28) {
                pi.longVersionCode
            } else {
                @Suppress("DEPRECATION") pi.versionCode.toLong()
            },
            screenResolution = "${dm.widthPixels}x${dm.heightPixels}",
            locale = Locale.getDefault().toString(),
            networkType = getNetworkType(cm)
        )
    }

    private fun getNetworkType(cm: ConnectivityManager): String {
        val network = cm.activeNetwork ?: return "none"
        val caps = cm.getNetworkCapabilities(network) ?: return "none"
        return when {
            caps.hasTransport(TRANSPORT_WIFI) -> "wifi"
            caps.hasTransport(TRANSPORT_CELLULAR) -> "cellular"
            else -> "other"
        }
    }
}
