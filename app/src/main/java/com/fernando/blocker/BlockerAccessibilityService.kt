package com.fernando.blocker

import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class BlockerAccessibilityService : AccessibilityService() {

    private val settingsPackages = setOf(
        "com.android.settings",
        "com.android.packageinstaller",
        "com.google.android.packageinstaller"
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.DEFAULT
            notificationTimeout = 0
        }
        serviceInfo = info
        PrefsManager.init(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val root = rootInActiveWindow ?: return
        val pkg = event?.packageName?.toString() ?: return

        // 1. Bloqueia conteúdo adulto em navegadores
        if (pkg in BlockList.browserPackages) {
            val urlText = findUrlBarText(root)
            if (BlockList.matches(urlText)) {
                triggerBlock()
                return
            }
        }

        // 2. Varre texto da tela (apps de vídeo etc.)
        val screenText = extractAllText(root)
        if (BlockList.matches(screenText)) {
            triggerBlock()
            return
        }

        // 3. Protege contra desativar o serviço ou desinstalar o app
        if (pkg in settingsPackages && !PrefsManager.isUnlocked(applicationContext)) {
            val text = screenText.lowercase()
            val touchesBlockerSettings = text.contains("blocker") &&
                    (text.contains("acessibilidade") ||
                     text.contains("accessibility") ||
                     text.contains("desinstalar") ||
                     text.contains("uninstall") ||
                     text.contains("forçar parada") ||
                     text.contains("force stop"))

            if (touchesBlockerSettings) {
                performGlobalAction(GLOBAL_ACTION_HOME)
                val intent = Intent(this, UnlockActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                startActivity(intent)
            }
        }
    }

    private fun findUrlBarText(node: AccessibilityNodeInfo): String? {
        val candidates = listOf(
            "com.android.chrome:id/url_bar",
            "org.mozilla.firefox:id/mozac_browser_toolbar_url_view",
            "com.brave.browser:id/url_bar",
            "com.sec.android.app.sbrowser:id/location_bar_edit_text"
        )
        for (id in candidates) {
            val nodes = node.findAccessibilityNodeInfosByViewId(id)
            if (nodes.isNotEmpty()) {
                return nodes[0].text?.toString()
            }
        }
        return null
    }

    private fun extractAllText(node: AccessibilityNodeInfo, depth: Int = 0): String {
        if (depth > 6) return ""
        val sb = StringBuilder()
        node.text?.let { sb.append(it).append(" ") }
        node.contentDescription?.let { sb.append(it).append(" ") }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { child ->
                sb.append(extractAllText(child, depth + 1))
                child.recycle()
            }
        }
        return sb.toString()
    }

    private fun triggerBlock() {
        PrefsManager.registerBlock(applicationContext)
        performGlobalAction(GLOBAL_ACTION_HOME)
        val intent = Intent(this, BlockedOverlayActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(intent)
    }

    override fun onInterrupt() {}
}
EOF.
cat > app/src/main/java/com/fernando/blocker/MainActivity.kt << 'EOF'
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
