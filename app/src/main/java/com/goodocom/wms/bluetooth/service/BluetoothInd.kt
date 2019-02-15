package com.goodocom.wms.bluetooth.service

import android.util.Log
import com.goodocom.wms.bluetooth.port.BluetoothDevice.*

class BluetoothInd(val ser: BluetoothService) {
    companion object {
        private const val TAG = "btapp"
    }

    private val ind: HashMap<String, (arg: String) -> Unit> = HashMap()
    private val buff = ByteArray(1024)
    private var count = 0

    init {
        ind["A0"] = { ser.mgmt.selected(it) }
        ind["P0"] = { ser.mgmt.poweron = true }
        ind["P1"] = { ser.mgmt.poweron = false }
        ind["IA"] = { ser.mgmt.selected.hfpStatus = HfpStatus.DISCONNECTED }
        ind["IB"] = { ser.mgmt.selected.hfpStatus = HfpStatus.CONNECTED }
        ind["IV"] = { ser.mgmt.selected.hfpStatus = HfpStatus.CONNECTING }
        ind["Id"] = { ser.mgmt.selected.hfpStatus = HfpStatus.INCOMING }
        ind["IG"] = { ser.mgmt.selected.hfpStatus = HfpStatus.TALKING }
        ind["IF"] = { ser.mgmt.selected.hfpStatus = HfpStatus.CONNECTED }
        ind["IL"] = { }
        ind["Iw"] = { ser.mgmt.selected.hfpStatus = HfpStatus.TWC_INCOMING }
        ind["Io"] = { ser.mgmt.selected.hfpStatus = HfpStatus.TWC_OUTGOING }
        ind["IM"] = { ser.mgmt.selected.hfpStatus = HfpStatus.TWC_MULTIPARTY }
        ind["IN"] = { ser.mgmt.selected.hfpStatus = HfpStatus.TWC_HELD_REMAINING }
        ind["IK"] = { ser.mgmt.selected.hfpStatus = HfpStatus.TWC_HELD_ACTIVE }
        ind["ID"] = { ser.mgmt.selected.incomingNumber = it; ser.mgmt.selected.hfpStatus = HfpStatus.INCOMING }
        ind["IC"] = { ser.mgmt.selected.outgoingNumber = it; ser.mgmt.selected.hfpStatus = HfpStatus.OUTGOING }
        ind["IR"] = { ser.mgmt.selected.talkingNumber = it; }
        ind["IE"] = { ser.mgmt.selected.twcWaitNumber = it; }
        ind["IH"] = { ser.mgmt.selected.twcHeldNumber = it; }
        ind["IO"] = { ser.mgmt.selected.micMuted = it.toInt() == 1 }
        ind["MC"] = { ser.mgmt.selected.audioDirection = AudioDirection.BLUETOOTH }
        ind["MD"] = { ser.mgmt.selected.audioDirection = AudioDirection.PHONE }
        ind["MG"] = { ser.mgmt.selected.hfpStatus(it) }
        ind["MA"] = { ser.mgmt.selected.avrcpStatus = AvrcpStatus.PAUSE }
        ind["MB"] = { ser.mgmt.selected.avrcpStatus = AvrcpStatus.PLAY }
        ind["MS"] = { ser.mgmt.selected.avrcpStatus = AvrcpStatus.STOP }
        ind["ML"] = {}
        ind["MP"] = { ser.mgmt.selected.avrcpPlaybackPos = it.toLong() }
        ind["MU"] = { ser.mgmt.selected.a2dpStatus(it) }
        ind["PA"] = { ser.mgmt.selected.pbapStatus(it) }
        ind["PB"] = { ser.mgmt.selected.phonebookItem(it.split("\b")) }
        ind["PD"] = { ser.mgmt.selected.historyItem(it.substring(0, 1), it.substring(1).split("\b")) }
        ind["PC"] = { ser.mgmt.selected.phonebookComplete() }
        ind["PE"] = { ser.mgmt.selected.historyComplete() }
        ind["SH"] = { ser.inquiryComplete() }
        ind["SF"] = { ser.inquiryResult(it.substring(0, 12), it.substring(12)) }
        ind["MW"] = { ser.mgmt.version = it }
        ind["DB"] = { ser.mgmt.bdaddr = it }
        ind["MM"] = { ser.mgmt.name = it }
        ind["MN"] = { ser.mgmt.pin = it }
        ind["MX"] = { ser.pairlist(it.substring(0, 1).toInt(), it.substring(1, 13), it.substring(13)) }
        ind["II"] = { ser.mgmt.pairMode = true }
        ind["IJ"] = { ser.mgmt.pairMode = false }
        ind["IS"] = { ser.initComplete() }
        ind["JH"] = {}
        ind["SA"] = { ser.mgmt.selected.name = it }
        ind["Mc"] = { it.split(",").let{ser.mgmt.selected.browsingChangePathComplete(it[0], it[1])} }
        ind["Mf"] = { it.split(",").let{ser.mgmt.selected.browsingFolder(it[0], it[1], it[2], it[3])} }
        ind["MF"] = { ser.mgmt.autoconnect(it.substring(0, 1)); ser.mgmt.autoanswer(it.substring(1, 2)) }
        ind["MI"] = { ser.mgmt.selected.avrcpAttribute = it.split("\b") }
        ind["PS"] = { ser.mgmt.selected.signal(it.substring(0, 2)); ser.mgmt.selected.battchg(it.substring(2, 4)) }
        ind["Mm"] = {
            it.split(",").let{
                val str = it[3].split("\b")
                ser.mgmt.selected.browsingMedia(it[0], it[1], it[2], str[0], str[1], str[2], str[3])}
        }
        ind["AL"] = {
            it.split(",").let {
                ser.mgmt.selected.hfpStatus(it[0])
                ser.mgmt.selected.a2dpStatus(it[1])
                ser.mgmt.selected.avrcpStatus(it[2])
            }
        }
    }

    fun onBytes(data: ByteArray) {
        data.forEach {
            if (it == '\r'.toByte() || it == '\n'.toByte()) {
                val cmd = String(buff.sliceArray(0 until count))
                if (cmd.length >= 2) {
                    val c = cmd.substring(0, 2)
                    val a = if (cmd.length > 2) cmd.substring(2) else ""
                    Log.i(TAG, "cmd: $c arg: $a")

                    ind[c]?.let { it(a) }
                }
                count = 0
            } else if (it == 0xFF.toByte()) {
                buff[count++] = '\b'.toByte()
            } else {
                buff[count++] = it
            }

            if (count > 1000)
                count = 0
        }
    }
}
