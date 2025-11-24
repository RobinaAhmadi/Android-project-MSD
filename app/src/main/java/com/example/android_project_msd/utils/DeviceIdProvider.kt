package com.example.android_project_msd.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import java.util.UUID

class DeviceIdProvider(private val context: Context) {
    @SuppressLint("HardwareIds")
    fun getDeviceId(): String {
        val hardwareId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        return hardwareId ?: UUID.randomUUID().toString()
    }
}
