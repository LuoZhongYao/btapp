package com.goodocom.wms.bluetooth

import com.goodocom.wms.bluetooth.port.BluetoothDevice

interface Media {

    fun onA2dpStatus(status: BluetoothDevice.A2dpStatus)
    fun onAvrcpStatus(status: BluetoothDevice.AvrcpStatus)
    fun onAvrcpPlaybackPos(pos: Long)
    fun onAvrcpAttribute(attr: List<String>)
    fun onAvrcpBrowsingChangePathComplete(status: Int, num_items: Int)
    fun onAvrcpBrowsingFolder(folder: FolderItem)
    fun onAvrcpBrowsingMedia(media: MediaItem)
}


open class AvrcpBrowsingItem(val display: String, val type: Int, val msb: Long, val lsb: Long, val img: Int) {

}

class MediaItem(display: String, type: Int, val title: String,
                val artist: String, val album: String, msb: Long, lsb:Long)
    : AvrcpBrowsingItem(display, type, msb, lsb, R.drawable.ic_music_24dp) {

}

class FolderItem(display: String, type: Int, msb: Long, lsb: Long)
    : AvrcpBrowsingItem(display, type, msb, lsb, R.drawable.ic_folder) {

}