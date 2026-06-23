// ConfigStore.kt
package com.yuchens.equipinspectandroid.util

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

private const val DS_NAME = "app_config"
val Context.configDataStore by preferencesDataStore(name = DS_NAME)

object ConfigStore {
    private val KEY_BASE_URL = stringPreferencesKey("base_url")

    private val KEY_LIMIT_WINDOW_MS = longPreferencesKey("limit_window_ms")

    const val DEFAULT_BASE_URL = "http://127.0.0.1"

    const val DEFAULT_LIMIT_WINDOW_MS = 60_000L

    suspend fun getBaseUrl(context: Context): String =
        context.configDataStore.data.map { it[KEY_BASE_URL] ?: DEFAULT_BASE_URL }.first()

    suspend fun setBaseUrl(context: Context, input: String): Boolean {
        val normalized = normalizeBaseUrl(input) ?: return false
        context.configDataStore.edit { it[KEY_BASE_URL] = normalized }
        return true
    }

    suspend fun getLimitWindowMs(context: Context): Long {
        return context.configDataStore.data
            .map { it[KEY_LIMIT_WINDOW_MS] ?: DEFAULT_LIMIT_WINDOW_MS }
            .first()
    }

    suspend fun setLimitWindowMs(context: Context, ms: Long) {
        context.configDataStore.edit { prefs ->
            prefs[KEY_LIMIT_WINDOW_MS] = ms
        }
    }

    /**
     * 規則：
     * - 若沒寫 http/https，預設 http
     * - 驗證為合法 URL（OkHttp 檢查）
     * - 去掉結尾單一斜線（保持與你現有拼字串邏輯相容）
     * - 支援帶路徑（如 http://host:8080/api）
     */
    fun normalizeBaseUrl(raw: String): String? {
        var s = raw.trim()
        if (!s.startsWith("http://") && !s.startsWith("https://")) s = "http://$s"
        val ok = s.toHttpUrlOrNull() ?: return null
        // OkHttp 會在空路徑時輸出結尾 "/"，這裡統一移除以便字串拼接
        return ok.toString().removeSuffix("/")
    }
}
