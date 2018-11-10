package com.goodocom.wms.bluetooth


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.AppCompatButton
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.goodocom.wms.bluetooth.port.BluetoothDevice
import kotlinx.android.synthetic.main.call.*
import kotlinx.android.synthetic.main.fragment_dialpad.*


class DialpadFragment : Fragment(), Call {
    private var dev: BluetoothDeviceImpl? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dialpad, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (parentFragment is ProfileFragment)
            dev = (parentFragment as ProfileFragment).dev
        dev?.call = this
        initListener()
        dev?.let {
            onHfpStatus(it.hfpStatus)
            onNumber(it.number)
            onMicMuted(it.micMuted)
            onAudio(it.audioDirection)
        }
        super.onViewCreated(view, savedInstanceState)
    }

    private fun initListener() {
        arrayOf(btn_number0, btn_number1, btn_number2, btn_number3, btn_number4, btn_number5,
            btn_number6,  btn_number7, btn_number8, btn_number9, btn_number10, btn_number11).forEach { btn ->
            btn.setOnClickListener {
                val btn = it as AppCompatButton
                if(dev?.hfpStatus == BluetoothDevice.HfpStatus.TALKING)
                    dev?.DTMF((btn.text.toString()))
                tv_display.append(btn.text)
            }
        }

        btn_redial.setOnClickListener { dev?.Redial() }
        btn_backspace.setOnClickListener {
            val length = tv_display.length()
            if (length > 0)
                tv_display.editableText.delete(length - 1, length)
        }
        btn_dial.setOnClickListener {
            when(dev?.hfpStatus) {
                BluetoothDevice.HfpStatus.CONNECTED -> dev?.Dial(tv_display.text.toString())
                BluetoothDevice.HfpStatus.INCOMING -> dev?.Answer()
            }
        }
        iv_hangup.setOnClickListener { dev?.Hangup() }
        iv_mic_off.setOnClickListener { dev?.MicToggle()  }
        iv_audio_route.setOnClickListener { dev?.VoiceToggle() }
    }

    override fun onHfpStatus(status: BluetoothDevice.HfpStatus) {
        when(status) {
            BluetoothDevice.HfpStatus.OUTGOING -> {
                tv_callstatus?.text = resources.getString(R.string.outgoing)
            }
            BluetoothDevice.HfpStatus.INCOMING -> {
                tv_callstatus?.text = resources.getString(R.string.incoming)
            }
            BluetoothDevice.HfpStatus.TALKING -> {
                tv_callstatus?.text = resources.getString(R.string.talking)
            }
        }
        rl_call?.visibility =  if (status > BluetoothDevice.HfpStatus.CONNECTED)  View.VISIBLE else View.GONE
    }

    override fun onAudio(dir: BluetoothDevice.AudioDirection) {
        iv_audio_route?.setImageResource(
            if(dir == BluetoothDevice.AudioDirection.BLUETOOTH) R.drawable.ic_phone_bluetooth_speaker
            else R.drawable.ic_phone_in_talk)
    }

    override fun onSignal(sig: Int) {

    }

    override fun onBattchg(chg: Int) {

    }

    override fun onNumber(number: String) {
        tv_call_number?.text = number
    }

    override fun onMicMuted(muted: Boolean) {
        iv_mic_off?.setImageResource(if(muted) R.drawable.ic_mic_off else R.drawable.ic_mic)
    }
}
