package com.goodocom.wms.bluetooth

import com.goodocom.wms.bluetooth.port.BluetoothDevice

interface Call {

    fun onHfpStatus(status: BluetoothDevice.HfpStatus)
    fun onAudio(dir: BluetoothDevice.AudioDirection)
    fun onSignal(sig: Int)
    fun onBattchg(chg: Int)
    fun onNumber(number: String)
    fun onMicMuted(muted: Boolean)
}