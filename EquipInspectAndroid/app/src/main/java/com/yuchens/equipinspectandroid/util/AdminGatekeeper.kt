package com.yuchens.equipinspectandroid.util

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object AdminGatekeeper {

    // ===== 設定 =====
    data class Config(val pinHashHex: String, val pepper: String)
    @Volatile private var config: Config? = null
    fun configure(cfg: Config) { config = cfg }

    private val pinHashHex get() = config?.pinHashHex.orEmpty()
    private val pepper get() = config?.pepper.orEmpty()

    // ===== 可調參數 =====
    var elevationMinutes: Long = 10
    private const val MAX_ATTEMPTS_PER_WINDOW = 5
    private val ATTEMPT_WINDOW_MS = TimeUnit.MINUTES.toMillis(5)
    private val LOCKOUT_MS = TimeUnit.MINUTES.toMillis(5)

    private enum class PinResult { SUCCESS, CANCELLED }

    // ===== 對外 API（全程 suspend，無 lifecycleScope 依賴） =====
    suspend fun isElevated(context: Context): Boolean =
        System.currentTimeMillis() < AdminStore.getElevatedUntil(context)

    suspend fun clearElevation(context: Context) {
        AdminStore.clearElevation(context)
    }

    /**
     * 若 hasBaseAdmin 或已升權，直接 onGranted()；
     * 否則顯示 PIN 對話框，連續錯誤會逐次計數並在達到上限時自動鎖定/關閉。
     */
    suspend fun requireAdminOrElevate(
        activity: Activity,
        hasBaseAdmin: Boolean,
        onGranted: () -> Unit
    ) {
        if (hasBaseAdmin || isElevated(activity)) {
            onGranted(); return
        }
        if (pinHashHex.isBlank()) {
            Toast.makeText(activity, "尚未設定管理者 PIN", Toast.LENGTH_SHORT).show()
            return
        }
        if (AdminStore.isLocked(activity)) {
            val left = (AdminStore.getLockUntil(activity) - System.currentTimeMillis()).coerceAtLeast(0L)
            Toast.makeText(activity, "PIN 嘗試過多，請稍後（${left / 1000}s）", Toast.LENGTH_SHORT).show()
            return
        }

        when (awaitPinSuccessOrCancel(activity)) {
            PinResult.SUCCESS -> {
                val until = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(elevationMinutes)
                AdminStore.setElevatedUntil(activity, until)
                AdminStore.resetAttempts(activity)
                Toast.makeText(activity, "已開啟管理者模式（${elevationMinutes} 分）", Toast.LENGTH_SHORT).show()
                onGranted()
            }
            PinResult.CANCELLED -> {
                // 使用者自行取消／或被鎖定自動關閉 → 不處理
            }
        }
    }

    /**
     * 單一對話框「內部自迴圈」：
     * - 驗證成功：關閉對話框並回傳 SUCCESS（resume 一次）
     * - 驗證失敗：立即計次（DataStore），不關閉對話框，讓使用者可繼續輸入
     * - 達到上限 → 設定鎖定、提示剩餘秒數、關閉對話框並回傳 CANCELLED
     * - 使用者按取消：關閉對話框並回傳 CANCELLED
     */
    private suspend fun awaitPinSuccessOrCancel(activity: Activity): PinResult =
        suspendCancellableCoroutine { cont ->
            val input = EditText(activity).apply {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                transformationMethod = PasswordTransformationMethod.getInstance()
                // 僅允許可見 ASCII（0x20..0x7E），長度上限 64
                val printableAscii = InputFilter { source, _, _, _, _, _ ->
                    if (source.isEmpty()) null
                    else {
                        val out = source.filter { ch -> ch.code in 0x20..0x7E }
                        if (out.length == source.length) null else out
                    }
                }
                filters = arrayOf(InputFilter.LengthFilter(64), printableAscii)
                hint = "請輸入管理者 PIN"
            }

            val container = FrameLayout(activity).apply {
                val pad = (16 * resources.displayMetrics.density).toInt()
                setPadding(pad, pad, pad, pad)
                addView(input)
            }

            val dialog = AlertDialog.Builder(activity)
                .setTitle("管理者驗證")
                .setView(container)
                .setPositiveButton("確認", null)
                .setNegativeButton("取消", null)
                .setCancelable(false)
                .create()

            dialog.setOnShowListener {
                val ok = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                val cancel = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

                fun handleLockedAndClose() {
                    // 計次後可能剛好進入鎖定，這裡立即提示並關閉
                    val leftMs = runBlocking { AdminStore.getLockUntil(activity) } - System.currentTimeMillis()
                    if (leftMs > 0) {
                        Toast.makeText(activity, "PIN 嘗試過多，請稍後（${leftMs / 1000}s）", Toast.LENGTH_SHORT).show()
                        hideIme(input, dialog)
                        dialog.dismiss()
                        if (cont.isActive) cont.resume(PinResult.CANCELLED)
                    }
                }

                ok.setOnClickListener {
                    val pin = input.text?.toString().orEmpty()
                    if (verifyPin(pin)) {
                        hideIme(input, dialog)
                        dialog.dismiss()
                        if (cont.isActive) cont.resume(PinResult.SUCCESS)
                    } else {
                        // 每次失敗都即時計次（DataStore 原子更新），不關閉對話框
                        input.error = "PIN 錯誤"
                        runBlocking {
                            AdminStore.registerFailedAttempt(
                                ctx = activity,
                                now = System.currentTimeMillis(),
                                attemptWindowMs = ATTEMPT_WINDOW_MS,
                                lockoutMs = LOCKOUT_MS,
                                maxAttempts = MAX_ATTEMPTS_PER_WINDOW
                            )
                        }
                        handleLockedAndClose()
                    }
                }

                cancel.setOnClickListener {
                    hideIme(input, dialog)
                    dialog.dismiss()
                    if (cont.isActive) cont.resume(PinResult.CANCELLED)
                }
            }

            cont.invokeOnCancellation { dialog.dismiss() }
            dialog.show()
        }

    // ===== Hash 與驗證 =====
    private fun sha256Hex(s: String): String {
        val d = MessageDigest.getInstance("SHA-256").digest(s.toByteArray(Charsets.UTF_8))
        return d.joinToString("") { "%02x".format(it) }
    }

    private fun verifyPin(pin: String): Boolean {
        // 不 trim；空白也視為有效字元
        val guess = sha256Hex("$pepper:$pin")
        return guess.equals(pinHashHex, ignoreCase = true)
    }

    /** 收起鍵盤（先 IMM，再 WindowCompat 補強），並清焦點 */
    private fun hideIme(view: View, dialog: Dialog? = null) {
        // 1) 傳統 IMM
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        // 2) 以 Window 為單位隱藏 IME
        dialog?.window?.let { win ->
            val controller = WindowCompat.getInsetsController(win, win.decorView)
            controller.hide(WindowInsetsCompat.Type.ime())
        }
        view.clearFocus()
    }
}
