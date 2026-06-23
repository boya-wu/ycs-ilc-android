package com.yuchens.equipinspectandroid.ui.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class VerticalSpaceItemDecoration(private val verticalSpaceHeight: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        // 第一個 item 不加間距，之後每個 item 上面加 space
        if (parent.getChildAdapterPosition(view) != 0) {
            outRect.top = verticalSpaceHeight
        }
    }
}
