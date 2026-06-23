package com.yuchens.equipinspectandroid.util

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import java.net.NetworkInterface

object DeviceIdProvider {

    fun getPreferredIdentifier(context: Context): String {
        return getAndroidId(context)
    }

    @SuppressLint("HardwareIds")
    private fun getAndroidId(context: Context): String =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: ""
}
