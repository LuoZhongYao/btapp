package com.goodocom.wms.bluetooth


import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.CursorAdapter
import android.widget.SimpleCursorAdapter
import com.goodocom.wms.bluetooth.port.BluetoothDevice
import com.goodocom.wms.bluetooth.utils.True
import kotlinx.android.synthetic.main.fragment_contact.*


class ContactFragment : Fragment(), Phonebook {
    private var dev: BluetoothDeviceImpl? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (parentFragment is ProfileFragment)
            dev = (parentFragment as ProfileFragment).dev
        dev?.book = this
        initView()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun initView() {
        btn_sync.setOnClickListener {
            dev?.SyncPhonebook()
            rl_downloading.visibility = View.VISIBLE
            lv_content.visibility = View.GONE
            btn_sync.visibility = View.GONE
            btn_cancel.visibility = View.VISIBLE
            val animation = TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0.8f
            )
            animation.duration = 1000
            animation.fillAfter = false
            animation.repeatCount = -1
            animation.repeatMode = Animation.RESTART
            image_animation!!.startAnimation(animation)
        }

        btn_cancel.setOnClickListener {
            dev?.CancelSync()
        }
        lv_content.adapter = SimpleCursorAdapter(
            context!!, R.layout.contact_item, null,
            arrayOf("name", "number"), intArrayOf(R.id.tv_name, R.id.tv_number),
            CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        )
        lv_content.setOnItemClickListener { parent, _, position, _ ->
            parent.getItemAtPosition(position)?.let { m ->
                (m as Cursor).let { cur ->
                    cur.getColumnIndex("number")?.let { index ->
                        cur.getString(index)?.let { number ->
                            dev?.Dial(number)
                        }
                    }
                }
            }
        }
    }

    private fun complete() {
        rl_downloading?.visibility = View.GONE
        btn_cancel?.visibility = View.GONE
        btn_sync?.visibility = View.VISIBLE
        lv_content?.visibility = View.VISIBLE
    }

    override fun onPbapStatus(status: BluetoothDevice.PbapStatus) {
        (status == BluetoothDevice.PbapStatus.UNSUPPORTED).True {complete()}
    }

    override fun onPhonebookComplete(cursor: Cursor?) {
        complete()
        cursor?.let {(lv_content?.adapter as SimpleCursorAdapter?)?.changeCursor(it)}
    }
}
