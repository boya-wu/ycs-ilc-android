package com.yuchens.equipinspectandroid.util

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object LogHelper {

    fun write(context: Context, message: String, exception: Throwable? = null) {
        val now = Date()
        val month = SimpleDateFormat("yyyyMM", Locale.getDefault()).format(now)
        val day = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(now)
        val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now)
        val fileName = "$day.txt"

        val line = buildString {
            appendLine("===== [$timeStr] $message =====")
            exception?.let { appendLine(it.stackTraceToString()) }
            appendLine()
        }

        // 寫 App 私有外部空間：/Android/data/<pkg>/files/Log/yyyyMM/dd.txt
        val privateDir = File(context.getExternalFilesDir(null), "Log/$month")
        if (!privateDir.exists()) privateDir.mkdirs()
        File(privateDir, fileName).appendText(line, Charsets.UTF_8)
    }
}
