package com.goodocom.wms.bluetooth


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.goodocom.wms.bluetooth.service.BluetoothService
import kotlinx.android.synthetic.main.fragment_search.*
import kotlin.properties.Delegates


class SearchFragment : Fragment(), Search, FragmentId {
    override var id: Long = 0L
    override var position = 0
    private val map = HashMap<String, String>()
    private val service: BluetoothService.BluetoothServiceImpl
        get() = (activity as MainActivity).service

    private var adapter = object: BaseAdapter() {
        val data = ArrayList<Map<String, String>>()
        override fun getCount(): Int = data.size
        override fun getItem(position: Int): Any = data[position]
        override fun getItemId(position: Int): Long = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            class ViewHolder(val name: TextView, val bdaddr: TextView, val headset: ImageView, val a2dp: ImageView)
            val view: View
            val holder: ViewHolder
            if (convertView == null) {
                view = LayoutInflater.from(context!!).inflate(R.layout.search_item, parent, false)
                holder = ViewHolder(view.findViewById(R.id.tv_name),
                    view.findViewById(R.id.tv_bdaddr),
                    view.findViewById(R.id.iv_headset),
                    view.findViewById(R.id.iv_music))
                view.tag = holder
            } else {
                holder = convertView.tag as ViewHolder
                view = convertView
            }
            holder.name.text = data[position]["name"]
            holder.bdaddr.text = data[position]["bdaddr"]
            holder.headset.tag = position
            holder.headset.setOnClickListener {
                data[it.tag as Int]["bdaddr"]?.let { service.ConnectAghfp(it) }
            }
            holder.a2dp.tag = position
            holder.a2dp.setOnClickListener {
                data[it.tag as Int]["bdaddr"]?.let { service.ConnectA2dp(it) }
            }
            return view
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()
        btn_search.setOnClickListener { map.clear(); service.Search();pb_search.visibility = View.VISIBLE }
        btn_cancel.setOnClickListener { service.CancelSearch(); pb_search.visibility = View.GONE }
        super.onViewCreated(view, savedInstanceState)
    }

    private fun initView() {
        lv_search.adapter = adapter
        lv_search.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            adapter.data[position]["bdaddr"]?.let { service.Connect(it)}
        }
    }

    private fun add(name: String, bdaddr: String) {
        adapter.data.clear()
        map[bdaddr] = name
        map.forEach {
            val m = HashMap<String, String>()
            m["bdaddr"] = it.key
            m["name"] = it.value
            adapter.data.add(m)
        }
        adapter.notifyDataSetChanged()
    }

    override fun onComplete() { pb_search?.visibility = View.GONE}
    override fun onResult(name: String, bdaddr: String) = add(name, bdaddr)
}
