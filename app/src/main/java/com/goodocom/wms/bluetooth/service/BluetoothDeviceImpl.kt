package com.goodocom.wms.bluetooth.service

import android.os.RemoteCallbackList
import android.os.RemoteException
import com.goodocom.wms.bluetooth.IBluetoothDeviceCallback
import com.goodocom.wms.bluetooth.port.BluetoothDevice
import com.goodocom.wms.bluetooth.utils.False
import com.goodocom.wms.bluetooth.utils.True


class BluetoothDeviceImpl(val bdaddr: String,
                          val mgmt: BluetoothService.Mgmt,
                          val io: BluetoothService.IOService): BluetoothDevice {

    private val callback = RemoteCallbackList<IBluetoothDeviceCallback>()
    override  var signal = 0
        set(v) { field = v; notify { it.onSignal(v)} }
    fun signal(sig: String) { signal = sig.toInt() }
    override  var battchg = 0
        set(v) { field = v; notify{ it.onBattchg(v)} }
    fun battchg(batt: String) { battchg = batt.toInt() }
    override var micMuted: Boolean = false
        set(v) { field = v; notify{ it.onMicMuted(v)} }
    override var pbapStatus = BluetoothDevice.PbapStatus.UNSUPPORTED
        set(v) { field = v; notify{ it.onPbapStatus(v.name)} }
    fun pbapStatus(status: String) { pbapStatus = BluetoothDevice.PbapStatus.values()[status.toInt()] }
    override var audioDirection =  BluetoothDevice.AudioDirection.BLUETOOTH
        set(v) { field = v; notify{ it.onAudioDirection(v.name)} }
    override var name: String = "Bluetooth"
        set(v) { field = v; notify{ it.onName(v)}}
    override var talkingNumber: String = ""
        set(v) { field = v; notify{ it.onTalkingNumber(v) }}

    override var incomingNumber: String = ""
        set(v) { field = v; notify { it.onIncomingNumber(v) }}

    override var outgoingNumber: String = ""
        set(v) { field = v; notify { it.onOutgoingNumber(v) }}

    override var twcHeldNumber: String = ""
        set(v) { field = v; notify { it.onTwcHeldNumber(v) }}

    override var twcWaitNumber: String = ""
        set(v) { field = v; notify { it.onTwcWaitNumber(v) }}

    var connected: Int = 0
        set(v) {
            (field != 0 && v == 0).True { mgmt.dmRemove(bdaddr) }
            (field == 0 && v != 0).True { mgmt.dmAdd(bdaddr) }
            field = v
        }

    init {
        (bdaddr == BluetoothService.INVALID).False {
            exec("CY")
        }
    }

    private fun connected(cond: Boolean, bit: Int) {
        connected = if(cond) connected or bit else connected and bit.inv()
    }
    override var hfpStatus = BluetoothDevice.HfpStatus.DISCONNECTED
        set(v) {
            notify{ it.onHfpStatus(v.name) }
            connected(v >= BluetoothDevice.HfpStatus.CONNECTED, HFP)
            (v > BluetoothDevice.HfpStatus.CONNECTED).True {  mgmt.startUI() }
            (v >= BluetoothDevice.HfpStatus.CONNECTED && field < BluetoothDevice.HfpStatus.CONNECTED).True {
                exec("QB")
            }
            field = v
        }

    fun hfpStatus(status: String) {
        hfpStatus = BluetoothDevice.HfpStatus.values()[status.toInt() - 1]
    }

    override var a2dpStatus = BluetoothDevice.A2dpStatus.DISCONNECTED
        set(v) {
            field = v
            notify { it.onA2dpStatus(v.name) }
            connected(v == BluetoothDevice.A2dpStatus.CONNECTED, A2DP)
        }

    fun a2dpStatus(status: String) {
        a2dpStatus = BluetoothDevice.A2dpStatus.values()[status.toInt() - 1]
    }

    override var avrcpStatus = BluetoothDevice.AvrcpStatus.STOP
        set(v) {
            field = v; notify{ it.onAvrcpStatus(v.name) }
        }
    fun avrcpStatus(status: String) {
        avrcpStatus = BluetoothDevice.AvrcpStatus.values()[status.toInt()]
    }

    override var avrcpPlaybackPos: Long = 0L
        set(v) {
            field = v; notify{ it.onAvrcpPlaybackPos(v) }
        }

    override var avrcpAttribute: List<String> = listOf("", "", "", "", "")
        set(v) {
            field = v; notify { it.onAvrcpAttribute(v) }
        }

    fun phonebookItem(item: List<String>) = notify { it.onPhonebookItem(item[0], item[1]) }
    fun historyItem(type: String, item: List<String>) = notify { it.onHistoryItem(type, item[0], item[1], item[2]) }
    fun phonebookComplete() = notify { it.onPhonebookComplete()  }
    fun historyComplete() = notify { it.onHistoryComplete()  }
    fun browsingChangePathComplete(status: String, num_items: String) {
        notify { it.onAvrcpBrowsingChangePathComplete(status.toInt(), num_items.toInt()) }
    }

    fun browsingFolder(type: String, msb: String, lsb: String, display: String) {
        notify { it.onAvrcpBrowsingFolder(type.toInt(), msb.toLong(), lsb.toLong(), display) }
    }

    fun browsingMedia(type: String, msb: String, lsb: String, display: String, title: String, artist: String, album: String) {
        notify { it.onAvrcpBrowsingMedia(type.toInt(), msb.toLong(), lsb.toLong(), display, title, artist, album) }
    }

    fun browsingMediaPlayer(id: String, play_status: String, major: String, subtype: String, display: String) {
        notify { it.onAvrcpBrowsingMediaPlayer(id.toInt(), play_status.toInt(), major.toInt(), subtype.toInt(), display) }
    }

    fun register(cbk: IBluetoothDeviceCallback) = callback.register(cbk)
    fun unregister(cbk: IBluetoothDeviceCallback) = callback.unregister(cbk)

    private fun exec(cmd: String) = io.write(bdaddr, cmd)
    private fun exec(cond: Boolean, cmd: String) = cond.True { exec(cmd) }

    override fun DTMF(dtmf: String) = exec(hfpStatus == BluetoothDevice.HfpStatus.TALKING, "CX$dtmf")
    override fun Dial(number: String) = exec(hfpStatus >= BluetoothDevice.HfpStatus.CONNECTED, "CW$number")
    override fun Answer() = exec(hfpStatus == BluetoothDevice.HfpStatus.INCOMING, "CE")
    override fun Hangup() = exec(hfpStatus > BluetoothDevice.HfpStatus.CONNECTED, "CG")
    override fun Redial() = exec(hfpStatus >= BluetoothDevice.HfpStatus.CONNECTED, "CH")
    override fun AudioToggle() = exec(hfpStatus >= BluetoothDevice.HfpStatus.CONNECTED, "CO")
    override fun Forward() = exec("MD")
    override fun Backward() = exec("ME")
    override fun Pause() = exec("MB")
    override fun Play() = exec("MA")
    override fun Stop() = exec("MC")
    override fun MicToggle() = exec("CM")
    override fun VoiceToggle() = exec("CO")
    override fun Disconnect() = exec("CD")
    override fun SyncPhonebook() = exec("PB")
    override fun SyncHistory() = exec("PN")
    override fun CancelSync() = exec("PS")
    override fun AudioSource() = exec("AS")
    override fun TwcConference() = exec("CT")
    override fun TwcHoldActiveAcceptOther() = exec("CS")
    override fun TwcReleaseActiveAnswerOther() = exec("CR")
    override fun TwcReleaseHeldRejectWait() = exec("CQ")
    override fun BrowsingNowPlayingTrack(index: Int, high: Long, low: Long, full: Int) = exec("Me$index,$high,$low,$full")
    override fun BrowsingRetrieveMediaPlayers(start: Int, end: Int) = exec("Mm$start,$end")
    override fun BrowsingRetrieveFilesystem(start: Int, end: Int) = exec("Mf$start,$end")
    override fun BrowsingRetrieveNowPlayingList(start: Int, end: Int) = exec("Ml$start,$end")
    override fun BrowsingRetrieveNumberOfItem(scope: Int) = exec("Mn$scope")
    override fun BrowsingPlayItem(msb: Long, lsb: Long) = exec("Mp$msb,$lsb")
    override fun BrowsingChangePath(dir: Int, msb: Long, lsb: Long) = exec("Mc$dir,$msb,$lsb")
    override fun BrowsingAddNowPlaying(msb: Long, lsb: Long) = exec("Ma$msb,$lsb")
    override fun BrowsingSetMediaPlayer(id: Int) = exec("Ms$id")

    private fun notify(cbk: (c: IBluetoothDeviceCallback) -> Unit) {
        for( i in 0 until callback.beginBroadcast()) {
            try {
                cbk(callback.getBroadcastItem(i))
            } catch (e: RemoteException) {
                // The RemoteCallbackList will take care of removing
                // the dead object for us.
            }
        }
        callback.finishBroadcast()
    }

    companion object {
        private const val HFP = 0x01
        private const val A2DP = 0x02
        private const val AVRCP = 0x04
        private const val PBAP = 0x08
    }
}