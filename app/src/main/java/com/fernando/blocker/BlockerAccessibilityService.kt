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

        if (pkg in BlockList.browserPackages) {
            val urlText = findUrlBarText(root)
            if (BlockList.matches(urlText)) {
                triggerBlock()
                return
            }
        }

        val screenText = extractAllText(root)
        if (BlockList.matches(screenText)) {
            triggerBlock()
            return
        }

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
