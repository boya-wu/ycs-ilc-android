package com.yuchens.equipinspectandroid.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val DS_NAME = "scan_rate_limit"
val Context.scanRateLimitDataStore by preferencesDataStore(name = DS_NAME)

object ScanRateLimitStore {
    private val KEY_LAST_BARCODE = stringPreferencesKey("last_barcode")
    private val KEY_LAST_AT_MS   = longPreferencesKey("last_at_ms")

    suspend fun read(ctx: Context): Pair<String?, Long> =
        ctx.scanRateLimitDataStore.data
            .map { prefs -> (prefs[KEY_LAST_BARCODE]) to (prefs[KEY_LAST_AT_MS] ?: 0L) }
            .first()

    suspend fun save(ctx: Context, barcode: String, atMs: Long) {
        ctx.scanRateLimitDataStore.edit {
            it[KEY_LAST_BARCODE] = barcode
            it[KEY_LAST_AT_MS]   = atMs
        }
    }

    suspend fun clear(ctx: Context) {
        ctx.scanRateLimitDataStore.edit {
            it.remove(KEY_LAST_BARCODE)
            it.remove(KEY_LAST_AT_MS)
        }
    }
}
