package io.testpulse.sdk.internal

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.widget.EditText
import android.widget.Toast
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
            val input = EditText(activity).apply {
                hint = "Your name or alias"
                maxLines = 1
            }

            val dialog = AlertDialog.Builder(activity)
                .setTitle("Welcome, Tester!")
                .setMessage("Enter your name so the developer can track your feedback.")
                .setView(input)
                .setCancelable(false)
                .setPositiveButton("Start Testing") { _, _ ->
                    val alias = input.text.toString().trim()
                    if (alias.isBlank()) {
                        Toast.makeText(activity, "Please enter your name", Toast.LENGTH_SHORT).show()
                        showRegistrationDialog(activity, onComplete)
                        return@setPositiveButton
                    }

                    val prefs = prefs(activity)
                    prefs.edit()
                        .putString(KEY_TESTER_ALIAS, alias)
                        .putString(KEY_DEVICE_UUID, getDeviceUuid(activity))
                        .putBoolean(KEY_IS_REGISTERED, true)
                        .apply()

                    onComplete(alias)
                }
                .create()

            dialog.show()
        }

        fun updateAlias(context: Context, newAlias: String) {
            prefs(context).edit().putString(KEY_TESTER_ALIAS, newAlias).apply()
        }
    }
}
