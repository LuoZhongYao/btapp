package com.goodocom.wms.bluetooth

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.goodocom.wms.bluetooth.service.BluetoothService
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity(), ServiceConnection {
    private val callback = BluetoothCallbackImpl(this)
    private val fixedTab = listOf(
        TabItem(R.drawable.ic_search, SearchFragment::class.java, "搜索"),
        TabItem(R.drawable.ic_pairlist, DevlistFragment::class.java, "配对记录"),
        TabItem(R.drawable.ic_settings, SettingsFragment::class.java, "设置")
    )
    private var device: HashMap<String, BluetoothDeviceImpl> = HashMap()
    open class TabItem(val image: Int, val clazz: Class<*>, val text: String, val obj: Any? = null) {
        open fun getTitle(): String = text
    }
    private val tabItem = fixedTab.toMutableList()

    var service: BluetoothService.BluetoothServiceImpl by Delegates.notNull()
    var selected: BluetoothDeviceImpl? = null

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        this.service = service as BluetoothService.BluetoothServiceImpl
        this.service.register(callback)
        initView()
        Log.i(TAG, "onServiceConnected")
    }

    override fun onServiceDisconnected(name: ComponentName?) {
    }

    private fun updateFragment() {
        val ft = supportFragmentManager.beginTransaction()
        tabItem.retainAll(fixedTab)
        device.forEach { tabItem.add(0, it.value.tabItem)}
        supportFragmentManager.fragments.forEach { ft.remove(it) }
        ft.commit()
        vp_content.adapter?.notifyDataSetChanged()
        for (i in 0 until tl_tablayout.tabCount)
            tl_tablayout.getTabAt(i)?.setIcon(tabItem[i].image)
        vp_content.currentItem = 0
        tl_tablayout.getTabAt(0)?.let { it.select() }
        callback.connectedDevices(device.values.toTypedArray())
    }

    fun broadcast(action: String, gen: (intent: Intent) -> Unit) {
        val intent = Intent(action)
        gen(intent)
        LocalBroadcastManager.getInstance(this.applicationContext).sendBroadcast(intent)
    }

    fun dmAdd(bdaddr: String) {
        Log.i(TAG, "selected: $bdaddr, " + (bdaddr == BluetoothService.INVALID))
        if(bdaddr == BluetoothService.INVALID) {
            device.forEach {service.devUnregister(it.value.bdaddr, it.value)}
            device.clear()
            updateFragment()
        } else if (device[bdaddr] == null) {
            val dev = BluetoothDeviceImpl(this.applicationContext, bdaddr, service) { vp_content.adapter?.notifyDataSetChanged() }
            service.devRegister(bdaddr, dev)
            device[bdaddr] = dev
            updateFragment()
        }
        device[bdaddr]?.let {selected = it}
    }

    fun dmRemove(bdaddr: String) {
        Log.i(TAG, "REMOVE: $bdaddr")
        device[bdaddr]?.let {
            service.devUnregister(it.bdaddr, it)
            device.remove(it.bdaddr)
            updateFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val intent = Intent(this, BluetoothService::class.java)
        startService(intent)
        bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    private fun initView() {
        tl_tablayout.setupWithViewPager(vp_content)
        vp_content.adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
            override fun getItem(pos :Int): Fragment {
                val fm = tabItem[pos].clazz.newInstance() as Fragment
                when(fm) {
                    is DevlistFragment -> { callback.devs = fm; fm.onConnectedDevice(device.values.toTypedArray())}
                    is SettingsFragment -> callback.settings = fm
                    is SearchFragment -> {}
                    is ProfileFragment -> fm.dev = tabItem[pos].obj as BluetoothDeviceImpl
                }
                return fm
            }
            override fun getCount(): Int = tabItem.size
            override fun getPageTitle(position: Int): CharSequence? = tabItem[position].getTitle()
            override fun getItemPosition(`object`: Any): Int = POSITION_NONE
        }

        for (i in 0 until tl_tablayout.tabCount)
            tl_tablayout.getTabAt(i)?.setIcon(tabItem[i].image)
    }

    override fun onDestroy() {
        super.onDestroy()
        service.unregister(callback)
        unbindService(this)
    }

    companion object {
        private const val TAG = "btapp"
    }
}
