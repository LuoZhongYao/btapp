package com.goodocom.wms.bluetooth.service

import android.util.Log
import com.goodocom.wms.bluetooth.port.BluetoothDevice.*

class BluetoothInd(val service: BluetoothService)
{
    companion object {
        private const val TAG = "btapp"
    }
    private val ind: HashMap<String, (arg: String) -> Unit> = HashMap()
    private val buff = ByteArray(1024)
    private var count = 0

    init {
        ind["A0"] = { service.mgmt.selected(it) }
        ind["P0"] = { service.mgmt.poweron = true }
        ind["P1"] = { service.mgmt.poweron = false }
        ind["IA"] = { service.mgmt.selected.hfpStatus = HfpStatus.DISCONNECTED }
        ind["IB"] = { service.mgmt.selected.hfpStatus = HfpStatus.CONNECTED }
        ind["IV"] = { service.mgmt.selected.hfpStatus = HfpStatus.CONNECTING }
        ind["id"] = { service.mgmt.selected.hfpStatus = HfpStatus.INCOMING }
        ind["IG"] = { service.mgmt.selected.hfpStatus = HfpStatus.TALKING }
        ind["IF"] = { service.mgmt.selected.hfpStatus = HfpStatus.CONNECTED }
        ind["IL"] = {}
        ind["IM"] = {}
        ind["IN"] = {}
        ind["IK"] = {}
        ind["ID"] = { service.mgmt.selected.number = it; service.mgmt.selected.hfpStatus = HfpStatus.INCOMING }
        ind["IC"] = { service.mgmt.selected.number = it; service.mgmt.selected.hfpStatus = HfpStatus.OUTGOING }
        ind["IR"] = { service.mgmt.selected.number = it; service.mgmt.selected.hfpStatus = HfpStatus.TALKING }
        ind["IE"] = { service.mgmt.selected.number = it }
        ind["IH"] = { service.mgmt.selected.number = it }
        ind["IO"] = { service.mgmt.selected.micMuted = it.toInt() == 1 }
        ind["MC"] = { service.mgmt.selected.audioDirection = AudioDirection.BLUETOOTH }
        ind["MD"] = { service.mgmt.selected.audioDirection = AudioDirection.PHONE }
        ind["MG"] = { service.mgmt.selected.hfpStatus(it) }
        ind["MA"] = { service.mgmt.selected.avrcpStatus = AvrcpStatus.PAUSE }
        ind["MB"] = { service.mgmt.selected.avrcpStatus = AvrcpStatus.PLAY }
        ind["MS"] = { service.mgmt.selected.avrcpStatus = AvrcpStatus.STOP }
        ind["ML"] = {}
        ind["MP"] = { service.mgmt.selected.avrcpPlaybackPos = it.toLong() }
        ind["MU"] = { service.mgmt.selected.a2dpStatus(it) }
        ind["PA"] = { service.mgmt.selected.pbapStatus(it) }
        ind["PB"] = { service.mgmt.selected.phonebookItem(it.split("\b")) }
        ind["PD"] = { service.mgmt.selected.historyItem(it.substring(0, 1), it.substring(1).split("\b")) }
        ind["PC"] = { service.mgmt.selected.phonebookComplete() }
        ind["PE"] = { service.mgmt.selected.historyComplete() }
        ind["SH"] = { service.inquiryComplete() }
        ind["SF"] = { service.inquiryResult(it.substring(0, 12), it.substring(12)) }
        ind["MW"] = { service.mgmt.version = it }
        ind["DB"] = { service.mgmt.bdaddr = it }
        ind["MM"] = { service.mgmt.name = it }
        ind["MN"] = { service.mgmt.pin = it }
        ind["MX"] = { service.pairlist(it.substring(0, 1).toInt(), it.substring(1, 13), it.substring(13)) }
        ind["II"] = { service.mgmt.pairMode = true }
        ind["IJ"] = { service.mgmt.pairMode = false }
        ind["IS"] = { service.initComplete() }
        ind["JH"] = {}
        ind["SA"] = { service.mgmt.selected.name = it }
        ind["Mc"] = { service.mgmt.selected.browsingChangePathComplete(it) }
        ind["Mf"] = { service.mgmt.selected.browsingFolder(it) }
        ind["Mm"] = { service.mgmt.selected.browsingMedia(it) }
        ind["PS"] = { service.mgmt.selected.signal(it.substring(0, 2)); service.mgmt.selected.battchg(it.substring(2, 4)) }
        ind["MF"] = { service.mgmt.autoconnect(it.substring(0, 1)); service.mgmt.autoanswer(it.substring(1, 2)) }
        ind["MI"] = { service.mgmt.selected.avrcpAttribute(it.split("\b")) }
        ind["AL"] = { it.split(",").let { service.mgmt.selected.hfpStatus(it[0]); service.mgmt.selected.a2dpStatus(it[1])}
        }
    }

    fun onBytes(data: ByteArray) {
        data.forEach {
            if(it == '\r'.toByte() || it == '\n'.toByte()) {
                val cmd = String(buff.sliceArray( 0 until count))
                if(cmd.length >= 2) {
                    val c = cmd.substring(0, 2)
                    val a = if(cmd.length > 2) cmd.substring(2) else ""
                    Log.i(TAG, "cmd: $c arg: $a")

                    ind[c]?.let { it(a) }
                }
                count = 0
            } else if(it == 0xFF.toByte()) {
                buff[count++] = '\b'.toByte()
            } else {
                buff[count++] = it
            }

            if(count > 1000)
                count = 0
        }
    }
}
