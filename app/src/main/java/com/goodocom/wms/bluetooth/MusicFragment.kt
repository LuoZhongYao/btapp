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
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import com.goodocom.wms.bluetooth.port.BluetoothDevice
import com.goodocom.wms.bluetooth.utils.False
import com.goodocom.wms.bluetooth.utils.True
import kotlinx.android.synthetic.main.fragment_music.*
import java.text.SimpleDateFormat
import java.util.*


class MusicFragment : Fragment(), Media {
    private var dev: BluetoothDeviceImpl? = null
    private var totalTime: Long = 0
    private val data = ArrayList<Map<String, Any>>()
    private var cur: AvrcpBrowsingUid? = null

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

    private fun updateStatus(status: BluetoothDevice.AvrcpStatus) {
        if (status == BluetoothDevice.AvrcpStatus.PLAY) {
            iv_play?.visibility = View.GONE
            iv_pause?.visibility = View.VISIBLE
        } else {
            iv_play?.visibility = View.VISIBLE
            iv_pause?.visibility = View.GONE
        }
    }

    private fun initView() {
        lv_media.adapter = SimpleAdapter(
            context!!, data, R.layout.playlist_item,
            arrayOf("display", "type"), intArrayOf(R.id.tv_display, R.id.iv_type)
        )



        dev?.let {
            updateStatus(it.avrcpStatus)
            onAvrcpAttribute(it.avrcpAttribute)
        }
    }

    private fun initListener() {
        iv_play.setOnClickListener { dev?.Play() }
        iv_pause.setOnClickListener { dev?.Pause() }
        iv_next.setOnClickListener { dev?.Forward() }
        iv_previous.setOnClickListener { dev?.Backward() }
        iv_back.setOnClickListener { cur?.let { dev?.BrowsingChangePath(0, it.msb, it.lsb)}}
        iv_refresh.setOnClickListener {
            data.clear()
            (lv_media?.adapter as SimpleAdapter?)?.notifyDataSetChanged()
            dev?.BrowsingRetrieveFilesystem(0, 65535)
        }
        iv_vol_down.setOnClickListener { }
        iv_vol_up.setOnClickListener { }
        lv_media.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            val lv = parent as ListView
            val map: HashMap<String, Any> = lv.getItemAtPosition(position) as HashMap<String, Any>
            val obj = map.get("obj")
            (obj is AvrcpBrowsingUid).True { cur = obj as AvrcpBrowsingUid }
            when (obj) {
                is MediaItem -> dev?.BrowsingPlayItem(obj.msb, obj.lsb)
                is FolderItem -> dev?.BrowsingChangePath(1, obj.msb, obj.lsb)
                is MediaPlayerItem -> dev?.BrowsingSetMediaPlayer(obj.id)
            }
        }
    }

    fun add(obj: AvrcpBrowsingItem) {
        val map = HashMap<String, Any>()
        map.put("display", obj.display)
        map.put("type", obj.img)
        map.put("obj", obj)
        data.add(map)
        (lv_media?.adapter as SimpleAdapter?)?.notifyDataSetChanged()
    }

    override fun onA2dpStatus(status: BluetoothDevice.A2dpStatus) {

    }

    override fun onAvrcpStatus(status: BluetoothDevice.AvrcpStatus) {
        updateStatus(status)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onAvrcpPlaybackPos(pos: Long) {
        val fmt = SimpleDateFormat("mm:ss:SS")
        tv_currenttime?.text = fmt.format(Date(pos))
        (totalTime != 0L).True { sb_progress?.progress = (pos * 100 / totalTime).toInt() }

    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    override fun onAvrcpAttribute(attr: List<String>) {
        val fmt = SimpleDateFormat("mm:ss:SS")
        attr[2].isEmpty().False { totalTime = attr[2].toLong() }
        tv_music_name?.text = attr[0]
        tv_music_artist?.text = attr[1]
        tv_totaltime?.text = fmt.format(Date(totalTime))
        tv_music_posandtotal?.text = attr[3] + "/" + attr[4]
    }

    override fun onAvrcpBrowsingChangePathComplete(status: Int, num_items: Int) {
        if (status == 0) {
            data.clear()
            dev?.BrowsingRetrieveFilesystem(0, num_items)
            (lv_media?.adapter as SimpleAdapter?)?.notifyDataSetChanged()
        }
    }

    override fun onAvrcpBrowsingFolder(folder: FolderItem) {
        add(folder)
    }

    override fun onAvrcpBrowsingMedia(media: MediaItem) {
        add(media)
    }

    override fun onAvrcpBrowsingMediaPlayer(player: MediaPlayerItem) {
        add(player)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        isVisibleToUser.True { dev?.AudioSource() }
        super.setUserVisibleHint(isVisibleToUser)
    }

    companion object {
        private const val TAG = "btapp"
    }
}
