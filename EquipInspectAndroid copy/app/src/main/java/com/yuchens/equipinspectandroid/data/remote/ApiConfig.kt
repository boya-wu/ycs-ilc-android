package com.yuchens.equipinspectandroid.data.remote

import android.content.Context
import com.yuchens.equipinspectandroid.util.ConfigStore
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient

object ApiConfig {
    const val PING_PATH = "/api/app/ping"
    const val DOWNLOAD_PATH = "/api/app/download"
    const val UPLOAD_PATH = "/api/app/upload"

    // suspend 版本
    suspend fun baseUrl(context: Context): String = ConfigStore.getBaseUrl(context)

    fun join(base: String, path: String): String {
        // base 不以 / 結尾、path 以 / 開頭 ⇒ 安全拼接
        val b = base.removeSuffix("/")
        val p = if (path.startsWith("/")) path else "/$path"
        return b + p
    }

    val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    val client: OkHttpClient by lazy { OkHttpClient.Builder().build() }
}
