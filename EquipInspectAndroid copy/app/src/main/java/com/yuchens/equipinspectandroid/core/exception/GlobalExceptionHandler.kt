package com.yuchens.equipinspectandroid.core.exception

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class GlobalExceptionHandler(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            writeErrorLog(thread, throwable)
        } catch (_: Exception) { /* swallow */ }

        defaultHandler?.uncaughtException(thread, throwable)
    }

    private fun writeErrorLog(thread: Thread, throwable: Throwable) {
        val now = Date()
        val month = SimpleDateFormat("yyyyMM", Locale.getDefault()).format(now)
        val day = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(now)
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now)
        val fileName = "$day.txt"

        val line = buildString {
            appendLine("===== [$timeStr] Crash in thread: ${thread.name} =====")
            appendLine(throwable.stackTraceToString())
            appendLine()
        }

        // 寫 App 私有外部空間：/Android/data/<pkg>/files/ErrorLog/yyyyMM/dd.txt
        val privateDir = File(context.getExternalFilesDir(null), "ErrorLog/$month")
        if (!privateDir.exists()) privateDir.mkdirs()
        File(privateDir, fileName).appendText(line, Charsets.UTF_8)

    }
}
