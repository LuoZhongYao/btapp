package com.goodocom.wms.bluetooth


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.goodocom.wms.bluetooth.utils.True
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlin.properties.Delegates


class ProfileFragment : Fragment(), FragmentId, Profile {
    override var position: Int = 0
    override var id: Long = 0L
    var dev: BluetoothDeviceImpl by Delegates.notNull()
    var parent: ViewPager by Delegates.notNull()

    private var old_pos: Int = 0
    private var old_item: Int = 0

    private val tabItem = arrayOf(
        DialpadFragment::class.java,
        MusicFragment::class.java,
        ContactFragment::class.java,
        HistoryFragment::class.java
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        parent = container as ViewPager
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dev.profile = this
        initView()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun initView() {

        vp_profile.adapter = ProfilePagerAdapter()
        vp_profile.offscreenPageLimit = 4
        ib_dialpad.setOnClickListener { vp_profile.setCurrentItem(0, false) }
        ib_music.setOnClickListener { vp_profile.setCurrentItem(1, false) }
        ib_contact.setOnClickListener { vp_profile.setCurrentItem(2, false) }
        ib_history.setOnClickListener { vp_profile.setCurrentItem(3, false) }
    }

    override fun active() {
        (parent.currentItem != position).True {
            old_pos = parent.currentItem
            parent.currentItem = position
        }
        vp_profile?.let {
            (it.currentItem != 0).True {
                old_item = it.currentItem
                it.setCurrentItem(0, false)
            }
        }
    }

    override fun inactive() {
        Log.i("btapp", "old_pos $old_pos, old_item $old_item")
        parent.currentItem = old_pos
        vp_profile?.setCurrentItem(old_item, false)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        vp_profile?.adapter?.let { (it as ProfilePagerAdapter).currentFragment?.userVisibleHint = isVisibleToUser }
        super.setUserVisibleHint(isVisibleToUser)
    }

    inner class ProfilePagerAdapter : FragmentPagerAdapter(childFragmentManager) {
        var currentFragment: Fragment? = null
        override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
            currentFragment = `object` as Fragment
            super.setPrimaryItem(container, position, `object`)
        }

        override fun getItem(pos: Int): Fragment = tabItem[pos].newInstance() as Fragment
        override fun getCount(): Int = tabItem.size
        //override fun getItemPosition(`object`: Any): Int = FragmentStatePagerAdapter.POSITION_NONE
    }
}
