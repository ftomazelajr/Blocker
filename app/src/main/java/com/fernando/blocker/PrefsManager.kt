package com.fernando.blocker

import android.content.Context
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

object PrefsManager {
    private const val PREFS = "blocker_prefs"
    private const val KEY_STREAK_START = "streak_start"
    private const val KEY_BLOCK_COUNT = "block_count"
    private const val KEY_PIN_HASH = "pin_hash"
    private const val KEY_UNLOCK_UNTIL = "unlock_until"

    private const val UNLOCK_WINDOW_MS = 3 * 60 * 1000L

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun init(ctx: Context) {
        val p = prefs(ctx)
        if (!p.contains(KEY_STREAK_START)) {
            p.edit().putLong(KEY_STREAK_START, System.currentTimeMillis()).apply()
        }
    }

    fun registerBlock(ctx: Context) {
        val p = prefs(ctx)
        val count = p.getInt(KEY_BLOCK_COUNT, 0) + 1
        p.edit()
            .putInt(KEY_BLOCK_COUNT, count)
            .putLong(KEY_STREAK_START, System.currentTimeMillis())
            .apply()
    }

    fun getBlockCount(ctx: Context): Int = prefs(ctx).getInt(KEY_BLOCK_COUNT, 0)

    fun getStreakDays(ctx: Context): Int {
        val start = prefs(ctx).getLong(KEY_STREAK_START, System.currentTimeMillis())
        val diff = System.currentTimeMillis() - start
        return TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }

    private fun hash(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hasPin(ctx: Context): Boolean =
        prefs(ctx).contains(KEY_PIN_HASH)

    fun setPin(ctx: Context, pin: String) {
        prefs(ctx).edit().putString(KEY_PIN_HASH, hash(pin)).apply()
    }

    fun verifyPin(ctx: Context, pin: String): Boolean {
        val stored = prefs(ctx).getString(KEY_PIN_HASH, null) ?: return false
        return stored == hash(pin)
    }

    fun unlockTemporarily(ctx: Context) {
        val until = System.currentTimeMillis() + UNLOCK_WINDOW_MS
        prefs(ctx).edit().putLong(KEY_UNLOCK_UNTIL, until).apply()
    }

    fun isUnlocked(ctx: Context): Boolean {
        val until = prefs(ctx).getLong(KEY_UNLOCK_UNTIL, 0L)
        return System.currentTimeMillis() < until
    }
}
