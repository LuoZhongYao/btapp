package com.goodocom.wms.bluetooth

import android.database.Cursor
import com.goodocom.wms.bluetooth.port.BluetoothDevice

interface Phonebook {

    fun onPbapStatus(status: BluetoothDevice.PbapStatus)
    fun onPhonebookComplete(cursor: Cursor?)
}