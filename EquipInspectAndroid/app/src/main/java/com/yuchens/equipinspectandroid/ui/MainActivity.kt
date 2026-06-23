package com.yuchens.equipinspectandroid.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.yuchens.equipinspectandroid.R
import com.yuchens.equipinspectandroid.databinding.ActivityMainBinding
import com.yuchens.equipinspectandroid.ui.fragment.DownloadFragment
import com.yuchens.equipinspectandroid.ui.fragment.InspectFragment
import com.yuchens.equipinspectandroid.ui.fragment.InspectedFragment
import com.yuchens.equipinspectandroid.ui.fragment.SettingFragment
import com.yuchens.equipinspectandroid.ui.fragment.UninspectedFragment
import com.yuchens.equipinspectandroid.ui.fragment.UploadFragment
import com.yuchens.equipinspectandroid.util.AdminGatekeeper
import com.yuchens.equipinspectandroid.util.DeviceIdProvider
import com.yuchens.equipinspectandroid.util.LogExporter
import com.yuchens.equipinspectandroid.util.LogHelper
import com.yuchens.equipinspectandroid.ui.viewmodel.NavViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val vm: NavViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 觀察 Tab，統一導頁
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.current.collect { tab ->
                    when (tab) {
                        NavViewModel.Tab.UNINSPECTED -> replaceFragment(UninspectedFragment(), addToBackStack = false, tag = "TAB_UNINSPECTED")
                        NavViewModel.Tab.INSPECTED   -> replaceFragment(InspectedFragment(),   addToBackStack = false, tag = "TAB_INSPECTED")
                        NavViewModel.Tab.INSPECT     -> replaceFragment(InspectFragment(),     addToBackStack = false, tag = "TAB_INSPECT")
                        NavViewModel.Tab.DOWNLOAD    -> replaceFragment(DownloadFragment(),    addToBackStack = false, tag = "TAB_DOWNLOAD")
                        NavViewModel.Tab.UPLOAD      -> replaceFragment(UploadFragment(),      addToBackStack = false, tag = "TAB_UPLOAD")
                    }
                }
            }
        }


        // 初始首頁
        if (savedInstanceState == null) {
            vm.select(NavViewModel.Tab.INSPECT)
        }

        // 先依狀態刷新一次群組顯示
        lifecycleScope.launch {
            refreshAdminMenuVisibility()
        }

        // 抽屜打開時也刷新（避免升權過期但 UI 沒更新）
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerOpened(drawerView: View) {
                lifecycleScope.launch {
                    refreshAdminMenuVisibility()
                    enforceAdminGuardIfNeeded()
                }
            }
            override fun onDrawerClosed(drawerView: View) {}
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerStateChanged(newState: Int) {}
        })

        // 側邊選單點擊事件
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {

                // 系統管理者：點了會跳 PIN，成功後顯示群組
                R.id.menu_admin -> {
                    lifecycleScope.launch {
                        AdminGatekeeper.requireAdminOrElevate(
                            activity = this@MainActivity,
                            hasBaseAdmin = hasBaseAdmin(),
                        ) {
                            // onGranted 是一般 callback，裡面要再開協程才能呼叫 suspend 方法
                            lifecycleScope.launch {
                                refreshAdminMenuVisibility()
                                enforceAdminGuardIfNeeded()
                            }
                        }
                    }
                    true
                }

                R.id.menu_id -> {
                    lifecycleScope.launch {
                        if (!ensureAdminUnlocked()) {
                            // 未升權：僅提示（ensureAdminUnlocked 內已顯示 Toast）
                            return@launch
                        }
                        val androidId = DeviceIdProvider.getPreferredIdentifier(this@MainActivity)
                        LogHelper.write(this@MainActivity, "Android ID：$androidId")
                        Toast.makeText(this@MainActivity, "取得裝置資訊成功", Toast.LENGTH_SHORT).show()
                    }
                    true
                }

                R.id.menu_log -> {
                    lifecycleScope.launch {
                        if (!ensureAdminUnlocked()) {
                            // 未升權：僅提示
                            return@launch
                        }
                        val n = withContext(Dispatchers.IO) {
                            LogExporter.exportAll(this@MainActivity)
                        }
                        Toast.makeText(
                            this@MainActivity,
                            if (n > 0) "匯出LOG紀錄完成：$n 個檔案"
                            else "沒有可匯出的LOG紀錄檔案",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    true
                }

                R.id.menu_setting -> {
                    lifecycleScope.launch {
                        if (!ensureAdminUnlocked()) return@launch
                        replaceFragment(
                            fragment = SettingFragment(),
                            addToBackStack = false,
                            tag = "PAGE_SETTING",
                            withAnim = true
                        )
                    }
                    true
                }

                else -> false
            }.also {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            refreshAdminMenuVisibility()
            enforceAdminGuardIfNeeded()
        }
    }

    // ====== 共用方法 ======

    /** 若系統另外有帳號權限，再另外處理 */
    private fun hasBaseAdmin(): Boolean = false

    /** 依升權狀態顯示/隱藏管理群組（suspend） */
    private suspend fun refreshAdminMenuVisibility() {
        val show = hasBaseAdmin() || AdminGatekeeper.isElevated(this)
        withContext(Dispatchers.Main) {
            binding.navigationView.menu.setGroupVisible(R.id.group_admin, show)
        }
    }

    /** 若未升權，提示用戶先點「系統管理者」（suspend） */
    private suspend fun ensureAdminUnlocked(): Boolean {
        val unlocked = hasBaseAdmin() || AdminGatekeeper.isElevated(this)
        if (!unlocked) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "請先點『系統管理者』並輸入 PIN", Toast.LENGTH_SHORT).show()
            }
        }
        return unlocked
    }

    /** 若目前是需要管理權的頁面（例如 SettingFragment），但現在未升權 → 直接導回 InspectFragment（suspend） */
    private suspend fun enforceAdminGuardIfNeeded() {
        val unlocked = hasBaseAdmin() || AdminGatekeeper.isElevated(this)
        if (!unlocked) {
            withContext(Dispatchers.Main) {
                val current = supportFragmentManager.findFragmentById(R.id.containerFragment)
                if (current is SettingFragment) {
                    supportFragmentManager.popBackStack(
                        null,
                        androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    // 回到 Inspect（不入棧）
                    vm.select(NavViewModel.Tab.INSPECT)
                    Toast.makeText(this@MainActivity, "已關閉管理者模式", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /** 更換中間內容 Fragment（分頁不入棧，深入頁才入棧） */
    private fun replaceFragment(
        fragment: Fragment,
        addToBackStack: Boolean = false,
        tag: String = fragment::class.java.name,
        withAnim: Boolean = false
    ) {
        // 若類型相同且 tag 一致就不重做
        val current = supportFragmentManager.findFragmentById(R.id.containerFragment)
        if (current?.javaClass == fragment.javaClass && current.tag == tag) return

        supportFragmentManager.beginTransaction().apply {
            setReorderingAllowed(true)
            if (withAnim) {
                setCustomAnimations(
                    R.anim.fade_in,  // 進
                    R.anim.fade_out, // 出
                    R.anim.fade_in_pop,  // 返回進
                    R.anim.fade_out_pop  // 返回出
                )
            }
            replace(R.id.containerFragment, fragment, tag)
            if (addToBackStack) addToBackStack(tag)   // 只有需要時才加入 back stack
        }.commit()
    }


    /** 供 Fragment 呼叫以開啟 Drawer */
    fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }
}
