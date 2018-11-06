package com.goodocom.wms.bluetooth

interface LocalSettings {
    fun onName(name: String)
    fun onPin(pin: String)
    fun onLocalBdaddr(bdaddr: String)
    fun onVersion(version: String)
    fun onPoweron(on: Boolean)
    fun onAutoConnect(auto: Boolean)
    fun onAutoAnswer(auto: Boolean)
}
