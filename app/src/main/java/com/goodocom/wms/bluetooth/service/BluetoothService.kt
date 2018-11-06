package com.goodocom.wms.bluetooth.service

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import com.goodocom.wms.bluetooth.IBluetoothCallback
import com.goodocom.wms.bluetooth.IBluetoothDeviceCallback
import com.goodocom.wms.bluetooth.IBluetoothService
import com.goodocom.wms.bluetooth.utils.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class BluetoothService : Service() {
    companion object {
        private val MSG_START = 0
        private val MSG_DATA  = 1
        private val MSG_STOP = 2
        private val MSG_INIT = 3
        private const val TAG = "btapp"
        const val INVALID = "000000000000"
    }

    private val handler = IndHandler()
    private var io = IOService()
    private val bluetoothInd = BluetoothInd(this)
    private val callback = RemoteCallbackList<IBluetoothCallback>()
    private val service = BluetoothServiceImpl()
    var mgmt = Mgmt()

    inner class Mgmt {
        val device: HashMap<String, BluetoothDeviceImpl> = HashMap()

        init {
            selected(INVALID)
            notify { it.onInquiryComplete() }
        }

        var autoconnect: Boolean = false
            set(value) {
                field = value; notify({ c, auto -> c.onAutoConnect(auto ) }, value)
            }
        fun autoconnect(auto: String) {autoconnect = auto == "1" }
        var autoanswer: Boolean = false
            set(value) {
                field = value; notify({ c, auto -> c.onAutoAnswer(auto) }, value)
            }
        fun autoanswer(auto: String) {autoanswer = auto == "1" }
        var poweron: Boolean = true
            set(value) {
                field = value; notify({ c, on -> c.onPoweron(on) }, value)
            }
        var name: String = "INVALID"
            set(value) {
                field = value; notify({ c, name -> c.onLocalName(name) }, value)
            }
        var pin: String = "INVALID"
            set(value) {
                field = value; notify({ c, pin -> c.onPin(pin) }, value)
            }
        var bdaddr: String = INVALID
            set(value) {
                field = value; notify({ c, bdaddr -> c.onLocalBdaddr(bdaddr) }, value)
            }
        var pairMode: Boolean = true
            set(value) {
                field = value; notify({ c, enable -> c.onPairMode(enable) }, value)
            }
        var version: String = "INVALID"
            set(value) {
                field = value; notify({ c, ver -> c.onVersion(ver) }, value)
            }

        var selected = device[INVALID]!!

        fun selected(bdaddr: String) {
            (bdaddr == INVALID).True { device.clear() }
            ((device[bdaddr] == null)).True {dmAdd(bdaddr)}
            device[bdaddr]?.let {selected = it}
        }

        fun dmAdd(bdaddr: String) {
            device[bdaddr] = BluetoothDeviceImpl(bdaddr, this, io)
            notify({ c, s -> c.onDMAdd(s) }, bdaddr)
        }

        fun dmRemove(bdaddr: String) {
            notify({ c, s -> c.onDMRemove(s) }, bdaddr)
            device.remove(bdaddr)
        }
    }

    private fun <T>notify(cbk: (c: IBluetoothCallback, arg: T) -> Unit, arg: T) {
        for( i in 0 until  callback.beginBroadcast()) {
            try {
                cbk(callback.getBroadcastItem(i), arg)
            } catch (e: RemoteException) {
                // The RemoteCallbackList will take care of removing
                // the dead object for us.
            }
        }
        callback.finishBroadcast()
    }

    private fun notify(cbk: (c: IBluetoothCallback) -> Unit) {
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

    private fun reset() {
        mgmt = Mgmt()
        io.requestVersion()
        io.requestLocalPin()
        io.requestLocalAddress()
        io.requestLocalName()
        io.requestAutoConnectAnswer()
        io.requestA2dpStatus()
        io.requestHfpStatus()
    }

    fun pairlist(index: Int, bdaddr: String, name: String) {
        notify ({ c, a -> c.onPairlist(a[0] as Int, a[1] as String, a[2] as String)}, arrayOf(index, bdaddr, name))
    }

    fun initComplete() = reset()
    fun inquiryComplete() =  notify { it.onInquiryComplete() }

    fun inquiryResult(bdaddr: String, name: String) {
        notify({ c, a -> c.onInquiryResult(a[0], a[1] ) }, arrayOf(bdaddr, name))
    }

    override fun onCreate() {
        io.start()
        Log.i(TAG, "BluetoothService onCreate")
        super.onCreate()
    }

    override fun onDestroy() {
        Log.i(TAG, "BluetoothService onDestroy")
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "BluetoothService onStartCommand")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder {
        return service
    }

    private inner class IndHandler: Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when(msg.what) {
                MSG_START -> {io = IOService(); io.start()}
                MSG_STOP -> {io.running = false}
                MSG_DATA -> {bluetoothInd.onBytes(msg.obj as ByteArray)}
                MSG_INIT -> {reset()}
            }
        }
    }

    inner class IOService: Thread() {
        var output: FileOutputStream? = null
        var running = true
        override fun run() {
            var input: FileInputStream? = null
            val buffer = ByteArray(1024)
            try {
                input = FileInputStream(File("/dev/goc_serial"))
                output = FileOutputStream(File("/dev/goc_serial"))
                handler.sendEmptyMessage(MSG_INIT)
                while (running) {
                    val n = input.read(buffer)
                    if (0 > n)
                        throw IOException()
                    handler.sendMessage(handler.obtainMessage(MSG_DATA, buffer.sliceArray(0 until n)))
                }
            } catch (e: IOException) {
                handler.sendEmptyMessageDelayed(MSG_START, 2000)
            } finally {
                input?.close()
                output?.close()
            }
        }

        fun write(cmd: String) {
            output?.write("AT#%s\r\n".format(cmd).toByteArray())
        }

        fun write(bdaddr: String, cmd: String) {
            output?.write("AT#A0$bdaddr\r\nAT#%s\r\n".format(cmd).toByteArray())
        }

        fun requestLocalName() = write("MM")
        fun requestLocalName(name: String) = write("MM$name")
        fun requestLocalPin() = write("MN")
        fun requestLocalPin(pin: String) = write("MN$pin")
        fun requestVersion() = write("MY")
        fun requestPairList() = write("MX")
        fun requestLocalAddress() = write("DB")
        fun requestAutoConnectAnswer() = write("MF")
        fun requestA2dpStatus() = write("")
        fun requestHfpStatus() = write("CY")
        fun requestSearch() = write("SD")
        fun requestCancelSearch() = write("ST")
        fun requestEnableAutoConnect() = write("MG")
        fun requestDisableAutoConnect() = write("MH")
        fun requestEnableAutoAnswer() = write("MP")
        fun requestDisableAutoAnswer() = write("MQ")
        fun requestConnect(bdaddr: String) = write("CC$bdaddr")
    }

    inner class BluetoothServiceImpl: IBluetoothService.Stub() {

        override fun devRegister(bdaddr: String, cbk: IBluetoothDeviceCallback) { mgmt.device[bdaddr]?.register(cbk) }
        override fun devUnregister(bdaddr: String, cbk: IBluetoothDeviceCallback) { mgmt.device[bdaddr]?.unregister(cbk) }
        override fun register(cbk: IBluetoothCallback) {
            callback.register(cbk)
            mgmt.device.forEach { (it.key == INVALID).False{ cbk.onDMAdd(it.value.bdaddr) } }
        }
        override fun unregister(cbk: IBluetoothCallback) { callback.unregister(cbk) }

        override fun Search() = io.requestSearch()
        override fun CancelSearch() = io.requestCancelSearch()
        override fun Connect(bdaddr: String) = io.requestConnect(bdaddr)


        override fun LocalName(): String = mgmt.name
        override fun SetLocalName(name: String) = io.requestLocalName(name)
        override fun PinCode(): String = mgmt.pin
        override fun SetPinCode(pin: String) = io.requestLocalPin(pin)
        override fun LocalAddress(): String = mgmt.bdaddr
        override fun Version(): String = mgmt.version
        override fun ReadPairList() = io.requestPairList()
        override fun IsAutoConnecte() = mgmt.autoconnect
        override fun IsAutoAnswer() = mgmt.autoanswer
        override fun EnableAutoConnect() = io.requestEnableAutoConnect()
        override fun DisableAutoConnect() = io.requestDisableAutoConnect()
        override fun EnableAutoAnswer() = io.requestEnableAutoAnswer()
        override fun DisableAutoAnswer() = io.requestDisableAutoAnswer()


        override fun DeviceNumber(bdaddr: String): String = mgmt.device[bdaddr]!!.number
        override fun DeviceName(bdaddr: String): String = mgmt.device[bdaddr]!!.name
        override fun DeviceSignal(bdaddr: String): Int = mgmt.device[bdaddr]!!.signal
        override fun DeviceBattchg(bdaddr: String): Int = mgmt.device[bdaddr]!!.battchg
        override fun DeviceMicMuted(bdaddr: String): Boolean = mgmt.device[bdaddr]!!.micMuted
        override fun DevicePbapStatus(bdaddr: String): String = mgmt.device[bdaddr]!!.pbapStatus.name
        override fun DeviceAudioDirection(bdaddr: String): String = mgmt.device[bdaddr]!!.audioDirection.name
        override fun DeviceHfpStatus(bdaddr: String): String = mgmt.device[bdaddr]!!.hfpStatus.name
        override fun DeviceA2dpStatus(bdaddr: String): String = mgmt.device[bdaddr]!!.a2dpStatus.name
        override fun DeviceAvrcpStatus(bdaddr: String): String = mgmt.device[bdaddr]!!.avrcpStatus.name
        override fun DeviceAvrcpPlaybackPos(bdaddr: String): Long = mgmt.device[bdaddr]!!.avrcpPlaybackPos

        override fun DeviceDTMF(bdaddr: String, dtfm: String) = mgmt.device[bdaddr]!!.DTMF(dtfm)
        override fun DeviceDial(bdaddr: String, number: String) = mgmt.device[bdaddr]!!.Dial(number)
        override fun DeviceAudioToggle(bdaddr: String) = mgmt.device[bdaddr]!!.AudioToggle()
        override fun DeviceHangup(bdaddr: String) = mgmt.device[bdaddr]!!.Hangup()
        override fun DeviceAnswer(bdaddr: String) = mgmt.device[bdaddr]!!.Answer()
        override fun DeviceRedial(bdaddr: String) = mgmt.device[bdaddr]!!.Redial()
        override fun DeviceForward(bdaddr: String) = mgmt.device[bdaddr]!!.Forward()
        override fun DeviceBackward(bdaddr: String) = mgmt.device[bdaddr]!!.Backward()
        override fun DevicePause(bdaddr: String) = mgmt.device[bdaddr]!!.Pause()
        override fun DevicePlay(bdaddr: String) = mgmt.device[bdaddr]!!.Play()
        override fun DeviceStop(bdaddr: String) = mgmt.device[bdaddr]!!.Stop()
        override fun DeviceMicToggle(bdaddr: String) = mgmt.device[bdaddr]!!.MicToggle()
        override fun DeviceVoiceToggle(bdaddr: String) = mgmt.device[bdaddr]!!.VoiceToggle()
        override fun DeviceDisconnect(bdaddr: String) = mgmt.device[bdaddr]!!.Disconnect()
        override fun DeviceSyncHistory(bdaddr: String) = mgmt.device[bdaddr]!!.SyncHistory()
        override fun DeviceSyncPhonebook(bdaddr: String) = mgmt.device[bdaddr]!!.SyncPhonebook()
    }
}
