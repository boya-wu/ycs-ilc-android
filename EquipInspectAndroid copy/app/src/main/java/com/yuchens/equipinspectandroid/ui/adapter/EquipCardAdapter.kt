package com.yuchens.equipinspectandroid.ui.adapter

import android.animation.ValueAnimator
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.core.animation.doOnCancel
import androidx.core.animation.doOnEnd
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.yuchens.equipinspectandroid.R
import com.yuchens.equipinspectandroid.ui.model.EquipUi
import java.lang.ref.WeakReference
import java.util.WeakHashMap
import kotlin.math.max
import androidx.core.view.isVisible

class EquipCardAdapter(
    private val onItemClick: ((EquipUi) -> Unit)? = null
) : ListAdapter<EquipUi, EquipCardAdapter.VH>(DIFF) {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val txtBarcode: TextView = view.findViewById(R.id.txtBarcode)
        val txtItemName: TextView = view.findViewById(R.id.txtItemName)
        val txtAreaName: TextView = view.findViewById(R.id.txtAreaName)
        val rowAbnormal: View = view.findViewById(R.id.rowAbnormal)
        val txtAbnormalName: TextView = view.findViewById(R.id.txtAbnormalName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_equip_card, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val e = getItem(position)
        val c = holder.itemView.context

        holder.txtBarcode.text = e.barcode
        holder.txtItemName.text = c.getString(
            R.string.desc_category_item_format, e.categoryName, e.itemName
        )
        holder.txtAreaName.text = c.getString(
            R.string.desc_building_area_format, e.buildingName, e.areaName
        )

        // 異常列顯示/隱藏
        val inspected = e.inspected
        if (!inspected) {
            holder.rowAbnormal.visibility = View.GONE
            holder.txtAbnormalName.text = "" // 清空，避免回收殘留
        } else {
            holder.rowAbnormal.visibility = View.VISIBLE
            holder.txtAbnormalName.text = e.abnormal.orEmpty().trim()
        }

        // 先全部復位（避免回收造成殘留）
        resetMarquee(holder.txtBarcode)
        resetMarquee(holder.txtItemName)
        resetMarquee(holder.txtAreaName)
        resetMarquee(holder.txtAbnormalName)

        // 綁點擊跑馬燈（只對可見的 TextView 綁定）
        attachMarqueeClick(holder, holder.txtBarcode)
        attachMarqueeClick(holder, holder.txtItemName)
        attachMarqueeClick(holder, holder.txtAreaName)
        if (holder.rowAbnormal.isVisible) {
            attachMarqueeClick(holder, holder.txtAbnormalName)
        } else {
            holder.txtAbnormalName.setOnClickListener(null)
        }

        // 整卡點擊（開 BottomSheet）
        holder.itemView.setOnClickListener { onItemClick?.invoke(e) }
    }

    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
        resetMarquee(holder.txtBarcode)
        resetMarquee(holder.txtItemName)
        resetMarquee(holder.txtAreaName)
        resetMarquee(holder.txtAbnormalName)
        if (currentRunningTv?.get() in listOf(
                holder.txtBarcode, holder.txtItemName, holder.txtAreaName, holder.txtAbnormalName
            )) currentRunningTv = null
    }

    // ===== 跑馬燈（滑到尾端就停住）共用實作 =====

    // 同時間只允許一個 TextView 在跑
    private var currentRunningTv: WeakReference<TextView>? = null
    private val runningAnimators = WeakHashMap<TextView, ValueAnimator>()
    private val pxPerSecond = 70f

    private fun attachMarqueeClick(holder: VH, tv: TextView) {
        tv.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener

            // 停掉其他正在跑的
            currentRunningTv?.get()?.let { prev ->
                if (prev !== tv) resetMarquee(prev)
            }
            // 同一個正在跑 → 重播
            if (runningAnimators.containsKey(tv)) resetMarquee(tv)

            runMarqueeOnce(tv)

            // 如希望點文字也算卡片點擊，保留；不需要可移除
            onItemClick?.invoke(getItem(pos))
        }
    }

    /** 從起點滑到尾端後停住（不跳回頭） */
    private fun runMarqueeOnce(tv: TextView) {
        resetMarquee(tv)

        tv.isSingleLine = true
        tv.setHorizontallyScrolling(true)
        tv.ellipsize = null // 動畫期間不顯示 "..."

        waitUntilLaidOut(tv) {
            val layout = tv.layout
            val textWidth = layout?.getLineWidth(0)
                ?: tv.paint.measureText(tv.text.toString())
            val available = (tv.width - tv.paddingLeft - tv.paddingRight).toFloat()
            val distance = max(0f, textWidth - available)
            if (distance <= 0f) {
                tv.ellipsize = TextUtils.TruncateAt.END
                return@waitUntilLaidOut
            }

            val durationMs = ((distance / pxPerSecond) * 1000).toLong().coerceAtLeast(300L)
            val animator = ValueAnimator.ofInt(0, distance.toInt()).apply {
                duration = durationMs
                interpolator = LinearInterpolator()
                addUpdateListener { va -> tv.scrollTo(va.animatedValue as Int, 0) }
                doOnEnd {
                    runningAnimators.remove(tv)
                    if (currentRunningTv?.get() === tv) currentRunningTv = null
                }
                doOnCancel { runningAnimators.remove(tv) }
            }
            currentRunningTv = WeakReference(tv)
            runningAnimators[tv] = animator
            animator.start()
        }
    }

    /** 停止並復位（回起點、恢復 … 顯示） */
    private fun resetMarquee(tv: TextView) {
        runningAnimators[tv]?.cancel()
        runningAnimators.remove(tv)
        tv.scrollTo(0, 0)
        tv.ellipsize = TextUtils.TruncateAt.END
    }

    /** 若尚未 layout，掛一次 GlobalLayout；完成後執行 block。 */
    private inline fun waitUntilLaidOut(tv: View, crossinline block: () -> Unit) {
        if (tv.width > 0 && tv.height > 0 && tv.isLaidOut) { block(); return }
        val vto = tv.viewTreeObserver
        val listener = object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (!vto.isAlive) return
                tv.viewTreeObserver.removeOnGlobalLayoutListener(this)
                block()
            }
        }
        vto.addOnGlobalLayoutListener(listener)
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<EquipUi>() {
            override fun areItemsTheSame(old: EquipUi, new: EquipUi) = old.guid == new.guid
            override fun areContentsTheSame(old: EquipUi, new: EquipUi) = old == new
        }
    }
}
