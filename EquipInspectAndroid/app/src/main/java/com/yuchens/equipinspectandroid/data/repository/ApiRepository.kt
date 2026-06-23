package com.yuchens.equipinspectandroid.data.repository

import android.content.Context
import com.yuchens.equipinspectandroid.data.mapper.toUploadDto
import com.yuchens.equipinspectandroid.data.remote.ApiParser
import com.yuchens.equipinspectandroid.data.remote.ApiConfig
import com.yuchens.equipinspectandroid.data.remote.dto.DownloadDto
import com.yuchens.equipinspectandroid.data.remote.dto.EquipDto
import com.yuchens.equipinspectandroid.data.remote.dto.UploadBody
import com.yuchens.equipinspectandroid.util.AESHelper
import com.yuchens.equipinspectandroid.util.DeviceIdProvider
import com.yuchens.equipinspectandroid.util.LogHelper
import com.yuchens.equipinspectandroid.util.UserPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Locale

object ApiRepository {

    suspend fun ping(
        context: Context,
        repo: LocalRepository
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = buildDeviceToken(context)
            val reqBody = "{}".toRequestBody(ApiConfig.jsonMediaType)

            val req = Request.Builder()
                .url(ApiConfig.join(ApiConfig.baseUrl(context), ApiConfig.PING_PATH))
                .post(reqBody)
                .addHeader("X-Device-Token", token)
                .build()

            val (code, respBody) = ApiConfig.client.newCall(req).execute().use { resp ->
                resp.code to (resp.body.string())
            }

            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun download(
        context: Context,
        repo: LocalRepository
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val token = buildDeviceToken(context)
            val reqBody = "{}".toRequestBody(ApiConfig.jsonMediaType)

            val req = Request.Builder()
                .url(ApiConfig.join(ApiConfig.baseUrl(context), ApiConfig.DOWNLOAD_PATH))
                .post(reqBody)
                .addHeader("X-Device-Token", token)
                .build()

            val (code, respBody) = ApiConfig.client.newCall(req).execute().use { resp ->
                resp.code to (resp.body.string())
            }

            val dto: DownloadDto = ApiParser.parseHttpOrThrow(code, respBody)

            repo.replaceAllFromDownload(dto)
            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun upload(
        context: Context,
        repo: LocalRepository
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 1) 取出「已巡檢」資料 → DTO
            val inspected: List<EquipDto> = repo.getEquipInspected().map { it.toUploadDto() }
            if (inspected.isEmpty()) return@withContext Result.success(Unit)

            // 2) 外框 + 轉 JSON（不加密）
            val payload = UploadBody(liEquipDetail = inspected)
            val jsonString = ApiParser.json.encodeToString(UploadBody.serializer(), payload)
            
            // 3) 建請求（驗證在 X-Device-Token）
            val token = buildDeviceToken(context)
            val reqBody = jsonString.toRequestBody(ApiConfig.jsonMediaType)
            val req = Request.Builder()
                .url(ApiConfig.join(ApiConfig.baseUrl(context), ApiConfig.UPLOAD_PATH))
                .post(reqBody)
                .addHeader("X-Device-Token", token)
                .build()

            // 4) 發送並讀回應
            val (code, respBody) = ApiConfig.client.newCall(req).execute().use { resp ->
                resp.code to (resp.body.string())
            }

            // 5) 解析外框 Data 為成功筆數（Int）
            val successCount: Int = ApiParser.parseHttpOrThrow(code, respBody)

            // 6) 若成功筆數與送出筆數相同 → 刪除本地已上傳資料
            val toDeleteGuids = inspected.mapNotNull { it.guid }
            if (successCount == toDeleteGuids.size && toDeleteGuids.isNotEmpty()) {
                repo.deleteEquipByGuids(toDeleteGuids)
            }

            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    /** 產生 JSON Payload → AES 加密的 Token */
    private suspend fun buildDeviceToken(context: Context): String {
        val androidId = DeviceIdProvider.getPreferredIdentifier(context)
        val userNo = UserPrefs(context).username.firstOrNull().orEmpty()

        val payload = JSONObject()
            .put("AndroidId", androidId)
            .put("UserNo", userNo)

        return AESHelper.encrypt(payload.toString())
    }

}