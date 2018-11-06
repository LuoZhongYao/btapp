package com.goodocom.wms.bluetooth


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.SimpleAdapter
import com.goodocom.wms.bluetooth.service.BluetoothService
import kotlinx.android.synthetic.main.fragment_search.*
import kotlin.properties.Delegates


class SearchFragment : Fragment() {
    private val data = ArrayList<Map<String, String>>()
    private val map = HashMap<String, String>()
    private var adapter: SimpleAdapter by Delegates.notNull()
    private val service: BluetoothService.BluetoothServiceImpl
        get() = (activity as MainActivity).service

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()
        registerBroad()
        btn_search.setOnClickListener { data.clear(); service.Search();pb_search.visibility = View.VISIBLE }
        btn_cancel.setOnClickListener { service.CancelSearch(); pb_search.visibility = View.GONE }
        super.onViewCreated(view, savedInstanceState)
    }

    private fun initView() {
        adapter = SimpleAdapter(context!!, data, R.layout.device_item,
            arrayOf("bdaddr", "name"), intArrayOf(R.id.tv_bdaddr, R.id.tv_name))
        lv_search.adapter = adapter
        lv_search.onItemClickListener = object: AdapterView.OnItemClickListener {
            override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                data[position]["bdaddr"]?.let { service.Connect(it)}
            }
        }
    }

    private fun registerBroad() {
        val intentFilter = IntentFilter()
        intentFilter.addAction("search")
        LocalBroadcastManager.getInstance(context!!).registerReceiver(object: BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                intent.getStringExtra("complete")?.let { pb_search?.visibility = View.GONE}
                intent.getStringArrayExtra("result")?.let { add(it[1], it[0])}
            }
        },intentFilter)
    }

    private fun add(name: String, bdaddr: String) {
        data.clear()
        map.put(bdaddr, name)
        map.forEach {
            val m = HashMap<String, String>()
            m["bdaddr"] = it.key
            m["name"] = it.value
            data.add(m)
        }
        adapter.notifyDataSetChanged()
    }
}
