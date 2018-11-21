package com.goodocom.wms.bluetooth.port
interface BluetoothDevice {
    var signal: Int
    var battchg: Int
    var micMuted: Boolean
    var pbapStatus: PbapStatus
    var audioDirection: AudioDirection
    var name: String
    var number: String
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
}