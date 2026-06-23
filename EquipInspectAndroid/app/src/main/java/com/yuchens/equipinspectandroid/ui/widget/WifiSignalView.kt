package com.yuchens.equipinspectandroid.ui.widget

import android.content.Context
import android.net.*
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.yuchens.equipinspectandroid.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WifiReading(val level: Int, val rssi: Int?) // 等級 0..4，RSSI 單位 dBm

class WifiSignalView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val appCtx = context.applicationContext
    private val connectivity = appCtx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val wifiManager = appCtx.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val icons = intArrayOf(
        R.drawable.ic_wifi_0,
        R.drawable.ic_wifi_1,
        R.drawable.ic_wifi_2,
        R.drawable.ic_wifi_3,
        R.drawable.ic_wifi_4
    )

    private val _reading = MutableStateFlow(WifiReading(level = 0, rssi = null))
    val reading: StateFlow<WifiReading> = _reading.asStateFlow()

    private var pollingJob: Job? = null
    private var registeredCallback = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = refreshOnce()
        override fun onLost(network: Network) = refreshOnce()
        override fun onCapabilitiesChanged(network: Network, nc: NetworkCapabilities) = refreshOnce()
    }

    fun startListening(pollingIntervalMs: Long = 1_000L) {
        if (!registeredCallback) {
            val req = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build()
            try {
                connectivity.registerNetworkCallback(req, networkCallback)
                registeredCallback = true
            } catch (_: Exception) { /* 忽略無 callback 權限或已註冊例外 */ }
        }

        if (pollingJob?.isActive != true) {
            pollingJob = CoroutineScope(Dispatchers.Default).launch {
                while (isActive) {
                    refreshOnce()
                    delay(pollingIntervalMs)
                }
            }
        }
        refreshOnce()
    }

    fun stopListening() {
        pollingJob?.cancel()
        pollingJob = null
        if (registeredCallback) {
            try {
                connectivity.unregisterNetworkCallback(networkCallback)
            } catch (_: Exception) { /* 已移除或未註冊 */ }
            registeredCallback = false
        }
    }

    override fun onAttachedToWindow() { super.onAttachedToWindow(); startListening() }
    override fun onDetachedFromWindow() { super.onDetachedFromWindow(); stopListening() }

    private fun refreshOnce() {
        val (isWifi, info) = getActiveWifiInfo()
        val newRssi: Int?
        val newLevel: Int
        if (!isWifi || info == null) {
            newRssi = null
            newLevel = 0
        } else {
            newRssi = info.rssi
            newLevel = rssiToLevel(newRssi)
        }
        post { applyReading(newLevel, newRssi) }
    }

    private fun applyReading(level: Int, rssi: Int?) {
        // 更新 Icon
        if (_reading.value.level != level || drawable == null) {
            setImageResource(icons[level])
        }

        // 更新 Flow
        _reading.value = WifiReading(level, rssi)

        // 無障礙說明
        contentDescription = if (rssi != null) "Wi-Fi 強度 $level/4（$rssi dBm）" else "Wi-Fi 未連線"
    }

    @Suppress("DEPRECATION")
    private fun getActiveWifiInfo(): Pair<Boolean, WifiInfo?> {
        val network = connectivity.activeNetwork ?: return false to null
        val caps = connectivity.getNetworkCapabilities(network) ?: return false to null
        val isWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        if (!isWifi) return false to null

        // 取得 WifiInfo（API 29+ 優先由 NetworkCapabilities.transportInfo 取得）
        var info: WifiInfo? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            caps.transportInfo as? WifiInfo
        } else {
            null
        }
        if (info == null) info = wifiManager.connectionInfo

        // 只把 RSSI = -127 視為無效；不要用 SSID / networkId 過濾
        if (info == null || info.rssi == -127) return true to null

        return true to info
    }

    private fun rssiToLevel(rssi: Int): Int = when {
        rssi >= -50 -> 4
        rssi >= -60 -> 3
        rssi >= -70 -> 2
        rssi >= -80 -> 1
        else -> 0
    }
}
