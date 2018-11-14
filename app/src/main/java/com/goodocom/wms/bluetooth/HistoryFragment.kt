package com.goodocom.wms.bluetooth

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.SimpleAdapter
import com.goodocom.wms.bluetooth.port.BluetoothDevice.*
import com.goodocom.wms.bluetooth.utils.True
import kotlinx.android.synthetic.main.fragment_history.*

class HistoryFragment : Fragment(), History {
    private var dev: BluetoothDeviceImpl? = null
    private var downloading: Boolean = false
    private val callin = ArrayList<Map<String, Any>>()
    private val callout = ArrayList<Map<String, Any>>()
    private val callmiss = ArrayList<Map<String, Any>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        dev?.let { (it.hfpStatus == HfpStatus.CONNECTED).True { syncHistory() } }
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (parentFragment is ProfileFragment)
            dev = (parentFragment as ProfileFragment).dev
        dev?.history = this
        initView()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun initView() {
        ib_call_in.setOnClickListener {
            vp_history.adapter = SimpleAdapter(
                context!!,
                callin, R.layout.history_item,
                arrayOf("name", "number", "date"),
                intArrayOf(R.id.tv_history_name, R.id.tv_history_number, R.id.tv_history_date)
            )
        }
        ib_call_out.setOnClickListener {
            vp_history.adapter = SimpleAdapter(
                context!!,
                callout, R.layout.history_item,
                arrayOf("name", "number", "date"),
                intArrayOf(R.id.tv_history_name, R.id.tv_history_number, R.id.tv_history_date)
            )
        }
        ib_call_missed.setOnClickListener {
            vp_history.adapter = SimpleAdapter(
                context!!,
                callmiss, R.layout.history_item,
                arrayOf("name", "number", "date"),
                intArrayOf(R.id.tv_history_name, R.id.tv_history_number, R.id.tv_history_date)
            )
        }

        vp_history.setOnItemClickListener { parent, _, position, _ ->
            parent.getItemAtPosition(position)?.let { m ->
                val map: HashMap<String, String> = m as HashMap<String, String>
                map["number"]?.let { dev?.Dial(it) }
            }
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        isVisibleToUser.True { if (downloading) showDownloading() else showData() }
        super.setUserVisibleHint(isVisibleToUser)
    }

    override fun onHfpStatus(status: HfpStatus) {
        (status == HfpStatus.CONNECTED).True { syncHistory() }
    }

    override fun onHistoryComplete() {
        downloading = false
    }

    private fun syncHistory() {
        callout.clear()
        callin.clear()
        callmiss.clear()
        dev?.let {
            it.SyncHistory()
            downloading = true
        }
    }

    private fun showDownloading() {
        history_downloading.visibility = View.VISIBLE
        val animation = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
            Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0.8f
        )
        animation.duration = 1000
        animation.fillAfter = false
        animation.repeatCount = -1
        animation.repeatMode = Animation.RESTART
        history_animation.startAnimation(animation)
    }

    private fun showData() {
        history_downloading.visibility = View.GONE
        vp_history.let {
            it.visibility = View.VISIBLE
            it.adapter = SimpleAdapter(
                context!!,
                callin, R.layout.history_item,
                arrayOf("name", "number", "date"),
                intArrayOf(R.id.tv_history_name, R.id.tv_history_number, R.id.tv_history_date)
            )
        }
    }

    override fun onHistoryItem(type: String, name: String, number: String, date: String) {
        fun add(data: ArrayList<Map<String, Any>>) {
            val map = HashMap<String, Any>()
            map["name"] = name
            map["number"] = number
            map["date"] = date
            data.add(map)
        }

        when (type.toInt()) {
            4 -> callout
            5 -> callin
            6 -> callmiss
            else -> null
        }?.let { add(it) }
    }

}
