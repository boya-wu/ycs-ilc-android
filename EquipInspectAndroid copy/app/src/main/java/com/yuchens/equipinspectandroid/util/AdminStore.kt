package com.yuchens.equipinspectandroid.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val DS_NAME = "admin_gatekeeper"

val Context.adminDataStore by preferencesDataStore(name = DS_NAME)

object AdminStore {
    private val KEY_ELEVATED_UNTIL     = longPreferencesKey("elev_until")
    private val KEY_LOCK_UNTIL         = longPreferencesKey("lock_until")
    private val KEY_ATTEMPT_COUNT      = intPreferencesKey("attempt_count")
    private val KEY_ATTEMPT_WIN_START  = longPreferencesKey("attempt_win_start")

    suspend fun getElevatedUntil(ctx: Context): Long =
        ctx.adminDataStore.data.map { it[KEY_ELEVATED_UNTIL] ?: 0L }.first()

    suspend fun setElevatedUntil(ctx: Context, until: Long) {
        ctx.adminDataStore.edit { it[KEY_ELEVATED_UNTIL] = until }
    }

    suspend fun clearElevation(ctx: Context) {
        ctx.adminDataStore.edit { it.remove(KEY_ELEVATED_UNTIL) }
    }

    suspend fun getLockUntil(ctx: Context): Long =
        ctx.adminDataStore.data.map { it[KEY_LOCK_UNTIL] ?: 0L }.first()

    suspend fun isLocked(ctx: Context, now: Long = System.currentTimeMillis()): Boolean =
        getLockUntil(ctx) > now

    suspend fun resetAttempts(ctx: Context) {
        ctx.adminDataStore.edit {
            it.remove(KEY_ATTEMPT_COUNT)
            it.remove(KEY_ATTEMPT_WIN_START)
            it.remove(KEY_LOCK_UNTIL)
        }
    }

    /**
     * 更新嘗試次數/視窗起點，並視需要設置 lock_until。
     * @return 更新後的嘗試次數
     */
    suspend fun registerFailedAttempt(
        ctx: Context,
        now: Long,
        attemptWindowMs: Long,
        lockoutMs: Long,
        maxAttempts: Int
    ): Int {
        var newCount = 0
        ctx.adminDataStore.edit { p ->
            val winStart = p[KEY_ATTEMPT_WIN_START] ?: 0L
            val inWindow = now - winStart <= attemptWindowMs
            val count    = if (inWindow) (p[KEY_ATTEMPT_COUNT] ?: 0) + 1 else 1
            val newStart = if (inWindow) winStart else now

            p[KEY_ATTEMPT_COUNT] = count
            p[KEY_ATTEMPT_WIN_START] = newStart
            if (count >= maxAttempts) {
                p[KEY_LOCK_UNTIL] = now + lockoutMs
            }
            newCount = count
        }
        return newCount
    }
}
