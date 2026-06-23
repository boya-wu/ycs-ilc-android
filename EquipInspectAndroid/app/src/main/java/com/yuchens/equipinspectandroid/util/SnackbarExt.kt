package com.yuchens.equipinspectandroid.util

import android.content.res.ColorStateList
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.yuchens.equipinspectandroid.R

/**
 * 預設錨定到 BottomNav（R.id.bottomNavFragment），顯示在其「上方」的 Snackbar。
 * 若沒找到 BottomNav，則使用 Snackbar 預設位置（底部）。
 */
fun View.snack(
    message: CharSequence,
    duration: Int = Snackbar.LENGTH_SHORT,
    anchor: View? = null,
    anchorId: Int? = null,
    actionText: CharSequence? = null,
    action: (() -> Unit)? = null,
    @DrawableRes iconStart: Int? = null,
    @ColorInt bgColor: Int? = null,
    @ColorInt textColor: Int? = null,
    @ColorInt actionColor: Int? = null,
    overlapAnchorFraction: Float = 0.4f,   // 建議預設 0，不與 BottomNav 重疊
    fadeAnimation: Boolean = true,
) {
    val sb = Snackbar.make(this, message, duration)

    // 配色
    val defaultBg   = ContextCompat.getColor(context, R.color.gray)
    val defaultText = ContextCompat.getColor(context, R.color.primary)

    if (actionText != null && action != null) sb.setAction(actionText) { action() }
    sb.view.backgroundTintList = ColorStateList.valueOf(bgColor ?: defaultBg)
    if (actionColor != null) sb.setActionTextColor(actionColor)

    val tv = sb.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    tv.setTextColor(textColor ?: defaultText)
    iconStart?.let {
        tv.setCompoundDrawablesWithIntrinsicBounds(it, 0, 0, 0)
        tv.compoundDrawablePadding = (8 * resources.displayMetrics.density).toInt()
        tv.maxLines = 4
    }

    if (fadeAnimation) {
        sb.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
    }

    // 解析錨點：傳入的優先，其次 anchorId，最後預設找 bottomNavFragment
    val resolvedAnchor: View? = when {
        anchor != null -> anchor
        anchorId != null -> rootView.findViewById(anchorId)
        else -> rootView.findViewById(R.id.bottomNavFragment)
    }

    // 沒有錨點：走預設（底部）
    if (resolvedAnchor == null) {
        sb.show()
        return
    }

    // 有錨點：先設定 anchor，再決定是否要「部分重疊」
    fun showWithOverlap(v: View) {
        sb.anchorView = v
        val h = v.height
        if (overlapAnchorFraction > 0f && h > 0) {
            // 先定位再 show，避免先出現再上移的跳動
            sb.view.translationY = h * overlapAnchorFraction
        }
        sb.show()
    }

    if (resolvedAnchor.height == 0 && overlapAnchorFraction > 0f) {
        // 錨點尚未 layout（拿不到高度），等它量完再顯示
        resolvedAnchor.doOnLayout { showWithOverlap(resolvedAnchor) }
    } else {
        showWithOverlap(resolvedAnchor)
    }
}
