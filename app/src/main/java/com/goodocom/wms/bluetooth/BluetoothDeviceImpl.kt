package com.goodocom.wms.bluetooth
import android.content.Context
import android.database.Cursor
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

    fun onDestroy() { db.commit() }
    fun query(number: String): String? = db.query(number)
    fun query(): Cursor? = db.query()

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

    override var talkingNumber: String
        set(_) { call?.onHfpStatus(hfpStatus)}
        get() = service.DeviceTalkingNumber(bdaddr)
    override var incomingNumber: String
        get() = service.DeviceIncomingNumber(bdaddr)
        set(_) { call?.onHfpStatus(hfpStatus)}
    override var outgoingNumber: String
        get() = service.DeviceOutgoingNumber(bdaddr)
        set(_) { call?.onHfpStatus(hfpStatus)}
    override var twcWaitNumber: String
        get() = service.DeviceTwcWaitNumber(bdaddr)
        set(_) { call?.onHfpStatus(hfpStatus)}
    override var twcHeldNumber: String
        get() = service.DeviceTwcHeldNumber(bdaddr)
        set(_) { call?.onHfpStatus(hfpStatus)}

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

    override fun TwcReleaseHeldRejectWait() = service.DeviceTwcReleaseHeldRejectWaiting(bdaddr)
    override fun TwcReleaseActiveAnswerOther() = service.DeviceTwcReleaseActiveAnswerOther(bdaddr)
    override fun TwcHoldActiveAcceptOther() = service.DeviceTwcHoldActiveAnswertOther(bdaddr)
    override fun TwcConference() = service.DeviceTwcConference(bdaddr)

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
    override fun BrowsingNowPlayingTrack(index: Int, high: Long, low: Long, full: Int) =
        service.DeviceBrowsingNowPlayingTrack(bdaddr, index, high, low, full)
    override fun BrowsingRetrieveMediaPlayers(start: Int, end: Int) = service.DeviceBrowsingRetrieveMediaPlayers(bdaddr, start, end)
    override fun BrowsingRetrieveFilesystem(start: Int, end: Int) = service.DeviceBrowsingRetrieveFilesystem(bdaddr, start, end)
    override fun BrowsingRetrieveNowPlayingList(start: Int, end: Int) = service.DeviceBrowsingRetrieveNowPlayingList(bdaddr, start, end)
    override fun BrowsingRetrieveNumberOfItem(scope: Int) = service.DeviceBrowsingRetrieveNumberOfItem(bdaddr, scope)
    override fun BrowsingPlayItem(msb: Long, lsb: Long) = service.DeviceBrowsingPlayItem(bdaddr, msb, lsb)
    override fun BrowsingChangePath(dir: Int, msb: Long, lsb: Long) = service.DeviceBrowsingChangePath(bdaddr, dir, msb, lsb)
    override fun BrowsingAddNowPlaying(msb: Long, lsb: Long) = service.DeviceBrowsingAddNowPlaying(bdaddr, msb, lsb)
    override fun BrowsingSetMediaPlayer(id: Int) = service.DeviceBrowsingSetMediaPlayer(bdaddr, id)
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

    override fun onAvrcpBrowsingChangePathComplete(status: Int, num_items: Int) {
        media?.onAvrcpBrowsingChangePathComplete(status, num_items)
    }

    override fun onAvrcpBrowsingFolder(type: Int, msb: Long, lsb: Long, display: String) {
        media?.onAvrcpBrowsingFolder(FolderItem(display, type, msb, lsb))
    }

    override fun onAvrcpBrowsingMedia(
        type: Int,
        msb: Long,
        lsb: Long,
        display: String,
        title: String,
        artist: String,
        album: String
    ) {
        media?.onAvrcpBrowsingMedia(MediaItem(display, type, msb, lsb, title, artist, album))
    }

    override fun onAvrcpBrowsingMediaPlayer(id: Int, play_status: Int, major: Int, subtype: Int, display: String) {
        media?.onAvrcpBrowsingMediaPlayer(MediaPlayerItem(display, id, play_status, major, subtype))
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

    override fun onTalkingNumber(number: String) {
       this.talkingNumber = number
    }

    override fun onIncomingNumber(number: String) {
        this.incomingNumber = number
    }

    override fun onOutgoingNumber(number: String) {
        this.outgoingNumber = number
    }

    override fun onTwcHeldNumber(number: String) {
        this.twcHeldNumber = number
    }

    override fun onTwcWaitNumber(number: String) {
        this.twcWaitNumber = number
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
        db.commit()
        book?.onPhonebookComplete(db.query())
    }

}