package io.testpulse.sdk.internal

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.provider.Settings
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import io.testpulse.sdk.R
import java.util.UUID

class TesterRegistration {

    companion object {
        private const val PREFS_NAME = "testpulse_prefs"
        private const val KEY_DEVICE_UUID = "tp_device_uuid"
        private const val KEY_DEVICE_FINGERPRINT = "tp_device_fingerprint"
        private const val KEY_TESTER_ALIAS = "tp_tester_alias"
        private const val KEY_IS_REGISTERED = "tp_is_registered"

        @Volatile
        private var isDialogShowing = false
        private var currentDialog: Dialog? = null

        private fun prefs(context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }

        fun isRegistered(context: Context): Boolean {
            return prefs(context).getBoolean(KEY_IS_REGISTERED, false)
        }

        /**
         * Returns a persistent device identifier using Settings.Secure.ANDROID_ID.
         * ANDROID_ID survives app data clears on the same device (same signing key).
         * Falls back to a random UUID if ANDROID_ID is unavailable.
         * Once generated, the value is cached in SharedPreferences for speed.
         */
        fun getDeviceFingerprint(context: Context): String {
            return resolveFingerprint(context)
        }

        fun getDeviceUuid(context: Context): String {
            val prefs = prefs(context)
            var uuid = prefs.getString(KEY_DEVICE_UUID, null)
            if (uuid == null) {
                uuid = resolveFingerprint(context)
                prefs.edit().putString(KEY_DEVICE_UUID, uuid).apply()
            }
            return uuid
        }

        private fun resolveFingerprint(context: Context): String {
            val prefs = prefs(context)
            var fp = prefs.getString(KEY_DEVICE_FINGERPRINT, null)
            if (fp == null) {
                fp = try {
                    val aid = Settings.Secure.getString(
                        context.contentResolver, Settings.Secure.ANDROID_ID
                    )
                    if (!aid.isNullOrBlank() && aid != "9774d56d682e549c") {
                        "tp_$aid"
                    } else {
                        "tp_${UUID.randomUUID()}"
                    }
                } catch (_: Exception) {
                    "tp_${UUID.randomUUID()}"
                }
                prefs.edit().putString(KEY_DEVICE_FINGERPRINT, fp).apply()
            }
            return fp
        }

        fun getTesterAlias(context: Context): String? {
            return prefs(context).getString(KEY_TESTER_ALIAS, null)
        }

        fun showRegistrationDialog(activity: Activity, onComplete: (alias: String) -> Unit) {
            if (isDialogShowing || activity.isFinishing) return
            isDialogShowing = true

            val view = activity.layoutInflater.inflate(
                R.layout.dialog_tester_registration, null
            )

            val etAlias = view.findViewById<EditText>(R.id.etAlias)
            val btnStart = view.findViewById<Button>(R.id.btnStartTesting)
            val btnAbout = view.findViewById<TextView>(R.id.btnAbout)

            btnAbout.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://testpulse-dashboard-x3yd.onrender.com/"))
                activity.startActivity(intent)
            }

            val dialog = Dialog(activity)
            currentDialog = dialog
            dialog.setContentView(view)
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
            val window = dialog.window
            if (window != null) {
                window.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                window.setBackgroundDrawableResource(android.R.color.transparent)
            }
            dialog.setOnDismissListener {
                isDialogShowing = false
                currentDialog = null
            }

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

                dialog.dismiss()
                onComplete(alias)
            }

            dialog.show()
        }

        fun updateAlias(context: Context, newAlias: String) {
            prefs(context).edit().putString(KEY_TESTER_ALIAS, newAlias).apply()
        }

        fun onActivityDestroyed() {
            isDialogShowing = false
            try {
                currentDialog?.dismiss()
            } catch (_: Exception) {
            }
            currentDialog = null
        }
    }
}
