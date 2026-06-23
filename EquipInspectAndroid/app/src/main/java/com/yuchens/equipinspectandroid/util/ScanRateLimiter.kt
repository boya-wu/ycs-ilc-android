package com.yuchens.equipinspectandroid.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanRateLimiter @Inject constructor(
    @ApplicationContext private val appContext: Context
) {
    /** 若應該被擋，回傳剩餘毫秒；否則回 0 */
    suspend fun remainingIfBlocked(
        barcode: String,
        windowMs: Long,
        now: Long = System.currentTimeMillis()
    ): Long {
        val (lastBarcode, lastAtMs) = ScanRateLimitStore.read(appContext)
        val isDifferent = lastBarcode?.equals(barcode, ignoreCase = false) != true
        val delta = now - lastAtMs
        return if (isDifferent && delta < windowMs) (windowMs - delta) else 0L
    }

    /** 記錄本次刷碼 */
    suspend fun mark(barcode: String, now: Long = System.currentTimeMillis()) {
        ScanRateLimitStore.save(appContext, barcode, now)
    }

    suspend fun clear() {
        ScanRateLimitStore.clear(appContext)
    }
}
