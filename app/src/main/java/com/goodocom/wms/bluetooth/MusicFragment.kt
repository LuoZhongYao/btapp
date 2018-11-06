package com.goodocom.wms.bluetooth


import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.goodocom.wms.bluetooth.port.BluetoothDevice
import com.goodocom.wms.bluetooth.utils.False
import com.goodocom.wms.bluetooth.utils.True
import kotlinx.android.synthetic.main.fragment_music.*
import java.text.SimpleDateFormat
import java.util.*


class MusicFragment : Fragment(), Media {
    private var dev: BluetoothDeviceImpl? = null
    private var totalTime: Long = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_music, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (parentFragment is ProfileFragment) {
            dev = (parentFragment as ProfileFragment).dev
        }
        dev?.media = this
        initListener()
        initView()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun initView() {

    }

    private fun initListener() {
        iv_play.setOnClickListener { dev?.Play() }
        iv_pause.setOnClickListener { dev?.Pause() }
        iv_next.setOnClickListener { dev?.Forward() }
        iv_previous.setOnClickListener { dev?.Backward() }
        iv_vol_down.setOnClickListener {  }
        iv_vol_up.setOnClickListener {  }
    }


    override fun onA2dpStatus(status: BluetoothDevice.A2dpStatus) {

    }

    override fun onAvrcpStatus(status: BluetoothDevice.AvrcpStatus) {
        if(status == BluetoothDevice.AvrcpStatus.PLAY) {
            iv_play?.visibility = View.GONE
            iv_pause?.visibility = View.VISIBLE
        } else {
            iv_play?.visibility = View.VISIBLE
            iv_pause?.visibility = View.GONE
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun onAvrcpPlaybackPos(pos: Long) {
        val fmt = SimpleDateFormat("mm:ss:SS")
        tv_currenttime?.text = fmt.format(Date(pos))
        (totalTime != 0L).True { sb_progress?.progress =  (pos * 100 / totalTime).toInt()}

    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onAvrcpAttribute(attr: List<String>) {
        val fmt = SimpleDateFormat("mm:ss:SS")
        attr[2].isEmpty().False { totalTime = attr[2].toLong()}
        tv_music_name?.text = attr[0]
        tv_music_artist?.text = attr[1]
        tv_totaltime?.text = fmt.format(Date(totalTime))
        tv_music_posandtotal?.text = attr[3] + "/" + attr[4]
    }

    companion object {
        private const val TAG = "btapp"
    }

}
