package com.goodocom.wms.bluetooth

import com.goodocom.wms.bluetooth.port.BluetoothDevice

interface Media {

    fun onA2dpStatus(status: BluetoothDevice.A2dpStatus)
    fun onAvrcpStatus(status: BluetoothDevice.AvrcpStatus)
    fun onAvrcpPlaybackPos(pos: Long)
    fun onAvrcpAttribute(attr: List<String>)
}