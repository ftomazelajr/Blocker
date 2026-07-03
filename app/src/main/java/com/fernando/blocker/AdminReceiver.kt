package com.fernando.blocker

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AdminReceiver : DeviceAdminReceiver() {
    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        return context.getString(R.string.admin_disable_warning)
    }

    override fun onEnabled(context: Context, intent: Intent) {
        Toast.makeText(context, R.string.admin_enabled, Toast.LENGTH_SHORT).show()
    }
}
