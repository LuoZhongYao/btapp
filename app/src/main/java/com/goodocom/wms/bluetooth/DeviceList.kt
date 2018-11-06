package com.goodocom.wms.bluetooth
interface DeviceList {
    fun onPairDevice(index: Int, bdaddr: String, name: String)
    fun onConnectedDevice(dev: Array<BluetoothDeviceImpl>)
}