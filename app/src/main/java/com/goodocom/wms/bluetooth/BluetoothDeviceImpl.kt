package com.goodocom.wms.bluetooth
import android.content.Context
import com.goodocom.wms.bluetooth.port.BluetoothDevice
import com.goodocom.wms.bluetooth.service.BluetoothService
import com.goodocom.wms.bluetooth.utils.True

class BluetoothDeviceImpl(
    val context: Context,
    val bdaddr: String,
    val service: BluetoothService.BluetoothServiceImpl,
    val rename: () -> Unit): BluetoothDevice, IBluetoothDeviceCallback.Stub() {

    var media: Media? = null
    var call: Call? = null
    var book: Phonebook? = null
    var history: History? = null
    var profile: Profile? = null
    private var db = Database(context, "Contact$bdaddr")

    override var signal: Int
        set(v) { call?.onSignal(v) }
        get() = service.DeviceSignal(bdaddr)

    override var battchg: Int
        set(v) { call?.onBattchg(v)}
        get() = service.DeviceBattchg(bdaddr)

    override var micMuted: Boolean
        set(v) { call?.onMicMuted(v)}
        get() = service.DeviceMicMuted(bdaddr)

    override var audioDirection: BluetoothDevice.AudioDirection
        set(v) { call?.onAudio(v)}
        get() = BluetoothDevice.AudioDirection.valueOf(service.DeviceAudioDirection(bdaddr))

    override var name: String
        set(_) { rename() }
        get() = service.DeviceName(bdaddr)

    override var number: String
        set(v) { call?.onNumber(v)}
        get() = service.DeviceNumber(bdaddr)

    override var hfpStatus: BluetoothDevice.HfpStatus =BluetoothDevice.HfpStatus.DISCONNECTED
        get() = BluetoothDevice.HfpStatus.valueOf(service.DeviceHfpStatus(bdaddr))
        set(v) {
            call?.onHfpStatus(v)
            history?.onHfpStatus(v)
            (v > BluetoothDevice.HfpStatus.CONNECTED).True { profile?.active()}
            (v == BluetoothDevice.HfpStatus.CONNECTED
                    && field > BluetoothDevice.HfpStatus.CONNECTED).True {
                profile?.inactive()
            }
            field = v
        }

    override var pbapStatus: BluetoothDevice.PbapStatus
        set(v) { book?.onPbapStatus(v) }
        get() = BluetoothDevice.PbapStatus.valueOf(service.DevicePbapStatus(bdaddr))

    override var avrcpStatus: BluetoothDevice.AvrcpStatus
        set(v) { media?.onAvrcpStatus(v)}
        get() = BluetoothDevice.AvrcpStatus.valueOf(service.DeviceAvrcpStatus(bdaddr))

    override var a2dpStatus: BluetoothDevice.A2dpStatus
        set(v) { media?.onA2dpStatus(v) }
        get() = BluetoothDevice.A2dpStatus.valueOf(service.DeviceA2dpStatus(bdaddr))

    override var avrcpPlaybackPos
        set(v) { media?.onAvrcpPlaybackPos(v) }
        get() = service.DeviceAvrcpPlaybackPos(bdaddr)

    override var avrcpAttribute: List<String>
        get() = service.DeviceAvrcpAttribute(bdaddr)
        set(value) { media?.onAvrcpAttribute(value)}

    val tabItem: MainActivity.TabItem = object: MainActivity.TabItem(R.drawable.ic_smartphone, ProfileFragment::class.java, name, this) {
        override fun getTitle(): String = name
    }

    override fun DTMF(dtmf: String) = service.DeviceDTMF(bdaddr, dtmf)
    override fun Dial(number: String) = service.DeviceDial(bdaddr, number)
    override fun Hangup() = service.DeviceHangup(bdaddr)
    override fun AudioToggle() = service.DeviceAudioToggle(bdaddr)
    override fun Redial() = service.DeviceRedial(bdaddr)
    override fun Forward() = service.DeviceForward(bdaddr)
    override fun Backward() = service.DeviceBackward(bdaddr)
    override fun Pause() = service.DevicePause(bdaddr)
    override fun Play() = service.DevicePlay(bdaddr)
    override fun Stop() = service.DeviceStop(bdaddr)
    override fun Answer() = service.DeviceAnswer(bdaddr)
    override fun MicToggle() = service.DeviceMicToggle(bdaddr)
    override fun VoiceToggle() = service.DeviceVoiceToggle(bdaddr)
    override fun Disconnect() = service.DeviceDisconnect(bdaddr)
    override fun SyncHistory() = service.DeviceSyncHistory(bdaddr)
    override fun CancelSync() = service.DeviceCancelSync(bdaddr)
    override fun AudioSource() = service.DeviceAudioSource(bdaddr)
    override fun SyncPhonebook() {
        db.clear()
        service.DeviceSyncPhonebook(bdaddr)
    }

    override fun onAvrcpAttribute(attr: List<String>) {
        avrcpAttribute = attr
    }

    override fun onA2dpStatus(status: String) {
        a2dpStatus = BluetoothDevice.A2dpStatus.valueOf(status)
    }

    override fun onAudioDirection(dir: String) {
        audioDirection = BluetoothDevice.AudioDirection.valueOf(dir)
    }

    override fun onAvrcpPlaybackPos(pos: Long) {
        avrcpPlaybackPos = pos
    }

    override fun onName(name: String) {
        this.name = name
    }

    override fun onSignal(sig: Int) {
        signal = sig
    }

    override fun onBattchg(chg: Int) {
        battchg = chg
    }

    override fun onPbapStatus(status: String) {
        pbapStatus = BluetoothDevice.PbapStatus.valueOf(status)
    }

    override fun onHfpStatus(status: String) {
        hfpStatus = BluetoothDevice.HfpStatus.valueOf(status)
    }

    override fun onAvrcpStatus(status: String) {
        avrcpStatus = BluetoothDevice.AvrcpStatus.valueOf(status)
    }

    override fun onMicMuted(mute: Boolean) {
        micMuted = mute
    }

    override fun onNumber(number: String) {
       this.number = number
    }

    override fun onHistoryItem(type: String, name: String, number: String, date: String) {
        history?.onHistoryItem(type, name, number, date)
    }

    override fun onPhonebookItem(name: String, number: String) {
        db.insert(name, number)
    }

    override fun onHistoryComplete() {
        history?.onHistoryComplete()
    }

    override fun onPhonebookComplete() {
        book?.onPhonebookComplete(db.query())
    }

}