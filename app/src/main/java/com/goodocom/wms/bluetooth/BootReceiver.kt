package com.goodocom.wms.bluetooth

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.goodocom.wms.bluetooth.service.BluetoothService

class BootReceiver: BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent?) {
        context.startService(Intent(context, BluetoothService::class.java))
    }
}