package io.testpulse.sdk.internal

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import io.testpulse.sdk.R
import org.json.JSONObject
import java.util.concurrent.Executors

class DailyTaskChecker(private val context: Context, private val apiClient: ApiClient) {

    private val ioExecutor = Executors.newCachedThreadPool()

    companion object {
        private const val PREFS_NAME = "testpulse_prefs"
        private const val KEY_LAST_TASK_SEEN = "tp_last_task_seen"

        private fun prefs(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun pollAndShowIfNeeded(activity: Activity) {
        val deviceUuid = TesterRegistration.getDeviceUuid(context)
        val response = apiClient.getDailyTask(deviceUuid) ?: return

        val dayNumber: Int
        val title: String
        val description: String
        try {
            val json = JSONObject(response)
            val taskJson = json.optJSONObject("task") ?: return
            val alreadySeen = taskJson.optBoolean("alreadySeen", false)

            if (alreadySeen) return

            dayNumber = taskJson.optInt("dayNumber", 0)
            title = taskJson.optString("title", "")
            description = taskJson.optString("description", "")
        } catch (_: Exception) {
            return
        }

        activity.runOnUiThread {
            showDailyTaskDialog(activity, dayNumber, title, description, deviceUuid)
        }
    }

    private fun showDailyTaskDialog(
        activity: Activity,
        dayNumber: Int,
        title: String,
        description: String,
        deviceUuid: String
    ) {
        val view = activity.layoutInflater.inflate(R.layout.dialog_daily_task, null)

        val tvDayNumber = view.findViewById<TextView>(R.id.tvDayNumber)
        val tvTaskTitle = view.findViewById<TextView>(R.id.tvTaskTitle)
        val tvTaskDescription = view.findViewById<TextView>(R.id.tvTaskDescription)
        val btnMarkDone = view.findViewById<Button>(R.id.btnMarkDone)

        tvDayNumber.text = "Day $dayNumber"
        tvTaskTitle.text = title
        if (description.isNotBlank()) {
            tvTaskDescription.text = description
        } else {
            tvTaskDescription.visibility = android.view.View.GONE
        }

        val btnBg = GradientDrawable().apply {
            setColor(Color.parseColor("#6C5CE7"))
            cornerRadii = floatArrayOf(12f, 12f, 12f, 12f, 12f, 12f, 12f, 12f)
        }
        btnMarkDone.background = btnBg

        val dialog = Dialog(activity)
        dialog.setContentView(view)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        val window = dialog.window
        if (window != null) {
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        btnMarkDone.setOnClickListener {
            prefs(context).edit().putInt(KEY_LAST_TASK_SEEN, dayNumber).apply()
            ioExecutor.execute {
                apiClient.markDailyTaskSeen(deviceUuid, dayNumber)
            }
            dialog.dismiss()
        }

        dialog.show()
    }
}
