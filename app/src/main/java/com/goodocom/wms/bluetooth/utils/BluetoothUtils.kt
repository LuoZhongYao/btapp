package com.goodocom.wms.bluetooth.utils

class BluetoothUtils {}

fun Boolean.True(body: () -> Unit) { if(this) body()}
fun Boolean.False(body: () -> Unit) { if(!this) body() }