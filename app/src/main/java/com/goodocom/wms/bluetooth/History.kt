package com.goodocom.wms.bluetooth

import com.goodocom.wms.bluetooth.port.BluetoothDevice

interface History {
    fun onHfpStatus(status: BluetoothDevice.HfpStatus)
    fun onHistoryItem(type: String, name: String, number: String, date: String)
    fun onHistoryComplete()
}