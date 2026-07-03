package com.fernando.blocker

import android.content.Context
import java.util.concurrent.TimeUnit

object PrefsManager {
    private const val PREFS = "blocker_prefs"
    private const val KEY_STREAK_START = "streak_start"
    private const val KEY_BLOCK_COUNT = "block_count"

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
            .putLong(KEY_STREAK_START, System.currentTimeMillis()) // reseta streak
            .apply()
    }

    fun getBlockCount(ctx: Context): Int =
        prefs(ctx).getInt(KEY_BLOCK_COUNT, 0)

    fun getStreakDays(ctx: Context): Int {
        val start = prefs(ctx).getLong(KEY_STREAK_START, System.currentTimeMillis())
        val diff = System.currentTimeMillis() - start
        return TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }
}
