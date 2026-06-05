package io.testpulse.sdk.internal

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.testpulse.sdk.R
import io.testpulse.sdk.TestPulse
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
            val dialog = BottomSheetDialog(activity)
            val view = activity.layoutInflater.inflate(
                R.layout.dialog_tester_registration, null
            )
            dialog.setContentView(view)
            dialog.setCancelable(false)

            val etAlias = view.findViewById<com.google.android.material.textfield.TextInputEditText>(
                R.id.etAlias
            )
            val btnStart = view.findViewById<com.google.android.material.button.MaterialButton>(
                R.id.btnStartTesting
            )

            btnStart.setOnClickListener {
                val alias = etAlias.text?.toString()?.trim() ?: ""
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
                dialog.dismiss()
            }

            dialog.show()
        }

        fun updateAlias(context: Context, newAlias: String) {
            prefs(context).edit().putString(KEY_TESTER_ALIAS, newAlias).apply()
        }
    }
}
