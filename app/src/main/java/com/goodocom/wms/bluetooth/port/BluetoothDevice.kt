package com.goodocom.wms.bluetooth.port
interface BluetoothDevice {
    var signal: Int
    var battchg: Int
    var micMuted: Boolean
    var pbapStatus: PbapStatus
    var audioDirection: AudioDirection
    var name: String
    var talkingNumber: String
    var incomingNumber: String
    var outgoingNumber: String
    var twcWaitNumber: String
    var twcHeldNumber: String
    var hfpStatus: HfpStatus
    var a2dpStatus: A2dpStatus
    var avrcpStatus: AvrcpStatus
    var avrcpPlaybackPos: Long
    var avrcpAttribute: List<String>

    enum class HfpStatus{
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        INCOMING,
        OUTGOING,
        TALKING,
        TWC_INCOMING,
        TWC_OUTGOING,
        TWC_HELD_ACTIVE,
        TWC_HELD_REMAINING,
        TWC_MULTIPARTY
    }

    enum class A2dpStatus { DISCONNECTED, CONNECTED }
    enum class AvrcpStatus { STOP, PLAY, PAUSE,}
    enum class AudioDirection{ BLUETOOTH, PHONE}
    enum class PbapStatus { UNSUPPORTED, SUPPORT}

    fun TwcReleaseHeldRejectWait()
    fun TwcReleaseActiveAnswerOther()
    fun TwcHoldActiveAcceptOther()
    fun TwcConference()
    fun DTMF(dtmf: String)
    fun Dial(number: String)
    fun AudioToggle()
    fun Answer()
    fun Hangup()
    fun Redial()
    fun Forward()
    fun Backward()
    fun Pause()
    fun Play()
    fun Stop()
    fun MicToggle()
    fun VoiceToggle()

    fun Disconnect()
    fun SyncPhonebook()
    fun SyncHistory()
    fun CancelSync()
    fun AudioSource()

    fun BrowsingNowPlayingTrack(index: Int, high: Long, low: Long, full: Int)
    fun BrowsingRetrieveMediaPlayers(start: Int, end: Int)
    fun BrowsingRetrieveFilesystem(start: Int, end: Int)
    fun BrowsingRetrieveNowPlayingList(start: Int, end: Int)
    fun BrowsingRetrieveNumberOfItem(scope: Int)
    fun BrowsingPlayItem(msb: Long, lsb: Long)
    fun BrowsingChangePath(dir: Int, msb: Long, lsb: Long)
    fun BrowsingAddNowPlaying(msb: Long, lsb: Long)
    fun BrowsingSetMediaPlayer(id: Int)
}