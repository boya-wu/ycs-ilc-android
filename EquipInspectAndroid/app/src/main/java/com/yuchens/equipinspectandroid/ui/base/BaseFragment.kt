package com.yuchens.equipinspectandroid.ui.base

import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.transition.TransitionManager
import androidx.transition.ChangeBounds
import com.yuchens.equipinspectandroid.R
import kotlin.math.abs
import kotlin.math.roundToInt

abstract class BaseFragment : Fragment() {
    private var isExpanded = false       // true = nav 被收起、內容全屏
    private var isAnimating = false

    private var lastDir = 0              // 1=向下, -1=向上, 0=未定
    private var accum = 0                // 同方向累積距離(px)

    private val animationDuration = 200L

    // ★ 離開畫面時，無動畫地把 Nav 還原顯示，避免返回上一頁時消失
    override fun onPause() {
        super.onPause()
        forceShowNavs()
        // 清掉狀態，避免影響下一頁
        accum = 0
        lastDir = 0
    }

    fun setupRecyclerViewScrollBehavior(
        recyclerView: RecyclerView,
        topNav: View,
        bottomNav: View
    ) {
        val density = recyclerView.resources.displayMetrics.density
        val thresholdPx = (12f * density).roundToInt()

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!isAdded || isDetached || isAnimating) return
                if (dy == 0) return

                val dir = if (dy > 0) 1 else -1
                if (dir != lastDir) { accum = 0; lastDir = dir }
                accum += abs(dy)

                if (dir == 1 && !isExpanded && accum > thresholdPx) {
                    hideNavs(topNav, bottomNav); accum = 0
                } else if (dir == -1 && isExpanded && accum > thresholdPx) {
                    showNavs(topNav, bottomNav); accum = 0
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    accum = 0; lastDir = 0
                    if (isExpanded && !recyclerView.canScrollVertically(-1)) {
                        showNavs(topNav, bottomNav)
                    }
                }
            }
        })
    }

    private fun hideNavs(topNav: View, bottomNav: View) {
        if (isExpanded || isAnimating) return
        isAnimating = true
        isExpanded = true

        topNav.animate().cancel()
        bottomNav.animate().cancel()

        updateContainerConstraints(expand = true, animate = true)

        topNav.animate()
            .translationY(-topNav.height.toFloat())
            .setDuration(animationDuration)
            .withEndAction { isAnimating = false }
            .start()

        bottomNav.animate()
            .translationY(bottomNav.height.toFloat())
            .setDuration(animationDuration)
            .start()
    }

    private fun showNavs(topNav: View, bottomNav: View) {
        if (!isExpanded || isAnimating) return
        isAnimating = true
        isExpanded = false

        topNav.animate().cancel()
        bottomNav.animate().cancel()

        updateContainerConstraints(expand = false, animate = true)

        topNav.animate()
            .translationY(0f)
            .setDuration(animationDuration)
            .withEndAction { isAnimating = false }
            .start()

        bottomNav.animate()
            .translationY(0f)
            .setDuration(animationDuration)
            .start()
    }

    // ★ 無動畫強制顯示（供 onPause 使用）
    private fun forceShowNavs() {
        val act = activity ?: return
        val topNav = act.findViewById<View>(R.id.topNavFragment) ?: return
        val bottomNav = act.findViewById<View>(R.id.bottomNavFragment) ?: return

        topNav.animate().cancel()
        bottomNav.animate().cancel()

        // 直接歸零位移，無動畫
        topNav.translationY = 0f
        bottomNav.translationY = 0f

        isAnimating = false
        isExpanded = false

        // 也把內容區的 Constraint 還原（無動畫）
        updateContainerConstraints(expand = false, animate = false)
    }

    // ★ 新增 animate 參數：onPause 時避免過場閃爍
    private fun updateContainerConstraints(expand: Boolean, animate: Boolean) {
        val activity = activity ?: return
        val rootLayout = activity.findViewById<ConstraintLayout>(R.id.rootConstraintLayout) ?: return

        val constraintSet = ConstraintSet().apply { clone(rootLayout) }
        constraintSet.clear(R.id.containerFragment, ConstraintSet.TOP)
        constraintSet.clear(R.id.containerFragment, ConstraintSet.BOTTOM)

        if (expand) {
            constraintSet.connect(R.id.containerFragment, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            constraintSet.connect(R.id.containerFragment, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        } else {
            constraintSet.connect(R.id.containerFragment, ConstraintSet.TOP, R.id.topNavFragment, ConstraintSet.BOTTOM)
            constraintSet.connect(R.id.containerFragment, ConstraintSet.BOTTOM, R.id.bottomNavFragment, ConstraintSet.TOP)
        }

        if (animate) {
            TransitionManager.beginDelayedTransition(rootLayout, ChangeBounds().apply { duration = animationDuration })
        }
        constraintSet.applyTo(rootLayout)
    }
}
