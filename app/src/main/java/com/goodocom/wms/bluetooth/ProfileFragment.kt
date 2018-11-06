package com.goodocom.wms.bluetooth


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.app.FragmentStatePagerAdapter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlin.properties.Delegates


class ProfileFragment : Fragment() {
    var dev: BluetoothDeviceImpl by Delegates.notNull()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        val tabItem = arrayOf(
            DialpadFragment::class.java,
            MusicFragment::class.java,
            ContactFragment::class.java,
            HistoryFragment::class.java
        )
        vp_profile.adapter = object : FragmentPagerAdapter(childFragmentManager) {
            override fun getItem(pos :Int): Fragment = tabItem[pos].newInstance() as Fragment
            override fun getCount(): Int = tabItem.size
            //override fun getItemPosition(`object`: Any): Int = FragmentStatePagerAdapter.POSITION_NONE
        }
        ib_dialpad.setOnClickListener { vp_profile.setCurrentItem(0, false)}
        ib_music.setOnClickListener { vp_profile.setCurrentItem(1, false)}
        ib_contact.setOnClickListener { vp_profile.setCurrentItem(2, false)}
        ib_history.setOnClickListener { vp_profile.setCurrentItem(3, false)}
    }
}
