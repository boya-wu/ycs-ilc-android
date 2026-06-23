package com.yuchens.equipinspectandroid.util

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.IOException

object LogExporter {
    private const val TAG = "LogExporter"

    /** 從主要外部私有的 ErrorLog/Log 匯出到 Downloads；回傳成功寫入的檔案數 */
    fun exportAll(context: Context): Int {
        val base = context.getExternalFilesDir(null) ?: return 0
        val srcRoots = listOf(File(base, "ErrorLog"), File(base, "Log"))

        var total = 0
        srcRoots.forEach { root ->
            if (!root.exists()) {
                Log.d(TAG, "skip: ${root.absolutePath} not exists")
                return@forEach
            }
            root.walkTopDown().filter { it.isFile }.forEach { file ->
                val month = file.parentFile?.name.orEmpty() // yyyyMM
                val relPath = "Download/EquipInspect/${root.name}/${month.ifBlank { "" }}".trimEnd('/')
                try {
                    val bytes = file.readBytes()
                    Log.d(TAG, "export: ${file.name} (${bytes.size} bytes) -> $relPath/")
                    upsertIntoDownloads(context, relPath, file.name, bytes)
                    total++
                } catch (e: Exception) {
                    Log.w(TAG, "export failed: ${file.absolutePath}", e)
                }
            }
        }
        Log.d(TAG, "export done. total=$total")
        return total
    }

    private fun upsertIntoDownloads(context: Context, relPath: String, fileName: String, bytes: ByteArray) {
        if (Build.VERSION.SDK_INT >= 29) {
            val cr = context.contentResolver
            val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
            val rel = ensureSlash(relPath) // 一律以斜線結尾

            // 先找同名同路徑
            val projection = arrayOf(MediaStore.MediaColumns._ID)
            val selection = "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?"
            val selectionArgs = arrayOf(fileName, rel)

            val uri = cr.query(collection, projection, selection, selectionArgs, null).use { c ->
                if (c != null && c.moveToFirst()) {
                    ContentUris.withAppendedId(collection, c.getLong(0))
                } else {
                    val values = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, rel) // **保持尾斜線**
                    }
                    cr.insert(collection, values)
                }
            } ?: throw IOException("insert MediaStore failed for $rel$fileName")

            // 覆寫寫入；openOutputStream 可能回 null，要檢查
            cr.openOutputStream(uri, "w")?.use { it.write(bytes) }
                ?: throw IOException("openOutputStream returned null for uri=$uri")
        } else {
            // Android 9-
            val base = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val targetDir = File(base, relPath.removePrefix("Download/"))
            if (!targetDir.exists()) targetDir.mkdirs()
            File(targetDir, fileName).writeBytes(bytes)
        }
    }

    private fun ensureSlash(path: String) = if (path.endsWith("/")) path else "$path/"
}
