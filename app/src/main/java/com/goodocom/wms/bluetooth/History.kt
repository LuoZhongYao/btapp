package com.goodocom.wms.bluetooth

interface History {
    fun onHistoryItem(type: String, name: String, number: String, date: String)
    fun onHistoryComplete()
}