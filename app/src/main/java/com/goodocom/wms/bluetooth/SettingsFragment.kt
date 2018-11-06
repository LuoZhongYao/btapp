package com.goodocom.wms.bluetooth

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.goodocom.wms.bluetooth.service.BluetoothService
import kotlinx.android.synthetic.main.fragment_settings.*


class SettingsFragment : Fragment(), LocalSettings {
    private val service: BluetoothService.BluetoothServiceImpl
        get() = (activity as MainActivity).service

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        et_device_name.setText(service.LocalName())
        et_pin_code.setText(service.PinCode())
        tv_version.text = service.Version()
        tv_local_bdaddr.text = service.LocalAddress()
        auto_answer_switch.isChecked = service.IsAutoAnswer()
        auto_connect_switch.isChecked = service.IsAutoConnecte()
        auto_connect_switch.setOnCheckedChangeListener {_, checked ->
            if(checked) service.EnableAutoConnect()
            else service.DisableAutoConnect()
        }
        auto_answer_switch.setOnCheckedChangeListener {_, checked ->
            if(checked) service.EnableAutoAnswer()
            else service.DisableAutoAnswer()
        }
        super.onViewCreated(view, savedInstanceState)
    }


    override fun onName(name: String) { et_device_name?.setText(name)}
    override fun onPin(pin: String) { et_pin_code?.setText(pin)}
    override fun onLocalBdaddr(bdaddr: String) { tv_local_bdaddr?.text = bdaddr}
    override fun onVersion(version: String) { tv_version?.text = version}
    override fun onPoweron(on: Boolean) { }
    override fun onAutoConnect(auto: Boolean) { auto_connect_switch?.isChecked = auto }
    override fun onAutoAnswer(auto: Boolean) { auto_answer_switch?.isChecked = auto}
}
