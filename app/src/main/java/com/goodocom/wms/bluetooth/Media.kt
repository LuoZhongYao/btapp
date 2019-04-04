package com.goodocom.wms.bluetooth

import com.goodocom.wms.bluetooth.port.BluetoothDevice

interface Media {

    fun onA2dpStatus(status: BluetoothDevice.A2dpStatus)
    fun onAvrcpStatus(status: BluetoothDevice.AvrcpStatus)
    fun onAvrcpPlaybackPos(pos: Long)
    fun onAvrcpAttribute(attr: List<String>)
    fun onAvrcpBrowsingChangePathComplete(status: Int, num_items: Int)
    fun onAvrcpBrowsingMediaPlayer(player: MediaPlayerItem)
    fun onAvrcpBrowsingFolder(folder: FolderItem)
    fun onAvrcpBrowsingMedia(media: MediaItem)
}

interface AvrcpBrowsingUid {
    val msb: Long
    val lsb: Long
}
open class AvrcpBrowsingItem(val display: String, val img: Int) {}

class MediaPlayerItem(display: String, val id: Int, val play_statu: Int, val major: Int, val subtype: Int)
    : AvrcpBrowsingItem(display, R.drawable.ic_mediaplayer) {

}

class MediaItem(display: String, val type: Int, override val msb:Long, override val lsb: Long, val title: String,
                val artist: String, val album: String)
    : AvrcpBrowsingItem(display, R.drawable.ic_music_24dp), AvrcpBrowsingUid {
}

class FolderItem(display: String, val type: Int, override val msb: Long, override val lsb: Long)
    : AvrcpBrowsingItem(display, R.drawable.ic_folder), AvrcpBrowsingUid {

}