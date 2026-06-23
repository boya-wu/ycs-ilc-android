package com.yuchens.equipinspectandroid

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.yuchens.equipinspectandroid.core.exception.GlobalExceptionHandler
import com.yuchens.equipinspectandroid.util.AdminGatekeeper
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.launch

@HiltAndroidApp
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val appCtx = applicationContext

        // 設定 Gatekeeper（與清除升權無相依）
        AdminGatekeeper.configure(
            AdminGatekeeper.Config(
                pinHashHex = BuildConfig.ADMIN_PIN_HASH,
                pepper     = BuildConfig.ADMIN_PIN_PEPPER
            )
        )

        // 啟動時清除升權（用 Process lifecycle 的 lifecycleScope 非阻塞執行）
        ProcessLifecycleOwner.get().lifecycleScope.launch {
            AdminGatekeeper.clearElevation(appCtx)
        }

        // App 進背景時清除升權
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                owner.lifecycleScope.launch {
                    AdminGatekeeper.clearElevation(appCtx)
                }
            }
        })

        // 你的全域 Exception Handler
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(
            GlobalExceptionHandler(this, defaultHandler)
        )
    }
}
