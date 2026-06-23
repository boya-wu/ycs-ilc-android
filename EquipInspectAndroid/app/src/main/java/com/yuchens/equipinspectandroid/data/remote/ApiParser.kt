package com.yuchens.equipinspectandroid.data.remote

import kotlinx.serialization.json.Json
import com.yuchens.equipinspectandroid.data.remote.dto.ApiEnvelope

object ApiParser {
    // 全域唯一的 Json 設定（避免每次 new）
    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    // 解析整個外框：取得 ApiEnvelope<T>
    inline fun <reified T> decodeEnvelope(body: String): ApiEnvelope<T> =
        json.decodeFromString(body)

    // 解析並強制回傳 Data，若 Success=false 或 Data=null 直接丟例外
    inline fun <reified T> requireData(body: String): T {
        val env = decodeEnvelope<T>(body)
        if (!env.success) throw IllegalStateException(env.message ?: "伺服器回傳失敗")
        return env.data ?: throw IllegalStateException("Data 為空")
    }

    // HTTP 狀態碼
    inline fun <reified T> parseHttpOrThrow(code: Int, body: String): T {
        if (code !in 200..299) throw IllegalStateException("HTTP $code")
        if (body.isBlank())     throw IllegalStateException("空白回應")
        return requireData<T>(body)
    }
}
