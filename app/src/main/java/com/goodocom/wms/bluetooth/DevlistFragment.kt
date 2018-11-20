package com.goodocom.wms.bluetooth


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.SimpleAdapter
import android.widget.TextView
import com.goodocom.wms.bluetooth.service.BluetoothService
import kotlinx.android.synthetic.main.fragment_devlist.*
import kotlin.properties.Delegates


class DevlistFragment : Fragment(), DeviceList, FragmentId {
    override var id: Long = 0L
    override var position = 0
    private val pairList = ArrayList<Map<String, Any>>()
    private var pairAdapter: SimpleAdapter by Delegates.notNull()
    private val devAdapter = object: BaseAdapter() {
        val data = ArrayList<BluetoothDeviceImpl>()
        override fun getCount(): Int = data.size
        override fun getItem(position: Int): Any = data[position]
        override fun getItemId(position: Int): Long = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            class ViewHolder(val name: TextView, val bdaddr: TextView)
            val view: View
            val holder: ViewHolder
            if (convertView == null) {
                view = LayoutInflater.from(context!!).inflate(R.layout.device_item, parent, false)
                holder = ViewHolder(view.findViewById(R.id.tv_name), view.findViewById(R.id.tv_bdaddr))
                view.tag = holder
            } else {
                holder = convertView.tag as ViewHolder
                view = convertView
            }
            holder.name.text = data[position].name
            holder.bdaddr.text = data[position].bdaddr

            return view
        }
    }

    private val service: BluetoothService.BluetoothServiceImpl
        get() = (activity as MainActivity).service


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_devlist, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        pairList.clear()
        initView()
        service.ReadPairList()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun initView() {
        pairAdapter = SimpleAdapter(context!!, pairList, R.layout.device_item,
            arrayOf("bdaddr", "name"), intArrayOf(R.id.tv_bdaddr, R.id.tv_name))
        lv_pairlist.adapter = pairAdapter
        lv_pairlist.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            pairList[position]["bdaddr"]?.let { service.Connect(it as String)}
        }

        lv_devlist.adapter = devAdapter
        lv_devlist.onItemClickListener = AdapterView.OnItemClickListener { parent, _, position, _ ->
            parent?.getItemAtPosition(position).let { (it as BluetoothDeviceImpl).Disconnect() }
        }
    }

    private fun add(index: Int, name: String, bdaddr: String) {
        val map = HashMap<String, Any>()
        map["bdaddr"] = bdaddr
        map["name"] = name
        map["index"] = index
        if(index == 1)
            pairList.clear()
        pairList.add(map)
        pairAdapter.notifyDataSetChanged()
    }

    override fun onPairDevice(index: Int, bdaddr: String, name: String) {
        add(index, name, bdaddr)
    }

    override fun onConnectedDevice(dev: Array<BluetoothDeviceImpl>) {
        devAdapter.data.clear()
        devAdapter.data.addAll(dev)
        devAdapter.notifyDataSetChanged()
    }
}
