package io.testpulse.sdk.internal

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import io.testpulse.sdk.R
import java.util.UUID

class TesterRegistration {

    companion object {
        private const val PREFS_NAME = "testpulse_prefs"
        private const val KEY_DEVICE_UUID = "tp_device_uuid"
        private const val KEY_TESTER_ALIAS = "tp_tester_alias"
        private const val KEY_IS_REGISTERED = "tp_is_registered"

        private fun prefs(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }

        fun isRegistered(context: Context): Boolean {
            return prefs(context).getBoolean(KEY_IS_REGISTERED, false)
        }

        fun getDeviceUuid(context: Context): String {
            val prefs = prefs(context)
            var uuid = prefs.getString(KEY_DEVICE_UUID, null)
            if (uuid == null) {
                uuid = UUID.randomUUID().toString()
                prefs.edit().putString(KEY_DEVICE_UUID, uuid).apply()
            }
            return uuid
        }

        fun getTesterAlias(context: Context): String? {
            return prefs(context).getString(KEY_TESTER_ALIAS, null)
        }

        fun showRegistrationDialog(activity: Activity, onComplete: (alias: String) -> Unit) {
            val view = activity.layoutInflater.inflate(
                R.layout.dialog_tester_registration, null
            )

            val etAlias = view.findViewById<EditText>(R.id.etAlias)
            val btnStart = view.findViewById<Button>(R.id.btnStartTesting)

            val btnBg = GradientDrawable().apply {
                setColor(Color.parseColor("#6C5CE7"))
                cornerRadii = floatArrayOf(12f, 12f, 12f, 12f, 12f, 12f, 12f, 12f)
            }
            btnStart.background = btnBg

            btnStart.setOnClickListener {
                val alias = etAlias.text.toString().trim()
                if (alias.isBlank()) {
                    Toast.makeText(activity, "Please enter your name", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val prefs = prefs(activity)
                prefs.edit()
                    .putString(KEY_TESTER_ALIAS, alias)
                    .putString(KEY_DEVICE_UUID, getDeviceUuid(activity))
                    .putBoolean(KEY_IS_REGISTERED, true)
                    .apply()

                onComplete(alias)
            }

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
            dialog.show()
        }

        fun updateAlias(context: Context, newAlias: String) {
            prefs(context).edit().putString(KEY_TESTER_ALIAS, newAlias).apply()
        }
    }
}
