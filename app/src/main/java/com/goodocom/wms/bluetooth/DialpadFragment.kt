package com.goodocom.wms.bluetooth


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.AppCompatButton
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.goodocom.wms.bluetooth.port.BluetoothDevice.HfpStatus
import com.goodocom.wms.bluetooth.port.BluetoothDevice.AudioDirection
import com.goodocom.wms.bluetooth.utils.True
import kotlinx.android.synthetic.main.call.*
import kotlinx.android.synthetic.main.fragment_dialpad.*


class DialpadFragment : Fragment(), Call {
    private var dev: BluetoothDeviceImpl? = null
    private var dtmf: Boolean = false
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
                if(dtmf && dev!!.hfpStatus >= HfpStatus.TALKING)
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
            dtmf = true
            when(dev?.hfpStatus) {
                HfpStatus.CONNECTED -> dev?.Dial(tv_display.text.toString())
                HfpStatus.INCOMING -> dev?.Answer()
                HfpStatus.TALKING -> dev?.Dial(tv_display.text.toString())
                HfpStatus.TWC_INCOMING -> dev?.TwcHoldActiveAcceptOther()
                else -> {}
            }
            (tv_display.length() > 0).True {tv_display.editableText.clear() }
        }
        iv_hangup.setOnClickListener {
            dtmf = true
            when(dev!!.hfpStatus) {
                HfpStatus.TWC_HELD_ACTIVE -> dev!!.TwcReleaseActiveAnswerOther()
                HfpStatus.TWC_INCOMING -> dev!!.TwcReleaseHeldRejectWait()
                else -> if (dev!!.hfpStatus > HfpStatus.CONNECTED) dev!!.Hangup()
            }
        }
        iv_mic_off.setOnClickListener { dev?.MicToggle()  }
        iv_audio_route.setOnClickListener { dev?.VoiceToggle() }
        iv_conference.setOnClickListener { dev?.TwcConference() }
        iv_swap.setOnClickListener { dev?.TwcHoldActiveAcceptOther() }
        iv_addcall.setOnClickListener {
            (tv_display.length() > 0).True {tv_display.editableText.clear() }
            dtmf = false
        }
    }

    private fun update(id1: Int, number1: String, id2: Int = R.string.talking, number2: String = "") {
        tv_primary_call_name?.visibility = View.GONE
        tv_secondary_call_name?.visibility = View.GONE

        tv_primary_call_status?.text = resources.getString(id1)
        tv_primary_call_number?.text = number1
        dev!!.query(number1)?.let {tv_primary_call_name?.text = it; tv_primary_call_name?.visibility = View.VISIBLE}

        tv_secondary_call_status?.text = resources.getString(id2)
        tv_secondary_call_number?.text = number2
        dev!!.query(number2)?.let { tv_secondary_call_name?.text = it; tv_secondary_call_name?.visibility = View.VISIBLE}
    }

    private fun visibility(cond: Boolean): Int = if(cond) View.VISIBLE else View.GONE

    override fun onHfpStatus(status: HfpStatus) {
        when(status) {
            HfpStatus.OUTGOING -> update(R.string.outgoing, dev!!.outgoingNumber)
            HfpStatus.INCOMING -> update(R.string.incoming, dev!!.incomingNumber)
            HfpStatus.TALKING -> update(R.string.talking, dev!!.talkingNumber)
            HfpStatus.TWC_OUTGOING -> update(R.string.talking, dev!!.talkingNumber, R.string.outgoing, dev!!.outgoingNumber)
            HfpStatus.TWC_INCOMING -> update(R.string.talking, dev!!.talkingNumber, R.string.incoming, dev!!.twcWaitNumber)
            HfpStatus.TWC_HELD_ACTIVE -> update(R.string.talking, dev!!.talkingNumber, R.string.held, dev!!.twcHeldNumber)
            HfpStatus.TWC_MULTIPARTY -> update(R.string.talking, dev!!.talkingNumber, R.string.talking, dev!!.talkingNumber)
            HfpStatus.TWC_HELD_REMAINING -> {  }
            else -> {}
        }

        iv_addcall?.visibility = visibility(status == HfpStatus.TALKING)
        iv_conference?.visibility = visibility(status == HfpStatus.TWC_HELD_ACTIVE)
        rl_secondary_call?.visibility = visibility(status > HfpStatus.TALKING)
        iv_swap?.visibility = visibility(status == HfpStatus.TWC_INCOMING || status == HfpStatus.TWC_HELD_ACTIVE)
        rl_call?.visibility =  visibility(status > HfpStatus.CONNECTED)
    }

    override fun onAudio(dir: AudioDirection) {
        iv_audio_route?.setImageResource(
            if(dir == AudioDirection.BLUETOOTH) R.drawable.ic_phone_bluetooth_speaker
            else R.drawable.ic_phone_in_talk)
    }

    override fun onSignal(sig: Int) {

    }

    override fun onBattchg(chg: Int) {

    }

    override fun onMicMuted(muted: Boolean) {
        iv_mic_off?.setImageResource(if(muted) R.drawable.ic_mic_off else R.drawable.ic_mic)
    }
}
