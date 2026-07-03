package com.fernando.blocker

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PrefsManager.init(applicationContext)

        if (!PrefsManager.hasPin(this)) {
            startActivity(Intent(this, SetupPinActivity::class.java))
        }

        findViewById<Button>(R.id.btnEnableAccessibility).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        findViewById<Button>(R.id.btnEnableAdmin).setOnClickListener {
            val compName = ComponentName(this, AdminReceiver::class.java)
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    getString(R.string.admin_explanation))
            }
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun updateStatus() {
        val enabled = isAccessibilityServiceEnabled()
        findViewById<TextView>(R.id.textStatus).text =
            if (enabled) getString(R.string.status_active)
            else getString(R.string.status_inactive)

        findViewById<TextView>(R.id.textStreak).text =
            getString(R.string.streak_format, PrefsManager.getStreakDays(this))

        findViewById<TextView>(R.id.textBlockCount).text =
            getString(R.string.block_count_format, PrefsManager.getBlockCount(this))
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expected = "${packageName}/${BlockerAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.split(":").any { it.equals(expected, ignoreCase = true) }
    }
}
