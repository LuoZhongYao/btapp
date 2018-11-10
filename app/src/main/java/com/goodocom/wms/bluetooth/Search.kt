package com.goodocom.wms.bluetooth

interface Search {
    fun onResult(name: String, bdaddr: String)
    fun onComplete()
}