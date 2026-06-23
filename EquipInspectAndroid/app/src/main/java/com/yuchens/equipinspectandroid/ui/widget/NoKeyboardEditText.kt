package com.yuchens.equipinspectandroid.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.appcompat.widget.AppCompatEditText

class NoKeyboardEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection? {
        val inputConnection = super.onCreateInputConnection(outAttrs)
        // 不顯示鍵盤
        outAttrs.imeOptions = outAttrs.imeOptions or EditorInfo.IME_FLAG_NO_EXTRACT_UI
        return inputConnection
    }

    override fun onCheckIsTextEditor(): Boolean {
        return false
    }

    fun clearAndRefocus() {
        setText("")
        requestFocus()
    }
}